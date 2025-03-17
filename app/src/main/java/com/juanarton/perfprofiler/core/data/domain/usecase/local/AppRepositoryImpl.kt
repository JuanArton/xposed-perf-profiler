package com.juanarton.perfprofiler.core.data.domain.usecase.local

import android.content.pm.PackageManager
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.repository.IAppRepository
import com.juanarton.perfprofiler.core.data.source.local.room.entity.AppProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val iAppRepository: IAppRepository
): AppRepositoryUseCase {
    override fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean): Flow<List<String>> =
        iAppRepository.getInstalledApps(packageManager, getSystem)

    override fun getCpuFolders(): Flow<List<String>> =
        iAppRepository.getCpuFolders()

    override fun getScalingAvailableFreq(policy: String): Flow<List<String>?> =
        iAppRepository.getScalingAvailableFreq(policy)

    override fun getScalingAvailableGov(policy: String): Flow<List<String>?> =
        iAppRepository.getScalingAvailableGov(policy)

    override fun getCpuMaxFreq(policy: String): Flow<String?> =
        iAppRepository.getCpuMaxFreq(policy)

    override fun getCpuMinFreq(policy: String): Flow<String?> =
        iAppRepository.getCpuMinFreq(policy)

    override fun getCurrentCpuGovernor(policy: String): Flow<String?> =
        iAppRepository.getCurrentCpuGovernor(policy)

    override fun getGpuFrequencies(): Flow<List<String>?> =
        iAppRepository.getGpuFrequencies()

    override fun getGpuGovernors(): Flow<List<String>?> =
        iAppRepository.getGpuGovernors()

    override fun getCurrentGpuGovernor(): Flow<String?> =
        iAppRepository.getCurrentGpuGovernor()

    override fun getGpuMaxFreq(): Flow<String?> =
        iAppRepository.getGpuMaxFreq()

    override fun getGpuMinFreq(): Flow<String?> =
        iAppRepository.getGpuMinFreq()

    override fun getProfile(): Flow<List<Profile>> =
        iAppRepository.getProfile()

    override fun getProfileByName(name: String): Flow<Profile> =
        iAppRepository.getProfileByName(name)

    override fun insertProfile(profile: Profile) {
        iAppRepository.insertProfile(profile)
    }

    override fun updateProfile(profile: Profile) {
        iAppRepository.updateProfile(profile)
    }

    override fun deleteProfile(profile: Profile) {
        iAppRepository.deleteProfile(profile)
    }

    override fun getAppProfile(): Flow<List<AppProfile>> =
        iAppRepository.getAppProfile()

    override fun getAppProfileByName(packageId: String): Flow<AppProfile?> =
        iAppRepository.getAppProfileByName(packageId)

    override fun insertAppProfile(appProfile: AppProfile) =
        iAppRepository.insertAppProfile(appProfile)

    override fun deleteAppProfile(appProfile: AppProfile) =
        iAppRepository.deleteAppProfile(appProfile)

    override fun deleteAppProfileByProfile(profile: String) {
        iAppRepository.deleteAppProfileByProfile(profile)
    }

    override fun setDefaultProfile(profile: String) {
        iAppRepository.setDefaultProfile(profile)
    }

    override fun getDefaultProfile(): String =
        iAppRepository.getDefaultProfile()

    override fun setChargingProfile(profile: String) {
        iAppRepository.setChargingProfile(profile)
    }

    override fun getChargingProfile(): String =
        iAppRepository.getChargingProfile()

    override fun setOvh40Profile(profile: String) {
        iAppRepository.setOvh40Profile(profile)
    }

    override fun getOvh40Profile(): String =
        iAppRepository.getOvh40Profile()

    override fun setOvh42Profile(profile: String) {
        iAppRepository.setOvh42Profile(profile)
    }

    override fun getOvh42Profile(): String =
        iAppRepository.getOvh42Profile()

    override fun setOvh45Profile(profile: String) {
        iAppRepository.setOvh45Profile(profile)
    }

    override fun getOvh45Profile(): String =
        iAppRepository.getOvh45Profile()
}