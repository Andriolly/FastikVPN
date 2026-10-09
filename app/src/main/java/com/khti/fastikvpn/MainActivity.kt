package com.khti.fastikvpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khti.fastikvpn.core.model.ThemeMode
import com.khti.fastikvpn.core.theme.FastikVpnTheme
import com.khti.fastikvpn.ui.mainscreen.MainScreen
import com.khti.fastikvpn.ui.mainscreen.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Единственная Activity приложения.
 *
 *  @AndroidEntryPoint сообщает Hilt, что сюда можно внедрять зависимости
 *  и что отсюда можно получать ViewModel через hiltViewModel().
 *  Без неё hiltViewModel() внутри Compose выдаст ошибку.
 *
 * Задачи: показать заставку, пока грузится тема, затем запустить Compose с этой темой.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ViewModel с темой. by viewModels() создаёт её через Hilt
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen обязательно вызываем ДО super.onCreate
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Пока тема не прочитана (null), заставка остаётся на экране.
        // Благодаря этому первый кадр приложения рисуется уже в правильной теме.
        splashScreen.setKeepOnScreenCondition { viewModel.themeMode.value == null }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            // Знак ?: означает "если слева пусто, возьми значение справа"
            FastikVpnTheme(themeMode = themeMode ?: ThemeMode.SYSTEM) {
                // Surface красит весь экран в цвет фона темы
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(onThemeSelected = viewModel::onThemeSelected)
                }
            }
        }
    }
}