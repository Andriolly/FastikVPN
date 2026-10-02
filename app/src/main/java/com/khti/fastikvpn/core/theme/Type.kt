package com.khti.fastikvpn.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Шрифт приложения. Сейчас системный.
 * Чтобы сменить шрифт, нужно положить файлы в res/font и заменить значение
 * FontFamily.Default на свой FontFamily. Больше нигде ничего менять не придётся.
 */
val AppFontFamily: FontFamily = FontFamily.Default

/**
 * Стили текста из дашборда TEXT_STYLES, привязанные к ролям Material:
 *
 *  status-text (40, bold)   -> headlineLarge  («Подключено»)
 *  text_name-vpn (30)       -> headlineMedium (название «FastikVPN» в шапке)
 *  title_text (25)          -> titleLarge     («Вход», «Настройки»)
 *  ip-text (10)             -> labelSmall     (подпись IP)
 *
 * Цвет здесь не задаём: он зависит от темы (чёрный/белый) и приходит из
 * colorScheme.onBackground. Заголовки красим в FastikTheme.colors.title на месте.
 */
val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    )
)