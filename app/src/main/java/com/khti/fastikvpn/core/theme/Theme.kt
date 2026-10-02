package com.khti.fastikvpn.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.khti.fastikvpn.core.model.ThemeMode

/**
 * Стандартная схема Material для тёмной темы. Каждой роли назначаем наш цвет:
 *  background  - основной фон экрана;
 *  surface     - нижнее меню, карточки;
 *  primary     - основной акцент;
 *  onXxx       - цвет контента (текста, иконок) поверх соответствующего фона.
 */
private val DarkColorScheme = darkColorScheme(
    primary = CircleGradientStart,
    onPrimary = TextWhite,
    secondary = CircleSecond,
    background = BgMainDark,
    onBackground = TextWhite,
    surface = BgMenuDark,
    onSurface = TextWhite,
    surfaceVariant = BgMenuDark,
    onSurfaceVariant = TextWhite,
    error = ErrorRed
)

/** То же самое для светлой темы. */
private val LightColorScheme = lightColorScheme(
    primary = BgButton,
    onPrimary = TextWhite,
    secondary = CircleSecond,
    background = BgMainLight,
    onBackground = TextBlack,
    surface = BgMenuLight,
    onSurface = TextBlack,
    surfaceVariant = BgMenuLight,
    onSurfaceVariant = TextBlack,
    error = ErrorRed
)

/**
 * Корневая тема приложения. Оборачивает весь UI:
 *   FastikVpnTheme(themeMode) { MainScreen() }
 *
 * Что она делает:
 * 1. определяет, тёмная ли тема сейчас (по выбору пользователя или по системе);
 * 2. выбирает нужную схему цветов;
 * 3. раздаёт вниз по дереву схему, типографику, формы и наши доп. цвета;
 * 4. подстраивает цвет иконок системного статус-бара под фон.
 */
@Composable
fun FastikVpnTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    // Статус-бар: на светлом фоне иконки должны быть тёмными, на тёмном - светлыми.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

/**
 * Короткий доступ к нашим доп. цветам в любом Composable:
 *   FastikTheme.colors.title
 * По аналогии со стандартным MaterialTheme.colorScheme.
 */
object FastikTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}