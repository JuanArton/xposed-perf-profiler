package com.juanarton.perfprofiler.core.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.juanarton.perfprofiler.core.data.source.local.room.dao.AppProfileDAO
import com.juanarton.perfprofiler.core.data.source.local.room.dao.ProfileDAO
import com.juanarton.perfprofiler.core.data.source.local.room.entity.AppProfileEntity
import com.juanarton.perfprofiler.core.data.source.local.room.entity.ProfileEntity

@Database(entities = [
    ProfileEntity::class,
    AppProfileEntity::class
], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDAO
    abstract fun appProfileDao(): AppProfileDAO
}