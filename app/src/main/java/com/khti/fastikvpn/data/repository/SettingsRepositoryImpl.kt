package com.khti.fastikvpn.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.khti.fastikvpn.core.model.ThemeMode
import com.khti.fastikvpn.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * Реализация настроек поверх DataStore (маленькое хранилище «ключ - значение»
 * внутри телефона, замена SharedPreferences).
 *
 * @Inject constructor говорит Hilt: «создавай этот класс сам и подставляй
 * DataStore в конструктор». DataStore предоставляет DataStoreModule.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    // Ключ, под которым тема лежит в хранилище. Храним имя enum как строку.
    private val themeKey = stringPreferencesKey("theme_mode")

    override val themeMode: Flow<ThemeMode> = dataStore.data
        // Если файл повреждён или не читается, считаем настройки пустыми, а не падаем.
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            // Пусто или неизвестное значение -> SYSTEM.
            val saved = prefs[themeKey]
            ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.SYSTEM
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[themeKey] = mode.name }
    }
}