package com.juanarton.perfprofiler.di

import com.juanarton.perfprofiler.core.data.domain.usecase.local.AppRepositoryImpl
import com.juanarton.perfprofiler.core.data.domain.usecase.local.AppRepositoryUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
abstract class AppModule
{
    @Binds
    @ViewModelScoped
    abstract fun provideAppRepositoryUseCase(
        appRepositoryImpl: AppRepositoryImpl
    ): AppRepositoryUseCase
}