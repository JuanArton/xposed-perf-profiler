package com.juanarton.perfprofiler.core.data.source.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juanarton.perfprofiler.core.data.source.local.room.entity.AppProfileEntity

@Dao
interface AppProfileDAO {
    @Query("SELECT * FROM appprofile")
    fun getAppProfile(): List<AppProfileEntity>

    @Query("SELECT * FROM appprofile WHERE packageId = :packageId")
    fun getAppProfileByName(packageId: String): AppProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = AppProfileEntity::class)
    fun insertAppProfile(appProfile: AppProfileEntity)

    @Query("DELETE FROM appprofile WHERE profile = :profile")
    fun deleteAppProfileByProfile(profile: String)

    @Delete
    fun deleteAppProfile(appProfile: AppProfileEntity)
}