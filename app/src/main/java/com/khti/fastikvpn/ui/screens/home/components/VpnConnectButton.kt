package com.khti.fastikvpn.ui.screens.home.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.khti.fastikvpn.R
import com.khti.fastikvpn.core.constants.AnimationConstants
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.core.theme.FastikTheme

// Большая кнопка подключения: три вложенных круга.
// Нажимается вся область большого круга, а не только центральный кружок.
// enabled - можно ли нажимать. Пока всегда true, но когда появится вход в аккаунт,
// для неавторизованного пользователя кнопка станет серой.
@Composable
fun VpnConnectButton(
    connectionState: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Насколько кнопка должна светиться в каждом состоянии (от 0 до 1)
    val glowTarget = when (connectionState) {
        ConnectionState.DISCONNECTED -> 0f
        ConnectionState.CONNECTING -> 0.5f
        ConnectionState.CONNECTED -> 1f
    }

    // Плавный переход к целевому значению, как ручка диммера
    val glow by animateFloatAsState(
        targetValue = glowTarget,
        animationSpec = tween(durationMillis = AnimationConstants.CONNECT_BUTTON_MS),
        label = "glow"
    )

    // Пульсация запускается, только пока что-то светится, чтобы не тратить батарею
    val pulse = if (glow > 0f) rememberPulse() else 0f

    // Коэффициент "дыхания" яркости. Когда кнопка полностью включена (glow = 1):
    // на вдохе (pulse = 1) он равен 1, то есть полная яркость,
    // на выдохе (pulse = 0) он равен 0.6, то есть заметно тусклее.
    // Когда кнопка выключена (glow = 0), он всегда равен 1 и ничего не меняется.
    val breath = 1f - 0.4f * glow * (1f - pulse)

    // InteractionSource следит за касаниями. Нужен, чтобы узнать, прижат ли палец
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Пока палец прижат, вся кнопка слегка уменьшается до 95%.
    // Это даёт ощущение, что кнопку действительно нажимаешь.
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressScale"
    )

    // Подпись для программ чтения с экрана
    val description = stringResource(R.string.cd_connect_button)

    // Нажимать можно, если кнопка доступна и подключение сейчас не идёт
    val canClick = enabled && connectionState != ConnectionState.CONNECTING

    Box(
        modifier = modifier.size(Dimens.ThirdCircle),
        contentAlignment = Alignment.Center
    ) {
        // Слой с картинкой: три круга. Он уменьшается при нажатии.
        // Нажатие обрабатывает не он, а отдельный слой ниже по коду.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                },
            contentAlignment = Alignment.Center
        ) {
            // Третье кольцо: самое большое и слабое
            Box(
                modifier = Modifier
                    .size(Dimens.ThirdCircle)
                    .graphicsLayer {
                        // Размер колеблется на 10% вместе с пульсацией
                        val scale = 1f + 0.10f * pulse * glow
                        scaleX = scale
                        scaleY = scale
                        // Прозрачность тоже "дышит": базовое значение умножаем на breath
                        alpha = (0.06f + 0.14f * glow) * breath
                    }
                    .background(FastikTheme.colors.connectRingThird, CircleShape)
            )

            // Второе кольцо: та же идея, но амплитуда поменьше
            Box(
                modifier = Modifier
                    .size(Dimens.SecondCircle)
                    .graphicsLayer {
                        val scale = 1f + 0.06f * pulse * glow
                        scaleX = scale
                        scaleY = scale
                        alpha = (0.12f + 0.18f * glow) * breath
                    }
                    .background(FastikTheme.colors.connectRingSecond, CircleShape)
            )

            // Сама кнопка: внутренний круг с градиентом и иконкой
            Box(
                modifier = Modifier
                    .size(Dimens.FirstCircle)
                    .graphicsLayer {
                        // Кнопка тоже чуть набухает на вдохе
                        val scale = 1f + 0.03f * pulse * glow
                        scaleX = scale
                        scaleY = scale
                        // Яркость растёт с включением и чуть колеблется вместе с дыханием
                        alpha = (0.55f + 0.45f * glow) * (1f - 0.15f * glow * (1f - pulse))
                    }
                    .clip(CircleShape)
                    .background(
                        if (enabled) {
                            Brush.linearGradient(
                                listOf(
                                    FastikTheme.colors.connectGradientStart,
                                    FastikTheme.colors.connectGradientEnd
                                )
                            )
                        } else {
                            SolidColor(FastikTheme.colors.disabledContainer)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    // Описание вынесено на слой нажатия, здесь оно не нужно
                    contentDescription = null,
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        FastikTheme.colors.disabledContent
                    },
                    modifier = Modifier.size(Dimens.WifiIconWidth)
                )
            }
        }

        // Невидимый слой нажатия. Лежит поверх всех кругов и занимает всю область.
        // Порядок модификаторов важен: сначала clip обрезает слой по форме круга,
        // и только потом clickable. Поэтому нажатие срабатывает внутри большого круга,
        // а в углах квадрата вокруг него ничего не происходит.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .semantics { contentDescription = description }
                .clickable(
                    interactionSource = interactionSource,
                    // Стандартную волну отключаем: на таком большом круге она смотрится странно.
                    // Вместо неё работает эффект уменьшения при нажатии (pressScale)
                    indication = null,
                    enabled = canClick,
                    role = Role.Button,
                    onClick = onClick
                )
        )
    }
}

// Возвращает число, которое плавно ходит от 0 до 1 и обратно, как качели.
// Из него складывается эффект дыхания.
@Composable
private fun rememberPulse(): Float {
    val transition = rememberInfiniteTransition(label = "pulseTransition")

    val value by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = AnimationConstants.PULSE_MS,
                // EaseInOutSine: плавный разгон и плавное торможение, как у настоящего дыхания
                easing = EaseInOutSine
            ),
            // Reverse: дойдя до конца, идём обратно, а не прыгаем в начало
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    return value
}