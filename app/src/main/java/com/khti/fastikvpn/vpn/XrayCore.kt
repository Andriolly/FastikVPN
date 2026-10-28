package com.khti.fastikvpn.vpn

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import javax.inject.Inject

// Обёртка над ядром Xray. Все обращения к библиотеке libv2ray собраны в этом классе.
// Остальной код приложения про библиотеку ничего не знает и просто вызывает start и stop.
// Если библиотеку когда-нибудь заменят, править придётся только этот файл.
class XrayCore @Inject constructor(
    // Контекст всего приложения, нужен для пути к папке с файлами
    @ApplicationContext private val context: Context
) {

    // Контроллер запущенного ядра. Пока он не null, ядро работает
    private var controller: CoreController? = null

    // Ядро умеет сообщать нам о своих событиях. Для этого мы даём ему объект с тремя методами.
    // Типы Long здесь не случайны: в языке Go это тип int, а gomobile превращает его в Long.
    // Каждый метод обязан вернуть число, ноль означает "всё хорошо".
    private val callbackHandler = object : CoreCallbackHandler {

        // Ядро запустилось
        override fun startup(): Long {
            Log.i(TAG, "Ядро Xray: запущено")
            return 0
        }

        // Ядро остановилось
        override fun shutdown(): Long {
            Log.i(TAG, "Ядро Xray: остановлено")
            return 0
        }

        // Ядро прислало сообщение о состоянии. code - код, message - текст.
        // Тип String? с вопросом: библиотека написана не на Kotlin и может передать null
        override fun onEmitStatus(code: Long, message: String?): Long {
            Log.i(TAG, "Ядро Xray: $message")
            return 0
        }
    }

    // Запускает ядро.
    // configJson - настройки в формате JSON (их собирает XrayConfigBuilder),
    // tunFd - номер открытого виртуального интерфейса, из которого ядро будет читать пакеты.
    //
    // @Synchronized: пока один поток выполняет этот метод, остальные ждут.
    // Благодаря этому остановка не вклинится в середину запуска.
    @Synchronized
    fun start(configJson: String, tunFd: Int) {
        // Ядро уже запущено: повторный запуск не нужен
        if (controller != null) return

        // Готовим окружение ядра: папка для файлов и ключ шифрования XUDP (пока не нужен).
        // Вызов безопасно повторять
        Libv2ray.initCoreEnv(context.filesDir.absolutePath, "")

        // Создаём контроллер и передаём ему наш обработчик событий
        val newController = Libv2ray.newCoreController(callbackHandler)

        // Запуск. При ошибке в конфигурации бросит исключение, и мы в controller ничего не запишем
        newController.startLoop(configJson, tunFd)

        controller = newController
    }

    // Останавливает ядро. Если оно не запущено, ничего не делает
    @Synchronized
    fun stop() {
        val current = controller ?: return
        // Сначала забываем контроллер, чтобы повторная остановка не сработала дважды
        controller = null
        try {
            current.stopLoop()
        } catch (e: Exception) {
            Log.w(TAG, "Ошибка при остановке ядра: ${e.javaClass.simpleName}")
        }
    }

    private companion object {
        const val TAG = "FastikVPN"
    }
}