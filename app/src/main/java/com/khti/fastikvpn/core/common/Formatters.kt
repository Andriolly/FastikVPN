package com.khti.fastikvpn.core.common

import java.util.Locale

// Функции, которые превращают числа в читаемый текст.
// Это "чистые" функции: результат зависит только от аргументов, они ничего не читают
// из системы. Поэтому их легко проверять тестами.
object Formatters {

    // Единицы измерения размера, каждая следующая в 1024 раза больше предыдущей
    private val UNITS = arrayOf("Б", "КБ", "МБ", "ГБ", "ТБ")

    // Объём данных: 512 -> "512 Б", 1536 -> "1,5 КБ".
    // locale передаём параметром: на устройстве берётся язык телефона (запятая в русском),
    // а в тестах задаём явно, чтобы результат не зависел от компьютера
    fun formatBytes(bytes: Long, locale: Locale = Locale.getDefault()): String {
        // Отрицательных объёмов не бывает, на всякий случай считаем их нулём
        var value = bytes.coerceAtLeast(0).toDouble()
        var unitIndex = 0

        // Делим на 1024, пока число не станет меньше 1024 или единицы не закончатся
        while (value >= 1024 && unitIndex < UNITS.lastIndex) {
            value /= 1024
            unitIndex++
        }

        // Байты показываем целыми числами, остальное с одним знаком после запятой
        return if (unitIndex == 0) {
            "${value.toLong()} ${UNITS[0]}"
        } else {
            String.format(locale, "%.1f %s", value, UNITS[unitIndex])
        }
    }

    // Скорость: тот же формат с добавкой "/с"
    fun formatSpeed(bytesPerSecond: Long, locale: Locale = Locale.getDefault()): String =
        formatBytes(bytesPerSecond, locale) + "/с"

    // Длительность: 75 -> "01:15", 3725 -> "1:02:05".
    // Locale.ROOT: цифры и двоеточия не должны зависеть от языка телефона
    fun formatDuration(totalSeconds: Long): String {
        val seconds = totalSeconds.coerceAtLeast(0)
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val rest = seconds % 60

        return if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, rest)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", minutes, rest)
        }
    }
}