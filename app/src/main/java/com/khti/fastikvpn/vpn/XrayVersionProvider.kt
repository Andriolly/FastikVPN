package com.khti.fastikvpn.vpn

import libv2ray.Libv2ray
import javax.inject.Inject

// Единственное место, где мы обращаемся к библиотеке Xray на этом этапе.
// Если библиотеку заменят или переименуют, правки будут только здесь.
// ПРОВЕРЬТЕ имя класса Libv2ray в автодополнении студии: gomobile формирует его
// из названия Go-пакета и мог выбрать немного другое имя.
class XrayVersionProvider @Inject constructor() {

    fun getVersion(): String = try {
        // Вызов уходит в машинный код библиотеки и возвращает строку с версиями
        Libv2ray.checkVersionX()
    } catch (e: Throwable) {
        // Ловим даже UnsatisfiedLinkError (библиотека не загрузилась на этом процессоре),
        // чтобы приложение не падало при запуске
        "недоступно: ${e.message}"
    }
}