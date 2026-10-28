package com.khti.fastikvpn.vpn

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.khti.fastikvpn.R
import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.data.source.TestVlessSource
import com.khti.fastikvpn.data.vless.XrayConfigBuilder
import com.khti.fastikvpn.domain.model.VlessConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

// Сам VPN-сервис. Его задачи:
// 1) создать виртуальную сетевую карту (TUN);
// 2) запустить ядро Xray и отдать ему эту карту;
// 3) при отключении остановить всё в правильном порядке.
//
// @AndroidEntryPoint позволяет Hilt заполнить поля, помеченные @Inject
@AndroidEntryPoint
class FastikVpnService : VpnService() {

    // Общее табло состояния. Hilt подставит тот же объект, что видит экран
    @Inject lateinit var statusHolder: VpnStatusHolder

    // Фабрика уведомления
    @Inject lateinit var notificationFactory: VpnNotificationFactory

    // Обёртка над ядром Xray
    @Inject lateinit var xrayCore: XrayCore

    // Источник параметров сервера. Пока это тестовая ссылка из local.properties,
    // позже сюда придёт выбранный пользователем сервер
    @Inject lateinit var vlessSource: TestVlessSource

    // Сборщик JSON-настроек для ядра
    @Inject lateinit var configBuilder: XrayConfigBuilder

    // Область для корутин сервиса. Корутина - задача, которая умеет ждать, не замораживая приложение.
    // Main - главный поток; SupervisorJob - если одна задача упала, остальные продолжают жить
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Задача запуска и задача остановки. Храним, чтобы не запустить дважды и уметь отменить
    private var startJob: Job? = null
    private var stopJob: Job? = null

    // Открытая виртуальная карта. Пока она не null, туннель создан.
    // ParcelFileDescriptor - обёртка над файловым дескриптором (номером открытого "файла").
    private var tunInterface: ParcelFileDescriptor? = null

    // Параметры сервера, с которыми работаем. Нужны для закрашивания секретов в логах
    private var activeConfig: VlessConfig? = null

    // Система вызывает эту функцию каждый раз, когда кто-то отправил сервису команду.
    // По полю action мы узнаём, что именно нужно сделать.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTunnel()
            ACTION_STOP -> stopTunnel()
            // Команда пришла без действия (например, после перезапуска системой): останавливаемся
            else -> stopSelf()
        }
        // NOT_STICKY: если систему вынудили убить сервис, не перезапускать его самой.
        return START_NOT_STICKY
    }

    // Включение VPN
    private fun startTunnel() {
        // Уже включаемся или включены: повторная команда ничего не меняет
        if (startJob?.isActive == true || tunInterface != null) return

        statusHolder.update(ConnectionState.CONNECTING)

        // Правило Android: после startForegroundService сервис обязан в течение ~5 секунд
        // показать уведомление. Поэтому делаем это первым делом.
        showForegroundNotification()

        startJob = serviceScope.launch {
            try {
                // Шаг 1. Берём параметры сервера и собираем из них JSON для ядра.
                // getOrThrow бросит исключение, если ссылка не задана или неверна
                val vless = vlessSource.getConfig().getOrThrow()
                activeConfig = vless
                val configJson = configBuilder.build(vless)

                // Шаг 2. Просим систему создать виртуальную карту.
                // null означает, что система отказала (например, пропало разрешение)
                val tun = createTunnelBuilder().establish()
                    ?: throw IllegalStateException("Система не создала туннель")
                tunInterface = tun

                // Шаг 3. Запускаем ядро в фоновом потоке, чтобы не замораживать приложение.
                // Ядру отдаём тот же номер, который держим мы, без копии. Владелец номера
                // остаётся сервис: ядро только читает из него, а закрываем мы сами
                // после остановки ядра. Тогда, когда мы закроем карту, она реально выключится
                withContext(Dispatchers.IO) {
                    xrayCore.start(configJson, tun.fd)
                }

                // Всё получилось
                statusHolder.update(ConnectionState.CONNECTED)
                Log.i(TAG, "VPN включён")
            } catch (e: CancellationException) {
                // Нас отменили (пользователь нажал "выключить"): пробрасываем дальше как есть
                throw e
            } catch (e: Exception) {
                // Любая другая ошибка: пишем в журнал без секретов и выключаемся
                Log.e(TAG, "Не удалось включить VPN: ${redact(e.message)}")
                stopTunnel()
            }
        }
    }

    // Показывает постоянное уведомление и переводит сервис в режим "на переднем плане"
    private fun showForegroundNotification() {
        val notification = notificationFactory.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14 и новее: нужно указать тип сервиса, он совпадает с манифестом
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    // Описывает параметры виртуальной карты.
    // Builder - "конструктор", в который мы по очереди складываем настройки.
    private fun createTunnelBuilder(): Builder {
        val builder = Builder()
            // Название, которое пользователь увидит в системных настройках VPN
            .setSession(getString(R.string.app_name))
            // Максимальный размер пакета
            .setMtu(TUN_MTU)
            // Адрес самой виртуальной карты внутри телефона (IPv4 и IPv6)
            .addAddress(TUN_ADDRESS_V4, TUN_PREFIX_V4)
            .addAddress(TUN_ADDRESS_V6, TUN_PREFIX_V6)
            // Какой трафик направить в туннель. Нулевой маршрут означает "весь".
            // IPv6 тоже нужен, иначе трафик по нему пошёл бы мимо VPN (утечка)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)
            // DNS-серверы внутри туннеля, чтобы запросы имён не утекали мимо VPN
            .addDnsServer(DNS_PRIMARY)
            .addDnsServer(DNS_SECONDARY)

        if (EXCLUDE_SELF) {
            try {
                // Исключаем наше приложение из туннеля: ядро Xray работает внутри него,
                // и его собственный трафик к серверу не должен возвращаться в туннель.
                // Иначе получится замкнутый круг
                builder.addDisallowedApplication(packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Не удалось исключить приложение из туннеля: ${e.javaClass.simpleName}")
            }
        }
        return builder
    }

    // Выключение VPN.
    // Порядок важен: сначала ядро, потом карта, потом состояние и уведомление.
    private fun stopTunnel() {
        // Остановка уже идёт: второй раз не запускаем
        if (stopJob?.isActive == true) return

        // Если ещё шёл запуск, отменяем его
        startJob?.cancel()
        startJob = null

        stopJob = serviceScope.launch {
            // Останавливаем ядро в фоновом потоке. Если запуск ещё не закончился,
            // метод stop подождёт его завершения (так устроен @Synchronized)
            withContext(Dispatchers.IO) { xrayCore.stop() }
            Log.i(TAG, "Остановка: ядро остановлено")

            closeTunnel()
            Log.i(TAG, "Остановка: карта закрыта")
            activeConfig = null
            statusHolder.update(ConnectionState.DISCONNECTED)

            // Убираем уведомление и завершаем сам сервис
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    // Закрывает нашу копию виртуальной карты. Когда закрыты все копии, туннель выключается.
    private fun closeTunnel() {
        try {
            tunInterface?.close()
        } catch (e: IOException) {
            Log.w(TAG, "Ошибка при закрытии туннеля: ${e.javaClass.simpleName}")
        }
        tunInterface = null
    }

    // Закрашивает секреты в тексте, чтобы они не попали в журнал.
    // Сообщение об ошибке иногда цитирует кусок конфигурации с UUID или ключом.
    private fun redact(text: String?): String {
        var result = text ?: return "нет сообщения"
        val config = activeConfig ?: return result

        listOf(config.uuid, config.publicKey, config.shortId)
            .filter { it.isNotBlank() }
            .forEach { secret -> result = result.replace(secret, "***") }
        return result
    }

    // Система вызывает, когда пользователь отозвал разрешение на VPN
    // или включил другое VPN-приложение, которое заняло наше место
    override fun onRevoke() {
        Log.i(TAG, "Разрешение на VPN отозвано")
        stopTunnel()
        super.onRevoke()
    }

    // Страховка: если сервис уничтожается неожиданно, всё равно всё закрываем
    override fun onDestroy() {
        serviceScope.cancel()
        xrayCore.stop()
        closeTunnel()
        statusHolder.update(ConnectionState.DISCONNECTED)
        Log.i(TAG, "Сервис уничтожен")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "FastikVPN"

        // Названия команд, которые понимает сервис
        const val ACTION_START = "com.khti.fastikvpn.action.START"
        const val ACTION_STOP = "com.khti.fastikvpn.action.STOP"

        private const val NOTIFICATION_ID = 1001

        // Параметры виртуальной карты
        private const val TUN_MTU = 1500
        private const val TUN_ADDRESS_V4 = "172.19.0.1"
        private const val TUN_PREFIX_V4 = 30
        private const val TUN_ADDRESS_V6 = "fdfe:dcba:9876::1"
        private const val TUN_PREFIX_V6 = 126
        private const val DNS_PRIMARY = "1.1.1.1"
        private const val DNS_SECONDARY = "8.8.8.8"

        // Исключать ли наше приложение из туннеля
        private const val EXCLUDE_SELF = true

        // Готовая команда "включить"
        fun startIntent(context: Context): Intent =
            Intent(context, FastikVpnService::class.java).setAction(ACTION_START)

        // Готовая команда "выключить"
        fun stopIntent(context: Context): Intent =
            Intent(context, FastikVpnService::class.java).setAction(ACTION_STOP)
    }
}