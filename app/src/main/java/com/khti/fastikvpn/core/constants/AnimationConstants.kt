package com.khti.fastikvpn.core.constants

// Длительности анимаций в миллисекундах.
// Собраны в одном месте, чтобы скорость анимаций подбирать, не листая весь проект.
object AnimationConstants {
    const val THEME_SWITCH_MS = 300

    // Плавное загорание и затухание кнопки подключения
    const val CONNECT_BUTTON_MS = 500

    // Один "вдох" кнопки (от самого маленького до самого большого размера).
    // Было 1800, но выглядело слишком быстро, поэтому замедлили
    const val PULSE_MS = 3200

    // Один полный оборот значка обновления пинга
    const val SPIN_MS = 900

    const val FADE_MS = 250
}