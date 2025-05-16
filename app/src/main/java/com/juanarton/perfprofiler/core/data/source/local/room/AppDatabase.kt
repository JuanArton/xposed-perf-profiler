package com.juanarton.perfprofiler.core.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.juanarton.perfprofiler.core.data.source.local.room.dao.AppProfileDAO
import com.juanarton.perfprofiler.core.data.source.local.room.dao.ProfileDAO
import com.juanarton.perfprofiler.core.data.source.local.room.entity.AppProfileEntity
import com.juanarton.perfprofiler.core.data.source.local.room.entity.ProfileEntity
import com.juanarton.perfprofiler.core.util.ListConverter

@Database(entities = [
    ProfileEntity::class,
    AppProfileEntity::class
], version = 2, exportSchema = false)
@TypeConverters(ListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDAO
    abstract fun appProfileDao(): AppProfileDAO
}