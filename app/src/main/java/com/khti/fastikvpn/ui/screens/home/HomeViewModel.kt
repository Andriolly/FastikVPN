package com.khti.fastikvpn.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.domain.repository.IpRepository
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
// IP берётся из интернета по-настоящему, а вот подключение и пинг пока ИМИТАЦИЯ.
// Репозиторий IP Hilt передаёт нам сам через конструктор.
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ipRepository: IpRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Ссылки на запущенные дела. Они нужны, чтобы старое дело можно было отменить,
    // когда запускаем новое. Иначе результаты двух запросов перемешаются.
    private var ipJob: Job? = null
    private var pingJob: Job? = null

    // init выполняется сразу при создании ViewModel: узнаём IP при запуске экрана
    init {
        refreshIp()
    }

    // Нажали на большую кнопку
    fun onConnectClick() {
        when (_uiState.value.connectionState) {
            ConnectionState.DISCONNECTED -> connect()
            ConnectionState.CONNECTED -> disconnect()
            // Во время подключения нажатия игнорируем
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
        // Измерять имеет смысл, только когда VPN включён и измерение ещё не идёт
        if (state.connectionState == ConnectionState.CONNECTED && !state.isPingLoading) {
            measurePing()
        }
    }

    private fun connect() {
        _uiState.update { it.copy(connectionState = ConnectionState.CONNECTING) }

        viewModelScope.launch {
            // ИМИТАЦИЯ: делаем вид, что соединяемся с сервером
            delay(CONNECT_DELAY_MS)

            _uiState.update { it.copy(connectionState = ConnectionState.CONNECTED) }

            // После подключения заново узнаём IP и замеряем пинг
            refreshIp()
            measurePing()
        }
    }

    private fun disconnect() {
        // Измерение пинга больше не нужно, отменяем его
        pingJob?.cancel()

        _uiState.update {
            it.copy(
                connectionState = ConnectionState.DISCONNECTED,
                pingMs = null,
                isPingLoading = false
            )
        }

        // После отключения снова узнаём IP
        refreshIp()
    }

    // Узнаём внешний IP из интернета
    private fun refreshIp() {
        // Если предыдущий запрос ещё идёт, отменяем его
        ipJob?.cancel()
        _uiState.update { it.copy(isIpLoading = true) }

        ipJob = viewModelScope.launch {
            val ip = ipRepository.getPublicIp()
            // Если ip == null, на экране появится подпись "не удалось определить"
            _uiState.update { it.copy(ipAddress = ip, isIpLoading = false) }
        }
    }

    // ИМИТАЦИЯ измерения пинга: пауза случайной длины и случайное число.
    // Настоящее измерение тоже не мгновенное: пакет уходит, потом приходит ответ.
    private fun measurePing() {
        pingJob?.cancel()
        _uiState.update { it.copy(isPingLoading = true) }

        pingJob = viewModelScope.launch {
            delay(Random.nextLong(PING_MIN_DELAY_MS, PING_MAX_DELAY_MS))
            // nextInt берёт число из диапазона, верхняя граница не включается, поэтому +1
            val ping = Random.nextInt(PING_MIN_MS, PING_MAX_MS + 1)
            _uiState.update { it.copy(pingMs = ping, isPingLoading = false) }
        }
    }

    private companion object {
        const val CONNECT_DELAY_MS = 800L
        const val PING_MIN_MS = 30
        const val PING_MAX_MS = 150
        const val PING_MIN_DELAY_MS = 400L
        const val PING_MAX_DELAY_MS = 1200L
    }
}