package com.juanarton.perfprofiler.di

import android.content.Context
import androidx.room.Room
import com.juanarton.perfprofiler.core.data.source.local.room.AppDatabase
import com.juanarton.perfprofiler.core.data.source.local.room.dao.AppProfileDAO
import com.juanarton.perfprofiler.core.data.source.local.room.dao.ProfileDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "profile.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: AppDatabase): ProfileDAO = database.profileDao()

    @Provides
    @Singleton
    fun provideAppProfileDao(database: AppDatabase): AppProfileDAO = database.appProfileDao()
}
