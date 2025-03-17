package com.juanarton.perfprofiler.core.data.source.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juanarton.perfprofiler.core.data.source.local.room.entity.ProfileEntity

@Dao
interface ProfileDAO {
    @Query("SELECT * FROM profile")
    fun getProfile(): List<ProfileEntity>

    @Query("SELECT * FROM profile WHERE name = :name")
    fun getProfileByName(name: String): ProfileEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ProfileEntity::class)
    fun insertProfile(profile: ProfileEntity)

    @Delete
    fun deleteProfile(profile: ProfileEntity)

    @Update
    fun updateProfile(profile: ProfileEntity)
}