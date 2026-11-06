package com.khti.fastikvpn.ui.screens.home.components

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.khti.fastikvpn.R
import com.khti.fastikvpn.core.constants.AnimationConstants
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.model.VpnError
import com.khti.fastikvpn.core.theme.FastikTheme

// Строка с сообщением об ошибке под статусом подключения.
// Когда ошибки нет, остаётся пустое место фиксированной высоты, чтобы остальные
// элементы экрана не прыгали при появлении и исчезновении сообщения.
@Composable
fun ErrorMessage(
    error: VpnError?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.ErrorMessageHeight),
        contentAlignment = Alignment.Center
    ) {
        // Crossfade плавно растворяет старое содержимое и проявляет новое при смене error.
        // Благодаря этому сообщение исчезает плавно, а не пропадает рывком
        Crossfade(
            targetState = error,
            animationSpec = tween(durationMillis = AnimationConstants.FADE_MS),
            label = "errorMessage"
        ) { current ->
            if (current != null) {
                Text(
                    text = stringResource(errorTextRes(current)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FastikTheme.colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                )
            }
        }
    }
}

// Выбирает текст для каждой ошибки.
// when по enum без ветки else: если добавить новую ошибку в VpnError и забыть про неё здесь,
// проект не соберётся. Это защита от забытых случаев.
@StringRes
private fun errorTextRes(error: VpnError): Int = when (error) {
    VpnError.INVALID_SERVER_LINK -> R.string.error_invalid_link
    VpnError.TUNNEL_NOT_CREATED -> R.string.error_tunnel
    VpnError.CORE_START_FAILED -> R.string.error_core
    VpnError.SERVER_UNREACHABLE -> R.string.error_server_unreachable
    VpnError.UNKNOWN -> R.string.error_unknown
}