package com.khti.fastikvpn.data.vless

import com.khti.fastikvpn.domain.model.VlessConfig
import java.net.URLDecoder
import javax.inject.Inject

// Разбирает ссылку вида vless://UUID@адрес:порт?параметры#название в объект VlessConfig.
// Устроено без классов Android (Uri и т.п.), поэтому класс можно проверять обычными
// юнит-тестами на компьютере, без телефона.
class VlessLinkParser @Inject constructor() {

    // Публичная функция. Возвращает Result: либо готовый конфиг, либо ошибку.
    // runCatching перехватывает любое исключение внутри и превращает его в Result.failure,
    // поэтому кривая ссылка от пользователя не роняет приложение.
    fun parse(rawLink: String): Result<VlessConfig> = runCatching {
        parseOrThrow(rawLink.trim())
    }

    // Основная работа. Если что-то не так, бросаем исключение с понятным текстом.
    private fun parseOrThrow(link: String): VlessConfig {
        // require(условие) { текст } бросает ошибку, если условие ложно
        require(link.startsWith(SCHEME, ignoreCase = true)) {
            "Ссылка должна начинаться с vless://"
        }

        // Отрезаем схему, остаётся: UUID@адрес:порт?параметры#название
        val withoutScheme = link.substring(SCHEME.length)

        // Шаг 1. Название. Оно стоит после первого знака #
        val hashIndex = withoutScheme.indexOf('#')
        val name = if (hashIndex >= 0) decode(withoutScheme.substring(hashIndex + 1)) else ""
        val beforeHash = if (hashIndex >= 0) withoutScheme.substring(0, hashIndex) else withoutScheme

        // Шаг 2. Параметры. Они стоят после знака ?
        val queryIndex = beforeHash.indexOf('?')
        val authority = if (queryIndex >= 0) beforeHash.substring(0, queryIndex) else beforeHash
        val query = if (queryIndex >= 0) beforeHash.substring(queryIndex + 1) else ""

        // Шаг 3. UUID и адрес разделены знаком @. Ищем последний, на случай если
        // в UUID когда-нибудь окажется такой символ
        val atIndex = authority.lastIndexOf('@')
        require(atIndex > 0) { "В ссылке нет UUID или адреса сервера" }

        val uuid = authority.substring(0, atIndex)
        require(UUID_REGEX.matches(uuid)) { "UUID имеет неверный формат" }

        // Шаг 4. Адрес и порт
        val (host, port) = parseHostAndPort(authority.substring(atIndex + 1))

        // Шаг 5. Параметры превращаем в словарь "ключ -> значение"
        val params = parseQuery(query)

        // Шаг 6. Достаём нужные параметры, подставляя разумные значения по умолчанию
        val security = params["security"]?.lowercase() ?: "none"
        require(security in SUPPORTED_SECURITY) { "Защита '$security' не поддерживается" }

        val network = params["type"]?.lowercase() ?: "tcp"
        val publicKey = params["pbk"].orEmpty()

        // Для Reality без публичного ключа соединение невозможно, сообщаем сразу
        if (security == "reality") {
            require(publicKey.isNotBlank()) { "Для Reality нужен параметр pbk" }
        }

        return VlessConfig(
            name = name,
            address = host,
            port = port,
            uuid = uuid,
            flow = params["flow"].orEmpty(),
            network = network,
            security = security,
            // Если sni не указан, используем адрес сервера
            sni = params["sni"] ?: host,
            // Если отпечаток не указан, берём chrome
            fingerprint = params["fp"] ?: "chrome",
            publicKey = publicKey,
            shortId = params["sid"].orEmpty(),
            spiderX = params["spx"].orEmpty()
        )
    }

    // Делит "адрес:порт" на две части. Возвращает пару (адрес, порт).
    private fun parseHostAndPort(hostPort: String): Pair<String, Int> {
        val host: String
        val portText: String

        if (hostPort.startsWith("[")) {
            // IPv6-адрес записывается в квадратных скобках: [2001:db8::1]:443
            val end = hostPort.indexOf(']')
            require(end > 0) { "Неверная запись IPv6-адреса" }
            host = hostPort.substring(1, end)
            portText = hostPort.substring(end + 1).removePrefix(":")
        } else {
            // Обычный адрес. Берём последнее двоеточие: после него идёт порт
            val colon = hostPort.lastIndexOf(':')
            require(colon > 0) { "В ссылке нет порта" }
            host = hostPort.substring(0, colon)
            portText = hostPort.substring(colon + 1)
        }

        // toIntOrNull вернёт null вместо ошибки, если в строке не число
        val port = portText.toIntOrNull()
        require(port != null && port in 1..65535) { "Порт должен быть числом от 1 до 65535" }
        require(host.isNotBlank()) { "Пустой адрес сервера" }

        return host to port
    }

    // Превращает строку "a=1&b=2" в словарь. Ключи переводим в нижний регистр,
    // значения раскодируем из процентной записи (%2F -> /)
    private fun parseQuery(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()

        return query.split('&')
            .filter { it.isNotBlank() }
            .associate { pair ->
                val eq = pair.indexOf('=')
                if (eq < 0) {
                    pair.lowercase() to ""
                } else {
                    pair.substring(0, eq).lowercase() to decode(pair.substring(eq + 1))
                }
            }
    }

    // В адресах нельзя писать некоторые символы, поэтому они заменяются на % и код.
    // Эта функция делает обратное преобразование.
    private fun decode(value: String): String =
        URLDecoder.decode(value, Charsets.UTF_8.name())

    private companion object {
        const val SCHEME = "vless://"

        // Допустимые способы защиты
        val SUPPORTED_SECURITY = setOf("reality", "tls", "none")

        // UUID записывается как 8-4-4-4-12 шестнадцатеричных символов
        val UUID_REGEX = Regex(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        )
    }
}