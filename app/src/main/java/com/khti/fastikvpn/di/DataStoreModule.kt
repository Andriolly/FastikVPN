package com.khti.fastikvpn.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Делегат создаёт DataStore один раз на весь процесс. Файл настроек называется
 * "fastik_settings". Обязательно объявляется на верхнем уровне файла (не в классе),
 * иначе получится несколько экземпляров, и приложение упадёт.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "fastik_settings"
)

/**
 * Hilt-модуль: «инструкция» для объектов, которые нельзя создать через @Inject.
 * DataStore создаётся не конструктором, а делегатом, поэтому описываем его вручную.
 *
 * @InstallIn(SingletonComponent) - живёт на уровне всего приложения.
 * @Singleton - экземпляр один на всё приложение.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.settingsDataStore
}