package com.khti.fastikvpn.ui.mainscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.ui.navigation.AppTab

// Нижнее меню целиком: скруглённая плашка и три иконки внутри.
// selectedTab - какая вкладка активна сейчас,
// onTabSelected - что сделать, когда нажали на другую вкладку.
@Composable
fun AppBottomBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Surface - подложка. Цвет и скругление берём из темы, а не пишем числами,
    // поэтому в светлой и тёмной теме меню выглядит правильно само.
    Surface(
        modifier = modifier
            .width(Dimens.BottomBarWidth)
            .height(Dimens.BottomBarHeight),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface
    ) {
        // Row ставит иконки в одну строку.
        // SpaceEvenly распределяет их с одинаковыми промежутками.
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // entries - список всех значений enum. Цикл создаёт иконку для каждой вкладки,
            // поэтому при добавлении новой вкладки в AppTab меню обновится само.
            AppTab.entries.forEach { tab ->
                BottomBarItem(
                    tab = tab,
                    // Сравниваем текущую вкладку с выбранной: true только у одной иконки
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}