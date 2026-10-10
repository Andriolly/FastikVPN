package com.khti.fastikvpn.ui.screens.home.components

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.khti.fastikvpn.R
import com.khti.fastikvpn.core.constants.AnimationConstants
import com.khti.fastikvpn.core.model.ConnectionState

// Подпись под кнопкой: "Отключено", "Подключение…" или "Подключено".
@Composable
fun ConnectionStatusText(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    // Выбираем ссылку на нужный текст по состоянию
    @StringRes val textRes = when (connectionState) {
        ConnectionState.DISCONNECTED -> R.string.status_disconnected
        ConnectionState.CONNECTING -> R.string.status_connecting
        ConnectionState.CONNECTED -> R.string.status_connected
    }

    // Crossfade плавно растворяет старую подпись и проявляет новую,
    // чтобы текст не менялся рывком
    Crossfade(
        targetState = textRes,
        animationSpec = tween(durationMillis = AnimationConstants.FADE_MS),
        modifier = modifier,
        label = "statusText"
    ) { res ->
        Text(
            text = stringResource(res),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}