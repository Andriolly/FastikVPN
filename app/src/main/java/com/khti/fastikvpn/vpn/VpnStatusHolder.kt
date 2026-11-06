package com.khti.fastikvpn.vpn

import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.core.model.VpnError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.khti.fastikvpn.core.model.SessionStats

// Единое табло состояния VPN для всего приложения.
// Сервис пишет сюда, что происходит, а экраны читают и перерисовываются.
// @Singleton: Hilt создаёт ровно один такой объект. Это принципиально:
// если бы у сервиса и у экрана были разные табло, они бы показывали разное.
@Singleton
class VpnStatusHolder @Inject constructor() {

    // Изменяемые части, видны только внутри класса
    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    private val _serverAddress = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<VpnError?>(null)

    private val _sessionStats = MutableStateFlow(SessionStats.EMPTY)

    // Версии для чтения: на них подписываются экраны

    // Состояние подключения
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    // Адрес сервера, к которому мы подключены. null, пока VPN выключен
    val serverAddress: StateFlow<String?> = _serverAddress.asStateFlow()

    // Последняя ошибка. null, если ошибок нет
    val error: StateFlow<VpnError?> = _error.asStateFlow()

    // Статистика текущей сессии: объём, скорость, время
    val sessionStats: StateFlow<SessionStats> = _sessionStats.asStateFlow()

    // Меняет состояние. Вызывает сервис, когда туннель включился или выключился
    fun update(newState: ConnectionState) {
        _state.value = newState
    }

    // Запоминает адрес сервера (или стирает его, если передать null)
    fun setServerAddress(address: String?) {
        _serverAddress.value = address
    }

    // Сообщает об ошибке
    fun reportError(newError: VpnError) {
        _error.value = newError
    }

    // Стирает ошибку: перед новой попыткой подключения или после показа сообщения
    fun clearError() {
        _error.value = null
    }

    // Публикует свежую статистику. Вызывает сервис раз в секунду
    fun updateSession(stats: SessionStats) {
        _sessionStats.value = stats
    }

    // Обнуляет статистику, когда VPN выключился
    fun resetSession() {
        _sessionStats.value = SessionStats.EMPTY
    }
}