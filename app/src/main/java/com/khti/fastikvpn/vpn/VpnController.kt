package com.khti.fastikvpn.vpn

import android.content.Context
import androidx.core.content.ContextCompat
import com.khti.fastikvpn.core.model.ConnectionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Пульт управления VPN для остального приложения.
// Экраны и ViewModel не должны знать, как запускаются сервисы Android.
// Им достаточно двух кнопок (connect, disconnect) и табло состояния.
@Singleton
class VpnController @Inject constructor(
    @ApplicationContext private val context: Context,
    statusHolder: VpnStatusHolder
) {

    // Табло: текущее состояние подключения
    val state: StateFlow<ConnectionState> = statusHolder.state

    // Запускает сервис как foreground. Разрешения должны быть получены ДО этого вызова.
    fun connect() {
        ContextCompat.startForegroundService(context, FastikVpnService.startIntent(context))
    }

    // Отправляет сервису команду остановки. Разрешения для этого не нужны.
    fun disconnect() {
        context.startService(FastikVpnService.stopIntent(context))
    }
}


/**
 *
 *   Кнопка -> Разрешения -> View ->  VPNController -> VPNService
 *
 *                                          ^ StatusHolder
 *
 *
 */