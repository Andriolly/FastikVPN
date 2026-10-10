package com.khti.fastikvpn.di

import com.khti.fastikvpn.data.repository.IpRepositoryImpl
import com.khti.fastikvpn.data.repository.SettingsRepositoryImpl
import com.khti.fastikvpn.domain.repository.IpRepository
import com.khti.fastikvpn.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Привязывает интерфейс к реализации: когда кому-то нужен SettingsRepository,
 * Hilt подставляет SettingsRepositoryImpl.
 * Если позже поменяем хранилище, достаточно заменить класс в этой строке.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    // Когда кто-то просит IpRepository, Hilt выдаёт IpRepositoryImpl
    @Binds
    @Singleton
    abstract fun bindIpRepository(
        impl: IpRepositoryImpl
    ): IpRepository
}