package com.khti.fastikvpn.vpn

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import javax.inject.Inject
import javax.inject.Singleton

// Обёртка над ядром Xray. Все обращения к библиотеке libv2ray собраны в этом классе.
// Остальной код приложения про библиотеку ничего не знает.
//
// @Singleton: теперь у приложения ровно одно ядро на всех. Раньше ядро жило только
// внутри сервиса, а пинг запрашивает экран. Чтобы они обращались к одному и тому же ядру,
// объект должен быть общим.
@Singleton
class XrayCore @Inject constructor(
    // Контекст всего приложения, нужен для пути к папке с файлами
    @ApplicationContext private val context: Context
) {

    // Контроллер запущенного ядра. Пока он не null, ядро работает
    private var controller: CoreController? = null

    // Объект, через который ядро сообщает нам о своих событиях.
    // Типы Long здесь не случайны: в языке Go это тип int, а gomobile превращает его в Long.
    private val callbackHandler = object : CoreCallbackHandler {

        override fun startup(): Long {
            Log.i(TAG, "Ядро Xray: запущено")
            return 0
        }

        override fun shutdown(): Long {
            Log.i(TAG, "Ядро Xray: остановлено")
            return 0
        }

        override fun onEmitStatus(code: Long, message: String?): Long {
            Log.i(TAG, "Ядро Xray: $message")
            return 0
        }
    }

    // Запускает ядро.
    // @Synchronized: пока один поток выполняет этот метод, остальные ждут.
    @Synchronized
    fun start(configJson: String, tunFd: Int) {
        if (controller != null) return

        Libv2ray.initCoreEnv(context.filesDir.absolutePath, "")

        val newController = Libv2ray.newCoreController(callbackHandler)
        newController.startLoop(configJson, tunFd)

        controller = newController
    }

    // Останавливает ядро. Если оно не запущено, ничего не делает
    @Synchronized
    fun stop() {
        val current = controller ?: return
        controller = null
        try {
            current.stopLoop()
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка при остановке ядра: ${e.javaClass.simpleName}")
        }
    }

    // Измеряет задержку: ядро отправляет запрос на указанный адрес ЧЕРЕЗ СЕРВЕР
    // и возвращает, сколько миллисекунд прошло до ответа.
    // Бросает исключение, если ядро не запущено или связи нет (ожидание ограничено 12 секундами).
    //
    // Метод намеренно БЕЗ @Synchronized: он может ждать до 12 секунд, и за это время
    // нельзя блокировать остановку ядра. Поэтому синхронизирована только короткая
    // операция "взять ссылку на контроллер", а само ожидание идёт без блокировки.
    fun measureDelay(url: String): Long {
        val current = synchronized(this) { controller }
            ?: throw IllegalStateException("Ядро не запущено")
        return current.measureDelay(url)
    }

    // Спрашивает у ядра счётчики трафика. Возвращает строку, которую разбирает TrafficStatsParser.
    // ВАЖНО: ядро обнуляет счётчики при каждом чтении, поэтому читать их должен только один
    // "читатель" (наш сервис). Если ядро не запущено, возвращаем пустую строку
    fun queryTraffic(): String {
        val current = synchronized(this) { controller } ?: return ""
        return current.queryAllOutboundTrafficStats()
    }

    private companion object {
        const val TAG = "FastikVPN"
    }
}