package com.khti.fastikvpn.ui.screens.home.components

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.khti.fastikvpn.core.common.Formatters
import com.khti.fastikvpn.core.constants.AnimationConstants
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.model.SessionStats
import com.khti.fastikvpn.core.model.VpnError
import com.khti.fastikvpn.core.theme.FastikTheme

// Место под статусом подключения. Показывает одно из трёх:
// ошибку (важнее всего), статистику сессии (если VPN включён) или пустое место.
// Высота фиксированная, чтобы остальные элементы экрана не прыгали при смене содержимого.
@Composable
fun StatusDetails(
    error: VpnError?,
    session: SessionStats,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    // Определяем, что показывать. Ошибка важнее статистики
    val content: DetailsContent = when {
        error != null -> DetailsContent.Error(error)
        isConnected -> DetailsContent.Stats
        else -> DetailsContent.None
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.StatusDetailsHeight),
        contentAlignment = Alignment.Center
    ) {
        // Crossfade срабатывает, когда меняется ВИД содержимого (пусто, статистика, ошибка).
        // Цифры внутри статистики обновляются каждую секунду, но вид остаётся прежним
        // (Stats это один и тот же объект), поэтому растворения не происходит
        Crossfade(
            targetState = content,
            animationSpec = tween(durationMillis = AnimationConstants.FADE_MS),
            label = "statusDetails"
        ) { current ->
            when (current) {
                is DetailsContent.Error -> ErrorText(current.error)
                DetailsContent.Stats -> StatsText(session)
                DetailsContent.None -> Unit
            }
        }
    }
}

// Виды содержимого. sealed interface: набор вариантов закрыт, компилятор проверяет,
// что в when мы разобрали все.
// Error - data class, потому что хранит ошибку. Stats и None - объекты без данных
private sealed interface DetailsContent {
    data class Error(val error: VpnError) : DetailsContent
    data object Stats : DetailsContent
    data object None : DetailsContent
}

// Три строки статистики: время, скорость, общий объём
@Composable
private fun StatsText(session: SessionStats) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Время сессии
        Text(
            text = Formatters.formatDuration(session.elapsedSeconds),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Текущая скорость: загрузка и отдача
        Text(
            text = stringResource(
                R.string.session_speed,
                Formatters.formatSpeed(session.downloadSpeed),
                Formatters.formatSpeed(session.uploadSpeed)
            ),
            style = MaterialTheme.typography.bodyMedium,
            // Приглушаем, чтобы время оставалось главным
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        // Общий объём за сессию
        Text(
            text = stringResource(
                R.string.session_total,
                Formatters.formatBytes(session.downloadBytes),
                Formatters.formatBytes(session.uploadBytes)
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

// Сообщение об ошибке красным цветом
@Composable
private fun ErrorText(error: VpnError) {
    Text(
        text = stringResource(errorTextRes(error)),
        style = MaterialTheme.typography.bodyMedium,
        color = FastikTheme.colors.error,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
    )
}

// Выбирает текст для каждой ошибки.
// when по enum без else: если добавить новую ошибку в VpnError и забыть про неё здесь,
// проект не соберётся
@StringRes
private fun errorTextRes(error: VpnError): Int = when (error) {
    VpnError.INVALID_SERVER_LINK -> R.string.error_invalid_link
    VpnError.TUNNEL_NOT_CREATED -> R.string.error_tunnel
    VpnError.CORE_START_FAILED -> R.string.error_core
    VpnError.SERVER_UNREACHABLE -> R.string.error_server_unreachable
    VpnError.UNKNOWN -> R.string.error_unknown
}