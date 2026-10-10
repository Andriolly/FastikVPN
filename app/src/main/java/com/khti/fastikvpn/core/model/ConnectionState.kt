package com.khti.fastikvpn.core.model

// Состояния подключения VPN. Лежит в core/model, потому что это понятие нужно
// не только главному экрану: потом его будут использовать VPN-сервис и уведомление.
enum class ConnectionState {
    // VPN выключен
    DISCONNECTED,

    // Подключение идёт, но ещё не завершилось
    CONNECTING,

    // VPN работает
    CONNECTED
}