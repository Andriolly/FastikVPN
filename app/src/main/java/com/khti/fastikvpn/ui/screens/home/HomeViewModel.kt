package com.khti.fastikvpn.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.core.model.VpnError
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

// ViewModel главного экрана.
// Подключение, пинг и адрес сервера теперь НАСТОЯЩИЕ. Раньше пинг был случайным числом.
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ipRepository: IpRepository,
    // Пульт управления VPN. Hilt подставляет его сам
    private val vpnController: VpnController
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Ссылки на запущенные дела, чтобы старое дело можно было отменить при запуске нового
    private var ipJob: Job? = null
    private var pingJob: Job? = null
    private var errorJob: Job? = null

    init {
        refreshIp()

        // Следим за состоянием VPN
        viewModelScope.launch {
            vpnController.state.collect { newState -> onVpnStateChanged(newState) }
        }

        // Следим за ошибками. Когда на табло появилась ошибка, показываем её
        viewModelScope.launch {
            vpnController.error.collect { error ->
                if (error != null) showError(error)
            }
        }

        // Следим за статистикой сессии. Данные собирает сервис, мы только показываем
        viewModelScope.launch {
            vpnController.sessionStats.collect { stats ->
                _uiState.update { it.copy(session = stats) }
            }
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

    // Определяем, какой адрес показать на карточке.
    // VPN включён: показываем адрес сервера. Своё приложение мы исключили из туннеля,
    // поэтому запрос "какой у меня IP" из приложения показал бы адрес телефона, а не сервера.
    // VPN выключен: спрашиваем настоящий IP телефона в интернете.
    private fun refreshIp() {
        ipJob?.cancel()

        val serverAddress = vpnController.serverAddress.value
        if (_uiState.value.connectionState == ConnectionState.CONNECTED && serverAddress != null) {
            _uiState.update { it.copy(ipAddress = serverAddress, isIpLoading = false) }
            return
        }

        _uiState.update { it.copy(isIpLoading = true) }
        ipJob = viewModelScope.launch {
            val ip = ipRepository.getPublicIp()
            _uiState.update { it.copy(ipAddress = ip, isIpLoading = false) }
        }
    }

    // НАСТОЯЩИЙ замер пинга через ядро.
    private fun measurePing() {
        pingJob?.cancel()
        _uiState.update { it.copy(isPingLoading = true) }

        pingJob = viewModelScope.launch {
            // Result хранит либо число, либо ошибку. Обрабатываем оба случая
            vpnController.measurePing()
                .onSuccess { ms ->
                    _uiState.update { it.copy(pingMs = ms, isPingLoading = false) }
                }
                .onFailure {
                    // Замер не удался: значит, сервер не отвечает
                    _uiState.update { it.copy(pingMs = null, isPingLoading = false) }
                    showError(VpnError.SERVER_UNREACHABLE)
                }
        }
    }

    // Показывает ошибку на экране и через несколько секунд убирает её
    private fun showError(error: VpnError) {
        errorJob?.cancel()
        _uiState.update { it.copy(error = error) }

        errorJob = viewModelScope.launch {
            delay(ERROR_SHOW_MS)
            _uiState.update { it.copy(error = null) }
            // Стираем ошибку и на общем табло, чтобы она не показалась снова
            vpnController.clearError()
        }
    }

    private companion object {
        // Сколько секунд сообщение об ошибке висит на экране
        const val ERROR_SHOW_MS = 6_000L
    }
}