package com.juanarton.perfprofiler.core.data.domain.usecase.local

import android.content.pm.PackageManager
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface AppRepositoryUseCase {
    fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean): Single<List<String>>
    fun getCpuFolders(): Single<List<String>>
    fun getScalingAvailableFreq(policy: String): Single<List<String>>
    fun getScalingAvailableGov(policy: String): Single<List<String>>
    fun getCpuMaxFreq(policy: String): Single<String>
    fun getCpuMinFreq(policy: String): Single<String>
    fun getCurrentCpuGovernor(policy: String): Single<String>
    fun getGpuFrequencies(): Single<List<String>>
    fun getGpuGovernors(): Single<List<String>>
    fun getCurrentGpuGovernor(): Single<String>
    fun getGpuMaxFreq(): Single<String>
    fun getGpuMinFreq(): Single<String>
    fun getProfile(): Single<List<Profile>>
    fun getProfileByName(name: String): Single<Profile>
    fun insertProfile(profile: Profile): Completable
    fun updateProfile(profile: Profile): Completable
    fun deleteProfile(profile: Profile): Completable
    fun getAppProfile(): Single<List<AppProfile>>
    fun getAppProfileByName(packageId: String): Single<AppProfile>
    fun insertAppProfile(appProfile: AppProfile): Completable
    fun deleteAppProfile(appProfile: AppProfile): Completable
    fun deleteAppProfileByProfile(profile: String): Completable
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
    fun setForceProfileActive(force: Boolean)
    fun getForceProfileActive(): Boolean
    fun setForceProfile(profile: String)
    fun getForceProfile(): String
    fun setBoostProfile(profile: String)
    fun getBoostProfile(): String
}