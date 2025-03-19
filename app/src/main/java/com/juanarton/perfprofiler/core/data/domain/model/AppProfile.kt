package com.juanarton.perfprofiler.core.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppProfile(
    val packageId: String = "",
    val profile: String = "Not Set",
): Parcelable
