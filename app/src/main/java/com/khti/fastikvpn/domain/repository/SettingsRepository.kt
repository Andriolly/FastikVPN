package com.khti.fastikvpn.domain.repository

import com.khti.fastikvpn.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Контракт (интерфейс) работы с настройками приложения.
 *
 * ViewModel знает только про этот интерфейс и не знает, где хранятся данные
 * (DataStore, база, сервер). Реализацию можно заменить, не трогая экраны.
 */
interface SettingsRepository {

    /** Поток текущего режима темы: UI подписывается и обновляется автоматически. */
    val themeMode: Flow<ThemeMode>

    /** Сохраняет выбор пользователя. */
    suspend fun setThemeMode(mode: ThemeMode)
}