package com.khti.fastikvpn.vpn

import org.junit.Assert.assertEquals
import org.junit.Test

// Проверяем разбор строки со счётчиками ядра.
class TrafficStatsParserTest {

    @Test
    fun `разбирает отдачу и загрузку основного выхода`() {
        val delta = TrafficStatsParser.parse("proxy,uplink,1200;proxy,downlink,54000;")
        assertEquals(1200L, delta.uploadBytes)
        assertEquals(54000L, delta.downloadBytes)
    }

    @Test
    fun `пустая строка даёт нули`() {
        val delta = TrafficStatsParser.parse("")
        assertEquals(0L, delta.uploadBytes)
        assertEquals(0L, delta.downloadBytes)
    }

    @Test
    fun `чужие выходы игнорируются`() {
        val delta = TrafficStatsParser.parse("direct,uplink,999;proxy,downlink,10;")
        assertEquals(0L, delta.uploadBytes)
        assertEquals(10L, delta.downloadBytes)
    }

    @Test
    fun `неверные записи пропускаются без ошибки`() {
        val delta = TrafficStatsParser.parse("мусор;proxy,uplink,abc;proxy,uplink,5;proxy,x,7;")
        assertEquals(5L, delta.uploadBytes)
        assertEquals(0L, delta.downloadBytes)
    }

    @Test
    fun `одинаковые записи суммируются`() {
        val delta = TrafficStatsParser.parse("proxy,uplink,10;proxy,uplink,15;")
        assertEquals(25L, delta.uploadBytes)
    }
}