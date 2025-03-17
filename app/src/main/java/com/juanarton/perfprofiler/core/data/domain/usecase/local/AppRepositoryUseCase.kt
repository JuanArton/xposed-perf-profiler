package com.juanarton.perfprofiler.core.data.domain.usecase.local

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.source.local.room.entity.AppProfileEntity
import com.juanarton.perfprofiler.core.data.source.local.room.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

interface AppRepositoryUseCase {
    fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean): Flow<List<String>>
    fun getCpuFolders(): Flow<List<String>>
    fun getScalingAvailableFreq(policy: String): Flow<List<String>?>
    fun getScalingAvailableGov(policy: String): Flow<List<String>?>
    fun getCpuMaxFreq(policy: String): Flow<String?>
    fun getCpuMinFreq(policy: String): Flow<String?>
    fun getCurrentCpuGovernor(policy: String): Flow<String?>
    fun getGpuFrequencies(): Flow<List<String>?>
    fun getGpuGovernors(): Flow<List<String>?>
    fun getCurrentGpuGovernor(): Flow<String?>
    fun getGpuMaxFreq(): Flow<String?>
    fun getGpuMinFreq(): Flow<String?>
    fun getProfile(): Flow<List<Profile>>
    fun getProfileByName(name: String): Flow<Profile>
    fun insertProfile(profile: Profile)
    fun updateProfile(profile: Profile)
    fun deleteProfile(profile: Profile)
    fun getAppProfile(): Flow<List<AppProfile>>
    fun getAppProfileByName(packageId: String): Flow<AppProfile?>
    fun insertAppProfile(appProfile: AppProfile)
    fun deleteAppProfile(appProfile: AppProfile)
    fun deleteAppProfileByProfile(profile: String)
    fun setDefaultProfile(profile: String)
    fun getDefaultProfile(): String
    fun setChargingProfile(profile: String)
    fun getChargingProfile(): String
    fun setOvh40Profile(profile: String)
    fun getOvh40Profile(): String
    fun setOvh42Profile(profile: String)
    fun getOvh42Profile(): String
    fun setOvh45Profile(profile: String)
    fun getOvh45Profile(): String
}