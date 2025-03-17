package com.juanarton.perfprofiler.core.data.source.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "c1MaxFreq")
    val c1MaxFreq: String = "0",

    @ColumnInfo(name = "c1MinFreq")
    val c1MinFreq: String = "0",

    @ColumnInfo(name = "c1Governor")
    val c1Governor: String = "performance",

    @ColumnInfo(name = "c2MaxFreq")
    val c2MaxFreq: String = "0",

    @ColumnInfo(name = "c2MinFreq")
    val c2MinFreq: String = "0",

    @ColumnInfo(name = "c2Governor")
    val c2Governor: String = "performance",

    @ColumnInfo(name = "c3MaxFreq")
    val c3MaxFreq: String = "0",

    @ColumnInfo(name = "c3MinFreq")
    val c3MinFreq: String = "0",

    @ColumnInfo(name = "c3Governor")
    val c3Governor: String = "performance",

    @ColumnInfo(name = "gpuMaxFreq")
    val gpuMaxFreq: String = "0",

    @ColumnInfo(name = "gpuMinFreq")
    val gpuMinFreq: String = "0",

    @ColumnInfo(name = "gpuGovernor")
    val gpuGovernor: String = "msm-adreno-tz",
)
