package com.khti.fastikvpn.vpn

import com.khti.fastikvpn.core.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Единое табло состояния VPN для всего приложения.
// Сервис пишет сюда, что происходит, а экраны читают и перерисовываются.
// @Singleton: Hilt создаёт ровно один такой объект. Это принципиально:
// если бы у сервиса и у экрана были разные табло, они бы показывали разное.
@Singleton
class VpnStatusHolder @Inject constructor() {

    // Изменяемая часть, видна только внутри класса
    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)

    // Версия для чтения: на неё подписываются экраны
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    // Меняет состояние. Вызывает сервис, когда туннель включился или выключился
    fun update(newState: ConnectionState) {
        _state.value = newState
    }
}