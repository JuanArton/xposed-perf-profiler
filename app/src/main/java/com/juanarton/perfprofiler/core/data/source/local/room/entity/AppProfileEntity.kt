package com.juanarton.perfprofiler.core.data.source.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appprofile")
data class AppProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "packageId")
    val packageId: String,

    @ColumnInfo(name = "profile")
    val profile: String = "Not Set",
)
