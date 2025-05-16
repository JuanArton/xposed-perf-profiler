package com.juanarton.perfprofiler.core.data.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(
    val name: String = "",
    val c1MaxFreq: String = "0",
    val c1MinFreq: String = "0",
    val c1Governor: String = "performance",
    val c2MaxFreq: String = "0",
    val c2MinFreq: String = "0",
    val c2Governor: String = "performance",
    val c3MaxFreq: String = "0",
    val c3MinFreq: String = "0",
    val c3Governor: String = "performance",
    val gpuMaxFreq: String = "0",
    val gpuMinFreq: String = "0",
    val gpuGovernor: String = "msm-adreno-tz",
    val cpusOnline: List<String> = listOf<String>("1", "1", "1", "1", "1", "1", "1", "1")
): Parcelable
