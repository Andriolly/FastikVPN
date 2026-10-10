package com.khti.fastikvpn.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.khti.fastikvpn.R
import com.khti.fastikvpn.core.constants.AnimationConstants
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.theme.FastikTheme

// Строка с информацией об IP. По ширине совпадает с нижним меню:
// слева карточка с адресом, справа выезжает плашка с пингом.
// Вместе с зазором они занимают ровно ширину меню (расчёт в Dimens).
@Composable
fun IpInfoRow(
    ipAddress: String?,
    isIpLoading: Boolean,
    pingMs: Int?,
    isPingLoading: Boolean,
    isPingVisible: Boolean,
    canRefreshPing: Boolean,
    onCardClick: () -> Unit,
    onRefreshPingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        // Ширина строки равна ширине нижнего меню, поэтому края совпадают
        modifier = modifier.width(Dimens.BottomBarWidth),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IpCard(
            ipAddress = ipAddress,
            isLoading = isIpLoading,
            isPingVisible = isPingVisible,
            onClick = onCardClick
        )

        // Плашка пинга плавно выезжает вбок и проявляется
        AnimatedVisibility(
            visible = isPingVisible,
            enter = fadeIn(tween(AnimationConstants.FADE_MS)) +
                    expandHorizontally(tween(AnimationConstants.FADE_MS)),
            exit = fadeOut(tween(AnimationConstants.FADE_MS)) +
                    shrinkHorizontally(tween(AnimationConstants.FADE_MS))
        ) {
            PingBadge(
                pingMs = pingMs,
                isLoading = isPingLoading,
                canRefresh = canRefreshPing,
                onRefreshClick = onRefreshPingClick,
                // Отступ слева создаёт зазор между карточкой и плашкой
                modifier = Modifier.padding(start = Dimens.IpPingGap)
            )
        }
    }
}

// Карточка с иконкой, адресом и стрелкой-подсказкой. Нажимается целиком.
@Composable
private fun IpCard(
    ipAddress: String?,
    isLoading: Boolean,
    isPingVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.small

    // Стрелка плавно разворачивается на 180 градусов, когда пинг открыт.
    // Это подсказка пользователю: по карточке можно нажать, и она что-то раскрывает.
    val arrowRotation by animateFloatAsState(
        targetValue = if (isPingVisible) 180f else 0f,
        animationSpec = tween(durationMillis = AnimationConstants.FADE_MS),
        label = "arrowRotation"
    )

    // Что показать вместо адреса: загрузку, ошибку или сам адрес
    val ipText = when {
        isLoading -> stringResource(R.string.ip_loading)
        ipAddress != null -> ipAddress
        else -> stringResource(R.string.ip_error)
    }

    Surface(
        modifier = modifier
            .width(Dimens.IpCardWidth)
            .height(Dimens.IpCardHeight)
            // Сначала обрезаем по форме карточки, потом делаем нажимаемой
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.SpacingS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
        ) {
            Icon(
                imageVector = Icons.Outlined.Public,
                contentDescription = null,
                tint = FastikTheme.colors.navIconActive,
                modifier = Modifier.size(Dimens.IpIconHeight)
            )

            // weight(1f) отдаёт тексту всё свободное место, поэтому стрелка прижимается к краю
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ip_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = ipText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    // Длинный адрес (IPv6) не должен ломать карточку
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                // Приглушённый цвет, чтобы стрелка подсказывала, но не отвлекала
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(Dimens.ActionIconSize)
                    .graphicsLayer { rotationZ = arrowRotation }
            )
        }
    }
}

// Плашка с пингом и кнопкой обновления справа.
@Composable
private fun PingBadge(
    pingMs: Int?,
    isLoading: Boolean,
    canRefresh: Boolean,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(Dimens.PingBadgeWidth)
            .height(Dimens.IpCardHeight),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
            // Текст слева, значок обновления справа
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Crossfade плавно меняет текст "Пинг: …" на число и обратно
            Crossfade(
                targetState = isLoading,
                animationSpec = tween(durationMillis = AnimationConstants.FADE_MS),
                label = "pingText"
            ) { loading ->
                Text(
                    text = when {
                        loading -> stringResource(R.string.ping_loading)
                        pingMs != null -> stringResource(R.string.ping_value, pingMs)
                        else -> stringResource(R.string.ping_unknown)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Вращение включаем только во время измерения, чтобы не тратить батарею
            val spin = if (isLoading) rememberSpin() else 0f

            IconButton(
                onClick = onRefreshClick,
                // Обновлять нельзя, если VPN выключен или измерение уже идёт
                enabled = canRefresh && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.cd_refresh_ping),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(Dimens.ActionIconSize)
                        .graphicsLayer { rotationZ = spin }
                )
            }
        }
    }
}

// Возвращает угол от 0 до 360 градусов, который бесконечно растёт.
// Значок, повёрнутый на этот угол, непрерывно вращается.
@Composable
private fun rememberSpin(): Float {
    val transition = rememberInfiniteTransition(label = "spinTransition")

    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            // LinearEasing: вращение с постоянной скоростью, без разгона и торможения
            animation = tween(
                durationMillis = AnimationConstants.SPIN_MS,
                easing = LinearEasing
            ),
            // Restart: дойдя до 360, начинаем заново, и вращение выглядит непрерывным
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    return angle
}