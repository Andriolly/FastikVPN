package com.khti.fastikvpn.vpn

import android.content.Context
import androidx.core.content.ContextCompat
import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.core.model.VpnError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.khti.fastikvpn.core.model.SessionStats

// Пульт управления VPN для остального приложения.
// Экраны и ViewModel не должны знать, как запускаются сервисы Android и как устроено ядро.
// Им достаточно нескольких кнопок и табло состояния.
@Singleton
class VpnController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val statusHolder: VpnStatusHolder,
    private val xrayCore: XrayCore
) {

    // Табло: текущее состояние подключения
    val state: StateFlow<ConnectionState> = statusHolder.state

    // Адрес сервера, пока VPN включён
    val serverAddress: StateFlow<String?> = statusHolder.serverAddress

    // Последняя ошибка
    val error: StateFlow<VpnError?> = statusHolder.error

    // Статистика текущей сессии: объём, скорость, время
    val sessionStats: StateFlow<SessionStats> = statusHolder.sessionStats

    // Запускает сервис как foreground. Разрешения должны быть получены ДО этого вызова.
    fun connect() {
        // Перед новой попыткой забываем старую ошибку
        statusHolder.clearError()
        ContextCompat.startForegroundService(context, FastikVpnService.startIntent(context))
    }

    // Отправляет сервису команду остановки. Разрешения для этого не нужны.
    fun disconnect() {
        context.startService(FastikVpnService.stopIntent(context))
    }

    // Убирает ошибку с табло (например, после того как пользователь её увидел)
    fun clearError() {
        statusHolder.clearError()
    }

    // Измеряет настоящий пинг в миллисекундах.
    // Сама библиотека делает два запроса и возвращает меньшее время, поэтому здесь
    // достаточно одного вызова. Result хранит либо число, либо ошибку.
    suspend fun measurePing(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching { xrayCore.measureDelay(PING_URL).toInt() }
    }

    private companion object {
        // Служебный адрес Google для проверки связи: отвечает пустым ответом 204
        const val PING_URL = "https://www.gstatic.com/generate_204"
    }
}