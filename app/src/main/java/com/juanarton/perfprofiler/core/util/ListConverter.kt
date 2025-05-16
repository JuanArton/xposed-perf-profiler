package com.juanarton.perfprofiler.core.util

import androidx.room.TypeConverter

class ListConverter {
    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return list.joinToString(separator = ",")
    }

    @TypeConverter
    fun fromStringToList(data: String): List<String> {
        return if (data.isEmpty()) emptyList() else data.split(",")
    }
}