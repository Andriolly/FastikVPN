package com.khti.fastikvpn.ui.mainscreen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khti.fastikvpn.core.constants.AnimationConstants
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.model.ThemeMode
import com.khti.fastikvpn.ui.mainscreen.components.AppBottomBar
import com.khti.fastikvpn.ui.mainscreen.components.AppTopBar
import com.khti.fastikvpn.ui.navigation.AppTab
import com.khti.fastikvpn.ui.screens.home.HomeScreen
import com.khti.fastikvpn.ui.screens.login.LoginScreen
import com.khti.fastikvpn.ui.screens.settings.SettingsScreen

// Основной экран-каркас. Он собирает в одну колонку три части:
// шапку, область с меняющимся содержимым и нижнее меню.
// onThemeSelected - временный параметр, нужен только для кнопок смены темы в настройках.
// viewModel по умолчанию создаёт Hilt (hiltViewModel), нам вручную ничего делать не нужно.
@Composable
fun MainScreen(
    onThemeSelected: (ThemeMode) -> Unit,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    // Подписываемся на выбранную вкладку. Когда значение во ViewModel меняется,
    // эта переменная обновляется и Compose сам перерисовывает нужные части.
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Отодвигаем содержимое от статус-бара сверху и от жестовой панели снизу,
            // иначе шапка окажется под часами, а меню под системной полосой
            .systemBarsPadding()
    ) {
        AppTopBar()

        // Область содержимого. weight(1f) говорит: "займи всё место,
        // которое осталось между шапкой и меню". Так меню всегда прижато к низу.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Crossfade плавно растворяет старый экран и показывает новый
            // при смене значения targetState. Без него экраны менялись бы рывком.
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = AnimationConstants.FADE_MS),
                label = "screenSwitch"
            ) { tab ->
                // when выбирает, какой экран показать для вкладки.
                // Если добавить новую вкладку в AppTab и забыть про неё здесь,
                // проект не соберётся. Это защита от забытых случаев.
                when (tab) {
                    AppTab.HOME -> HomeScreen()
                    AppTab.LOGIN -> LoginScreen()
                    AppTab.SETTINGS -> SettingsScreen(onThemeSelected = onThemeSelected)
                }
            }
        }

        AppBottomBar(
            selectedTab = selectedTab,
            // viewModel::onTabSelected - ссылка на функцию. Короткая запись вместо
            // { tab -> viewModel.onTabSelected(tab) }
            onTabSelected = viewModel::onTabSelected,
            modifier = Modifier
                // Меню по центру по горизонтали (align доступен внутри Column)
                .align(Alignment.CenterHorizontally)
                .padding(bottom = Dimens.SpacingM)
        )
    }
}