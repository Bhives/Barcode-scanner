package com.reproflex3.rplink.di

import com.reproflex3.rplink.data.location.RPLinkLocationManager
import com.reproflex3.rplink.data.location.RPLinkLocationManagerImpl
import com.reproflex3.rplink.data.preferences.PreferencesManager
import com.reproflex3.rplink.data.preferences.PreferencesManagerImpl
import com.reproflex3.rplink.data.repository.RPLinkRepositoryImpl
import com.reproflex3.rplink.domain.repositoryinterface.RPLinkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppInterfacesModule {

    @Binds
    @Singleton
    fun providePreferencesManager(preferencesManagerImpl: PreferencesManagerImpl): PreferencesManager

    @Binds
    @Singleton
    fun provideRepository(repositoryImpl: RPLinkRepositoryImpl): RPLinkRepository

    @Binds
    @Singleton
    fun provideRPLinkLocationManager(locationManagerImpl: RPLinkLocationManagerImpl): RPLinkLocationManager
}