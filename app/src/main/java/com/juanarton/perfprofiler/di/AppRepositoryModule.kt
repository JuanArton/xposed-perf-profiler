package com.juanarton.perfprofiler.di

import com.juanarton.perfprofiler.core.data.domain.repository.IAppRepository
import com.juanarton.perfprofiler.core.data.repository.AppRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class AppRepositoryModule {
    @Binds
    abstract fun provideAppRepository(
        appRepository: AppRepository
    ): IAppRepository
}