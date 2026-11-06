package com.khti.fastikvpn.core.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

// Проверяем форматирование размеров, скорости и времени.
// Locale.US задаём явно, чтобы результат не зависел от настроек компьютера.
class FormattersTest {

    private val us = Locale.US

    @Test
    fun `байты остаются целыми`() {
        assertEquals("512 Б", Formatters.formatBytes(512, us))
    }

    @Test
    fun `килобайты с одним знаком`() {
        assertEquals("1.5 КБ", Formatters.formatBytes(1536, us))
    }

    @Test
    fun `мегабайты`() {
        assertEquals("2.0 МБ", Formatters.formatBytes(2L * 1024 * 1024, us))
    }

    @Test
    fun `отрицательное значение считается нулём`() {
        assertEquals("0 Б", Formatters.formatBytes(-5, us))
    }

    @Test
    fun `скорость с добавкой в секунду`() {
        assertEquals("1.0 КБ/с", Formatters.formatSpeed(1024, us))
    }

    @Test
    fun `длительность меньше часа`() {
        assertEquals("01:15", Formatters.formatDuration(75))
    }

    @Test
    fun `длительность больше часа`() {
        assertEquals("1:02:05", Formatters.formatDuration(3725))
    }

    @Test
    fun `ноль секунд`() {
        assertEquals("00:00", Formatters.formatDuration(0))
    }
}