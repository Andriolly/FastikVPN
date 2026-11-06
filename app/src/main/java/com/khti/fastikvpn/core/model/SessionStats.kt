package com.khti.fastikvpn.core.model

// Статистика текущего VPN-подключения. Все поля - числа, форматирование делает экран.
// По умолчанию всё равно нулю: так выглядит "сессии нет".
data class SessionStats(
    // Сколько всего байт отправлено с телефона за сессию (uplink, отдача)
    val uploadBytes: Long = 0,

    // Сколько всего байт получено телефоном за сессию (downlink, загрузка)
    val downloadBytes: Long = 0,

    // Текущая скорость отдачи, байт в секунду
    val uploadSpeed: Long = 0,

    // Текущая скорость загрузки, байт в секунду
    val downloadSpeed: Long = 0,

    // Сколько секунд длится сессия
    val elapsedSeconds: Long = 0
) {
    companion object {
        // Пустая статистика: сессии нет или она только началась
        val EMPTY = SessionStats()
    }
}