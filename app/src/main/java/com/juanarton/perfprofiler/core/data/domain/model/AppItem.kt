package com.juanarton.perfprofiler.core.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppItem(
    val name: String,
    val packageId: String
): Parcelable
