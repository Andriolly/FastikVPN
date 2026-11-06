package com.khti.fastikvpn.ui.screens.home

import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.core.model.SessionStats
import com.khti.fastikvpn.core.model.VpnError

// Всё, что нужно главному экрану, чтобы нарисоваться, собрано в одном объекте.
// Экран ничего не хранит сам, он просто показывает этот объект.
// data class - класс для хранения данных, Kotlin сам создаёт для него функцию copy.
data class HomeUiState(
    // Текущее состояние подключения
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,

    // IP-адрес. null значит, что адреса пока нет: он ещё грузится или не получилось определить
    val ipAddress: String? = null,

    // Идёт ли сейчас определение IP. В начале true, потому что запрос стартует сразу
    val isIpLoading: Boolean = true,

    // Пинг в миллисекундах. null, пока VPN выключен или измерение не закончилось
    val pingMs: Int? = null,

    // Идёт ли сейчас измерение пинга (нужно для анимации и блокировки кнопки обновления)
    val isPingLoading: Boolean = false,

    // Показана ли плашка с пингом справа от карточки IP
    val isPingVisible: Boolean = false,

    // Вошёл ли пользователь. Пока всегда true, позже подставим настоящую проверку.
    // Без входа кнопка подключения будет серой и неактивной
    val isAuthorized: Boolean = true,

    // Текущая ошибка для показа пользователю. null, если ошибок нет
    val error: VpnError? = null,

    // Статистика текущей сессии: объём, скорость, время
    val session: SessionStats = SessionStats.EMPTY
)