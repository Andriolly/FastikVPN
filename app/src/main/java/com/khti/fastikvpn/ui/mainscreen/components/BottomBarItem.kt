package com.khti.fastikvpn.ui.mainscreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.khti.fastikvpn.core.constants.AnimationConstants
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.theme.FastikTheme
import com.khti.fastikvpn.ui.navigation.AppTab

// Одна иконка нижнего меню.
// Сама она ничего не решает: ей говорят, выбрана ли она (selected),
// и что делать при нажатии (onClick). Такой приём называется "подъём состояния":
// логика живёт выше, а здесь только внешний вид.
@Composable
fun BottomBarItem(
    tab: AppTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Целевой цвет иконки: активный, если вкладка выбрана, иначе неактивный.
    // animateColorAsState не меняет цвет рывком, а плавно перетекает к новому.
    // Длительность берём из общих констант, чтобы настраивать её в одном месте.
    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            FastikTheme.colors.navIconActive
        } else {
            FastikTheme.colors.navIconInactive
        },
        animationSpec = tween(durationMillis = AnimationConstants.FADE_MS),
        label = "navIconColor"
    )

    // IconButton уже умеет обрабатывать нажатие и показывает эффект волны.
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = tab.icon,
            // contentDescription читают программы для слабовидящих,
            // поэтому подпись у иконки обязательна
            contentDescription = stringResource(tab.labelRes),
            tint = iconColor,
            modifier = Modifier.size(Dimens.NavIconSize)
        )
    }
}