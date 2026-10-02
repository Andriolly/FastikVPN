package com.khti.fastikvpn.core.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Дополнительные цвета, которых нет в стандартной Material-схеме
 * (свечение кнопки, цвет заголовков и т.д.).
 *
 * Стандартная схема Material (colorScheme) знает только свои роли:
 * primary, background, surface и т.п. Всё специфичное для FastikVPN
 * складываем сюда, и оно так же переключается вместе с темой.
 */
data class ExtendedColors(
    // Круг кнопки подключения: градиент от start к end
    val connectGradientStart: Color,
    val connectGradientEnd: Color,
    // Внешние круги свечения вокруг кнопки
    val connectRingSecond: Color,
    val connectRingThird: Color,
    // Цвет заголовков и названия приложения
    val title: Color,
    // Акцентная кнопка («Войти») и текст на ней
    val accentButton: Color,
    val onAccentButton: Color,
    // Неактивная кнопка (пользователь не авторизован)
    val disabledContainer: Color,
    val disabledContent: Color,
    // Иконки нижнего меню
    val navIconActive: Color,
    val navIconInactive: Color,
    // Цвет ошибок
    val error: Color
)

/** Набор для тёмной темы. */
val DarkExtendedColors = ExtendedColors(
    connectGradientStart = CircleGradientStart,
    connectGradientEnd = CircleGradientEnd,
    connectRingSecond = CircleSecond,
    connectRingThird = CircleThird,
    title = TitlePurple,
    accentButton = BgButton,
    onAccentButton = TextWhite,
    disabledContainer = DisabledDark,
    disabledContent = InactiveIconDark,
    navIconActive = CircleGradientStart,
    navIconInactive = InactiveIconDark,
    error = ErrorRed
)

/** Набор для светлой темы. */
val LightExtendedColors = ExtendedColors(
    connectGradientStart = CircleGradientStart,
    connectGradientEnd = CircleGradientEnd,
    connectRingSecond = CircleSecond,
    connectRingThird = CircleThird,
    title = TitlePurple,
    accentButton = BgButton,
    onAccentButton = TextWhite,
    disabledContainer = DisabledLight,
    disabledContent = InactiveIconLight,
    navIconActive = BgButton,
    navIconInactive = InactiveIconLight,
    error = ErrorRed
)

/**
 * CompositionLocal: механизм Compose, который «спускает» значение вниз по дереву
 * UI без передачи параметрами. Тема кладёт сюда нужный набор цветов, а любой
 * экран читает его через FastikTheme.colors.
 * Значение по умолчанию - тёмный набор (на случай, если тему забыли подключить).
 */
val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }