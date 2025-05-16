package com.juanarton.perfprofiler.core.data.domain.usecase.local

import android.content.pm.PackageManager
import android.util.Log
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.repository.IAppRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val iAppRepository: IAppRepository
) : AppRepositoryUseCase {
    override fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean): Single<List<String>> =
        iAppRepository.getInstalledApps(packageManager, getSystem)

    override fun getCpuFolders(): Single<List<String>> =
        iAppRepository.getCpuFolders()

    override fun getScalingAvailableFreq(policy: String): Single<List<String>> =
        iAppRepository.getScalingAvailableFreq(policy)

    override fun getScalingAvailableGov(policy: String): Single<List<String>> =
        iAppRepository.getScalingAvailableGov(policy)

    override fun getCpuMaxFreq(policy: String): Single<String> =
        iAppRepository.getCpuMaxFreq(policy)

    override fun getCpuMinFreq(policy: String): Single<String> =
        iAppRepository.getCpuMinFreq(policy)

    override fun getCurrentCpuGovernor(policy: String): Single<String> =
        iAppRepository.getCurrentCpuGovernor(policy)

    override fun getGpuFrequencies(): Single<List<String>> =
        iAppRepository.getGpuFrequencies()

    override fun getGpuGovernors(): Single<List<String>> =
        iAppRepository.getGpuGovernors()

    override fun getCurrentGpuGovernor(): Single<String> =
        iAppRepository.getCurrentGpuGovernor()

    override fun getGpuMaxFreq(): Single<String> =
        iAppRepository.getGpuMaxFreq()

    override fun getGpuMinFreq(): Single<String> =
        iAppRepository.getGpuMinFreq()

    override fun getCPUSOnline(): Single<List<String>> =
        iAppRepository.getCPUSOnline()

    override fun getProfile(): Single<List<Profile>> =
        iAppRepository.getProfile()

    override fun getProfileByName(name: String): Single<Profile> =
        iAppRepository.getProfileByName(name)

    override fun insertProfile(profile: Profile): Completable {
        return iAppRepository.insertProfile(profile)
    }

    override fun updateProfile(profile: Profile): Completable {
        return iAppRepository.updateProfile(profile)
    }

    override fun deleteProfile(profile: Profile): Completable {
        return iAppRepository.deleteProfile(profile)
    }

    override fun getAppProfile(): Single<List<AppProfile>> =
        iAppRepository.getAppProfile()

    override fun getAppProfileByName(packageId: String): Single<AppProfile> =
        iAppRepository.getAppProfileByName(packageId)

    override fun insertAppProfile(appProfile: AppProfile) =
        iAppRepository.insertAppProfile(appProfile)

    override fun deleteAppProfile(appProfile: AppProfile) =
        iAppRepository.deleteAppProfile(appProfile)

    override fun deleteAppProfileByProfile(profile: String): Completable {
        return iAppRepository.deleteAppProfileByProfile(profile)
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

    override fun setForceProfileActive(force: Boolean) {
        iAppRepository.setForceProfileActive(force)
    }

    override fun getForceProfileActive(): Boolean =
        iAppRepository.getForceProfileActive()

    override fun setForceProfile(profile: String) {
        iAppRepository.setForceProfile(profile)
    }

    override fun getForceProfile(): String =
        iAppRepository.getForceProfile()

    override fun setBoostProfile(profile: String) {
        iAppRepository.setBoostProfile(profile)
    }

    override fun getBoostProfile(): String =
        iAppRepository.getBoostProfile()
}
