package com.khti.fastikvpn.core.theme

import androidx.compose.ui.graphics.Color

/**
 * ПАЛИТРА: здесь лежат только «сырые» цвета с дашборда, без привязки к теме.
 * Имена совпадают с именами в дашборде (bg_main_dark и т.д.), чтобы их было
 * легко сверять с макетом.
 *
 * Правило: НИГДЕ в экранах не пишем Color(0xFF...). Экраны берут цвета только из
 * MaterialTheme.colorScheme или FastikTheme.colors (см. Theme.kt).
 * Чтобы поменять цвет, меняем его ТОЛЬКО в этом файле.
 */

// --- Фоны (тёмная тема) ---
val BgMainDark = Color(0xFF0B142A)   // основной фон
val BgMenuDark = Color(0xFF0D1935)   // нижнее меню, карточки, поля

// --- Фоны (светлая тема) ---
val BgMainLight = Color(0xFFEDF1FD)  // основной фон
val BgMenuLight = Color(0xFFE0E1E2)  // нижнее меню, карточки, поля

// --- Акценты, общие для обеих тем ---
val BgButton = Color(0xFF4A1C76)         // кнопка «Войти»
val TitlePurple = Color(0xFF8442AA)      // название приложения и заголовки
val CircleGradientStart = Color(0xFFA855F7) // первый круг и «облако»: начало градиента
val CircleGradientEnd = Color(0xFF7C3AED)   // конец градиента
val CircleSecond = Color(0xFF8B5CF6)     // второй круг свечения
val CircleThird = Color(0xFF8B5CF6)      // третий круг свечения

// --- Текст ---
val TextWhite = Color(0xFFFFFFFF)
val TextBlack = Color(0xFF000000)

// --- Вспомогательные (на дашборде не заданы, подобраны мной, можно менять) ---
val DisabledDark = Color(0xFF2A3350)     // неактивная кнопка в тёмной теме
val DisabledLight = Color(0xFFC9CBD1)    // неактивная кнопка в светлой теме
val InactiveIconDark = Color(0xFF6B4A8F) // неактивная иконка меню (тёмная)
val InactiveIconLight = Color(0xFF7A7F8A)// неактивная иконка меню (светлая)
val ErrorRed = Color(0xFFE5484D)         // ошибки