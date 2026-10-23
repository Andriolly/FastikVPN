package com.khti.fastikvpn.domain.model

// Все параметры подключения к серверу в одном объекте.
// Строка vless:// неудобна: её нужно разбирать каждый раз. Поэтому разбираем один раз
// и дальше передаём этот объект. data class сам создаёт copy(), equals() и hashCode().
data class VlessConfig(
    // Подпись для человека (часть ссылки после знака #)
    val name: String,

    // IP-адрес или доменное имя сервера
    val address: String,

    // Порт сервера, у нас 443
    val port: Int,

    // Личный идентификатор пользователя на сервере. Это секрет!
    val uuid: String,

    // Режим ускорения, например xtls-rprx-vision. Может быть пустым
    val flow: String,

    // Транспорт: tcp, ws, grpc. Пока поддерживаем только tcp
    val network: String,

    // Способ защиты: reality, tls или none
    val security: String,

    // Имя сайта, под который маскируется соединение (Server Name Indication)
    val sni: String,

    // Отпечаток браузера, под который подделывается шифрование. У нас firefox
    val fingerprint: String,

    // Публичный ключ сервера для Reality
    val publicKey: String,

    // Короткий идентификатор Reality
    val shortId: String,

    // Путь SpiderX для Reality, может быть пустым
    val spiderX: String
) {
    // Переопределяем вывод в строку. Стандартный toString у data class печатает ВСЕ поля,
    // и UUID с ключом могли бы случайно попасть в лог. Поэтому секреты прячем.
    override fun toString(): String =
        "VlessConfig(name=$name, address=$address:$port, network=$network, " +
                "security=$security, flow=${flow.ifBlank { "нет" }}, uuid=***, publicKey=***)"
}