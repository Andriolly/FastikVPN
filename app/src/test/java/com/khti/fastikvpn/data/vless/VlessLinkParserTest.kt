package com.khti.fastikvpn.data.vless

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Проверяем парсер на корректных и некорректных ссылках.
class VlessLinkParserTest {

    private val parser = VlessLinkParser()

    // Вымышленная ссылка, повторяющая устройство настоящей
    private val validLink =
        "vless://11111111-2222-3333-4444-555555555555@203.0.113.10:443" +
            "?encryption=none&flow=xtls-rprx-vision&fp=firefox&pbk=TEST_PUBLIC_KEY" +
            "&security=reality&sid=abcd1234&sni=example.com&spx=%2Ftest&type=tcp" +
            "&support-x25519mlkem768=true#Test%20Server"

    @Test
    fun `корректная ссылка разбирается полностью`() {
        val config = parser.parse(validLink).getOrThrow()

        assertEquals("11111111-2222-3333-4444-555555555555", config.uuid)
        assertEquals("203.0.113.10", config.address)
        assertEquals(443, config.port)
        assertEquals("xtls-rprx-vision", config.flow)
        assertEquals("tcp", config.network)
        assertEquals("reality", config.security)
        assertEquals("example.com", config.sni)
        assertEquals("firefox", config.fingerprint)
        assertEquals("TEST_PUBLIC_KEY", config.publicKey)
        assertEquals("abcd1234", config.shortId)
        // %2F превратился в /
        assertEquals("/test", config.spiderX)
        // %20 превратился в пробел
        assertEquals("Test Server", config.name)
    }

    @Test
    fun `неверная схема даёт ошибку`() {
        assertTrue(parser.parse("vmess://abc").isFailure)
    }

    @Test
    fun `неверный UUID даёт ошибку`() {
        val link = validLink.replace("11111111-2222-3333-4444-555555555555", "not-a-uuid")
        assertTrue(parser.parse(link).isFailure)
    }

    @Test
    fun `reality без публичного ключа даёт ошибку`() {
        val link = validLink.replace("pbk=TEST_PUBLIC_KEY&", "")
        assertTrue(parser.parse(link).isFailure)
    }

    @Test
    fun `порт вне диапазона даёт ошибку`() {
        assertTrue(parser.parse(validLink.replace(":443", ":99999")).isFailure)
    }

    @Test
    fun `IPv6 адрес в скобках разбирается`() {
        val link = validLink.replace("203.0.113.10", "[2001:db8::1]")
        assertEquals("2001:db8::1", parser.parse(link).getOrThrow().address)
    }

    @Test
    fun `пробелы по краям игнорируются`() {
        assertTrue(parser.parse("  $validLink  \n").isSuccess)
    }
}