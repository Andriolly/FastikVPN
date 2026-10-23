package com.khti.fastikvpn.data.vless

import com.khti.fastikvpn.domain.model.VlessConfig
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

// Собирает JSON-конфигурацию для ядра Xray из нашего объекта VlessConfig.
// Это "инструкция для курьера": откуда брать пакеты, куда их отправлять и как защищать.
// ВАЖНО: структуру блока tun окончательно подтвердим на этапе C на реальном телефоне.
class XrayConfigBuilder @Inject constructor() {

    fun build(config: VlessConfig): String {
        // Пока умеем только обычный TCP. Остальные транспорты добавим позже.
        require(config.network == "tcp") {
            "Транспорт '${config.network}' пока не поддерживается"
        }

        val root = JSONObject()

        // Уровень журнала: только предупреждения и ошибки
        root.put("log", JSONObject().put("loglevel", "warning"))

        // Счётчики трафика. Нужны для статистики на экране (этап D)
        root.put("stats", JSONObject())
        root.put(
            "policy",
            JSONObject().put(
                "system",
                JSONObject()
                    .put("statsOutboundUplink", true)
                    .put("statsOutboundDownlink", true)
            )
        )

        // DNS-серверы, через которые Xray узнаёт адреса сайтов
        root.put(
            "dns",
            JSONObject().put("servers", JSONArray().put("1.1.1.1").put("8.8.8.8"))
        )

        // ВХОД: виртуальный интерфейс. Порт не нужен, вход не слушает порт,
        // а читает пакеты из интерфейса, который создал Android
        root.put("inbounds", JSONArray().put(buildTunInbound()))

        // ВЫХОДЫ: основной на наш сервер, запасные "напрямую" и "в никуда"
        root.put(
            "outbounds",
            JSONArray()
                .put(buildVlessOutbound(config))
                .put(JSONObject().put("tag", "direct").put("protocol", "freedom"))
                .put(JSONObject().put("tag", "block").put("protocol", "blackhole"))
        )

        return root.toString()
    }

    // Блок входа TUN
    private fun buildTunInbound(): JSONObject = JSONObject()
        .put("tag", "tun-in")
        .put("port", 0)
        .put("protocol", "tun")
        .put(
            "settings",
            JSONObject()
                .put("name", "xray0")
                // MTU: максимальный размер пакета в интерфейсе
                .put("mtu", 1500)
        )
        .put(
            "sniffing",
            JSONObject()
                .put("enabled", true)
                // Определяем имя сайта по началу соединения
                .put("destOverride", JSONArray().put("http").put("tls").put("quic"))
        )

    // Блок выхода: протокол VLESS и параметры сервера
    private fun buildVlessOutbound(config: VlessConfig): JSONObject {
        // Данные пользователя: UUID, режим шифрования none (так требует VLESS) и flow
        val user = JSONObject()
            .put("id", config.uuid)
            .put("encryption", "none")
        if (config.flow.isNotBlank()) {
            user.put("flow", config.flow)
        }

        val server = JSONObject()
            .put("address", config.address)
            .put("port", config.port)
            .put("users", JSONArray().put(user))

        return JSONObject()
            .put("tag", "proxy")
            .put("protocol", "vless")
            .put("settings", JSONObject().put("vnext", JSONArray().put(server)))
            .put("streamSettings", buildStreamSettings(config))
    }

    // Настройки транспорта и защиты
    private fun buildStreamSettings(config: VlessConfig): JSONObject {
        val stream = JSONObject()
            .put("network", config.network)
            .put("security", config.security)

        when (config.security) {
            "reality" -> {
                val reality = JSONObject()
                    .put("serverName", config.sni)
                    .put("fingerprint", config.fingerprint)
                    .put("publicKey", config.publicKey)
                    .put("shortId", config.shortId)
                // SpiderX добавляем, только если он задан
                if (config.spiderX.isNotBlank()) {
                    reality.put("spiderX", config.spiderX)
                }
                stream.put("realitySettings", reality)
            }
            "tls" -> {
                stream.put(
                    "tlsSettings",
                    JSONObject()
                        .put("serverName", config.sni)
                        .put("fingerprint", config.fingerprint)
                )
            }
            // "none": дополнительных настроек не нужно
        }
        return stream
    }
}