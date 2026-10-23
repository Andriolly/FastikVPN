package com.khti.fastikvpn.data.vless

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Проверяем, что собранный JSON содержит нужные блоки и значения.
class XrayConfigBuilderTest {

    private val parser = VlessLinkParser()
    private val builder = XrayConfigBuilder()

    private val link =
        "vless://11111111-2222-3333-4444-555555555555@203.0.113.10:443" +
            "?flow=xtls-rprx-vision&fp=firefox&pbk=TEST_PUBLIC_KEY&security=reality" +
            "&sid=abcd1234&sni=example.com&spx=%2Ftest&type=tcp#Test"

    @Test
    fun `в конфигурации есть вход tun и выход vless`() {
        val json = JSONObject(builder.build(parser.parse(link).getOrThrow()))

        val inbound = json.getJSONArray("inbounds").getJSONObject(0)
        assertEquals("tun", inbound.getString("protocol"))

        val outbound = json.getJSONArray("outbounds").getJSONObject(0)
        assertEquals("vless", outbound.getString("protocol"))
        assertEquals("proxy", outbound.getString("tag"))
    }

    @Test
    fun `параметры reality попадают в конфигурацию`() {
        val json = JSONObject(builder.build(parser.parse(link).getOrThrow()))
        val stream = json.getJSONArray("outbounds").getJSONObject(0).getJSONObject("streamSettings")
        val reality = stream.getJSONObject("realitySettings")

        assertEquals("reality", stream.getString("security"))
        assertEquals("example.com", reality.getString("serverName"))
        assertEquals("firefox", reality.getString("fingerprint"))
        assertEquals("TEST_PUBLIC_KEY", reality.getString("publicKey"))
        assertEquals("abcd1234", reality.getString("shortId"))
        assertEquals("/test", reality.getString("spiderX"))
    }

    @Test
    fun `flow передаётся пользователю`() {
        val json = JSONObject(builder.build(parser.parse(link).getOrThrow()))
        val user = json.getJSONArray("outbounds").getJSONObject(0)
            .getJSONObject("settings").getJSONArray("vnext").getJSONObject(0)
            .getJSONArray("users").getJSONObject(0)

        assertEquals("xtls-rprx-vision", user.getString("flow"))
        assertEquals("none", user.getString("encryption"))
    }

    @Test
    fun `без flow поле не добавляется`() {
        val noFlow = link.replace("flow=xtls-rprx-vision&", "")
        val json = JSONObject(builder.build(parser.parse(noFlow).getOrThrow()))
        val user = json.getJSONArray("outbounds").getJSONObject(0)
            .getJSONObject("settings").getJSONArray("vnext").getJSONObject(0)
            .getJSONArray("users").getJSONObject(0)

        assertFalse(user.has("flow"))
    }

    @Test
    fun `неподдерживаемый транспорт даёт ошибку`() {
        val ws = link.replace("type=tcp", "type=ws")
        val config = parser.parse(ws).getOrThrow()
        assertTrue(runCatching { builder.build(config) }.isFailure)
    }
}