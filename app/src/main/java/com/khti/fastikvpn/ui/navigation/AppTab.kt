package com.khti.fastikvpn.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.khti.fastikvpn.R

// Список вкладок нижнего меню.
// Это enum, но у каждого значения есть свои данные: иконка и подпись.
// Меню потом само пробегает по этому списку, поэтому чтобы добавить
// новую вкладку, достаточно дописать сюда одну строчку.
enum class AppTab(
    // Иконка вкладки. ImageVector - векторная картинка, её можно красить в любой цвет
    val icon: ImageVector,
    // Ссылка на текст в strings.xml. @StringRes - защита, чтобы сюда нельзя было
    // случайно передать обычное число, только ссылку на строку
    @StringRes val labelRes: Int
) {
    HOME(Icons.Filled.Home, R.string.tab_home),
    LOGIN(Icons.Filled.Person, R.string.tab_login),
    SETTINGS(Icons.Filled.Tune, R.string.tab_settings)
}