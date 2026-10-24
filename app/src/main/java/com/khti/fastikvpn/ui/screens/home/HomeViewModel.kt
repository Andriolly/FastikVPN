package com.khti.fastikvpn.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.domain.repository.IpRepository
import com.khti.fastikvpn.vpn.VpnController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

// ViewModel главного экрана.
// Теперь подключение НАСТОЯЩЕЕ: мы просим VpnController включить сервис
// и следим за его состоянием. А вот пинг пока ИМИТАЦИЯ.
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ipRepository: IpRepository,
    // Пульт управления VPN. Hilt подставляет его сам
    private val vpnController: VpnController
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var ipJob: Job? = null
    private var pingJob: Job? = null

    init {
        refreshIp()

        // Подписываемся на табло состояния VPN. collect работает всё время жизни ViewModel:
        // при любом изменении вызывается onVpnStateChanged
        viewModelScope.launch {
            vpnController.state.collect { newState -> onVpnStateChanged(newState) }
        }
    }

    // Нажали на большую кнопку (разрешения к этому моменту уже получены экраном)
    fun onConnectClick() {
        // Без входа подключаться нельзя. Защита стоит и здесь, а не только в экране
        if (!_uiState.value.isAuthorized) return

        when (_uiState.value.connectionState) {
            ConnectionState.DISCONNECTED -> vpnController.connect()
            ConnectionState.CONNECTED -> vpnController.disconnect()
            // Пока идёт подключение, повторные нажатия игнорируем
            ConnectionState.CONNECTING -> Unit
        }
    }

    // Нажали на карточку IP: показываем или прячем пинг
    fun onIpCardClick() {
        _uiState.update { it.copy(isPingVisible = !it.isPingVisible) }
    }

    // Нажали на значок обновления пинга
    fun onPingRefreshClick() {
        val state = _uiState.value
        if (state.connectionState == ConnectionState.CONNECTED && !state.isPingLoading) {
            measurePing()
        }
    }

    // Реакция на изменение состояния VPN. Состояние приходит из сервиса, а не придумывается здесь.
    // Благодаря этому, если VPN выключили кнопкой в уведомлении, кнопка на экране погаснет сама.
    private fun onVpnStateChanged(newState: ConnectionState) {
        val oldState = _uiState.value.connectionState
        _uiState.update { it.copy(connectionState = newState) }

        when {
            // VPN только что включился
            newState == ConnectionState.CONNECTED && oldState != ConnectionState.CONNECTED -> {
                refreshIp()
                measurePing()
            }
            // VPN только что выключился
            newState == ConnectionState.DISCONNECTED && oldState != ConnectionState.DISCONNECTED -> {
                pingJob?.cancel()
                _uiState.update { it.copy(pingMs = null, isPingLoading = false) }
                refreshIp()
            }
        }
    }

    // Узнаём внешний IP из интернета
    private fun refreshIp() {
        ipJob?.cancel()
        _uiState.update { it.copy(isIpLoading = true) }

        ipJob = viewModelScope.launch {
            val ip = ipRepository.getPublicIp()
            _uiState.update { it.copy(ipAddress = ip, isIpLoading = false) }
        }
    }

    // ИМИТАЦИЯ измерения пинга: пауза случайной длины и случайное число.
    // Настоящее измерение появится на этапе D.
    private fun measurePing() {
        pingJob?.cancel()
        _uiState.update { it.copy(isPingLoading = true) }

        pingJob = viewModelScope.launch {
            delay(Random.nextLong(PING_MIN_DELAY_MS, PING_MAX_DELAY_MS))
            val ping = Random.nextInt(PING_MIN_MS, PING_MAX_MS + 1)
            _uiState.update { it.copy(pingMs = ping, isPingLoading = false) }
        }
    }

    private companion object {
        const val PING_MIN_MS = 30
        const val PING_MAX_MS = 150
        const val PING_MIN_DELAY_MS = 400L
        const val PING_MAX_DELAY_MS = 1200L
    }
}