package com.khti.fastikvpn.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.khti.fastikvpn.R
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.model.ThemeMode

// Экран настроек. Пока заглушка.
// Кнопки смены темы здесь ВРЕМЕННЫЕ, чтобы мы могли проверить каркас в обеих темах.
// Когда будем делать настоящий экран, заменим их переключателем по макету.
// onThemeSelected - функция, которую нам передали сверху. Мы её только вызываем,
// а что с выбранной темой делать, решает не этот экран.
@Composable
fun SettingsScreen(
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.stub_settings),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.padding(top = Dimens.SpacingM),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
        ) {
            Button(onClick = { onThemeSelected(ThemeMode.DARK) }) {
                Text(stringResource(R.string.theme_dark))
            }
            Button(onClick = { onThemeSelected(ThemeMode.LIGHT) }) {
                Text(stringResource(R.string.theme_light))
            }
            Button(onClick = { onThemeSelected(ThemeMode.SYSTEM) }) {
                Text(stringResource(R.string.theme_system))
            }
        }
    }
}