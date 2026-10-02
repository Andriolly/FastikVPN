package com.khti.fastikvpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

/**
 * Единственная Activity приложения FastikVPN.
 *
 * @AndroidEntryPoint сообщает Hilt, что сюда можно внедрять зависимости
 * и что отсюда можно получать ViewModel через hiltViewModel().
 * Без неё hiltViewModel() внутри Compose выдаст ошибку.
 *
 * Activity ничего не рисует сама: она только запускает Compose
 * и вызывает корневой экран. Позже здесь появится:
 *   FastikVpnTheme { MainScreen() }
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Рисуем «во весь экран»: контент заходит под статус-бар и нижнюю панель системы.
        enableEdgeToEdge()

        // setContent - точка входа в Compose. Всё внутри - это наш интерфейс.
        setContent {
            // ВРЕМЕННАЯ заглушка, чтобы проверить, что проект собирается.
            // На следующем этапе заменим на тему и MainScreen.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "FastikVPN")
            }
        }
    }
}