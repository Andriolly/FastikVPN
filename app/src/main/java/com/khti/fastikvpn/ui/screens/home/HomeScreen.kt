package com.khti.fastikvpn.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khti.fastikvpn.core.constants.Dimens
import com.khti.fastikvpn.core.model.ConnectionState
import com.khti.fastikvpn.ui.screens.home.components.ConnectionStatusText
import com.khti.fastikvpn.ui.screens.home.components.IpInfoRow
import com.khti.fastikvpn.ui.screens.home.components.VpnConnectButton

// Главный экран, версия "с памятью": получает ViewModel и подписывается на её состояние.
// Сам ничего не рисует, а передаёт данные в HomeContent.
// ViewModel привязана к окну приложения, а не к этому экрану, поэтому при переключении
// вкладок состояние не теряется: вернувшись, пользователь увидит ту же включённую кнопку.
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Функция "запросить разрешения и подключиться". Когда все разрешения получены,
    // вызывается viewModel.onConnectClick (включение VPN)
    val requestConnect = rememberVpnPermissionRequest(onReady = viewModel::onConnectClick)

    HomeContent(
        state = state,
        onConnectClick = {
            if (state.connectionState == ConnectionState.DISCONNECTED) {
                // Включение: сначала разрешения. Отключению разрешения не нужны
                requestConnect()
            } else {
                viewModel.onConnectClick()
            }
        },
        onIpCardClick = viewModel::onIpCardClick,
        onPingRefreshClick = viewModel::onPingRefreshClick,
        modifier = modifier
    )
}

// Версия "без памяти": только рисует то, что ей передали.
@Composable
private fun HomeContent(
    state: HomeUiState,
    onConnectClick: () -> Unit,
    onIpCardClick: () -> Unit,
    onPingRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        // Верхняя часть: кнопка и подпись по центру
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VpnConnectButton(
                connectionState = state.connectionState,
                onClick = onConnectClick,
                // Без входа кнопка серая и неактивная
                enabled = state.isAuthorized
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingL))

            ConnectionStatusText(connectionState = state.connectionState)
        }

        // Нижняя часть: блок IP и пинг по центру, как нижнее меню
        IpInfoRow(
            ipAddress = state.ipAddress,
            isIpLoading = state.isIpLoading,
            pingMs = state.pingMs,
            isPingLoading = state.isPingLoading,
            isPingVisible = state.isPingVisible,
            // Обновлять пинг можно, только когда VPN включён
            canRefreshPing = state.connectionState == ConnectionState.CONNECTED,
            onCardClick = onIpCardClick,
            onRefreshPingClick = onPingRefreshClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = Dimens.SpacingM)
        )
    }
}