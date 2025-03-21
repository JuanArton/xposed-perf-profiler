package com.juanarton.perfprofiler.core.data.repository

import android.content.pm.PackageManager
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.repository.IAppRepository
import com.juanarton.perfprofiler.core.data.source.local.LocalDataSource
import com.juanarton.perfprofiler.core.data.source.local.room.entity.AppProfileEntity
import com.juanarton.perfprofiler.core.util.DataMapper.appProfileDomainToEntity
import com.juanarton.perfprofiler.core.util.DataMapper.appProfileEntityToDomain
import com.juanarton.perfprofiler.core.util.DataMapper.profileDomainToEntity
import com.juanarton.perfprofiler.core.util.DataMapper.profileEntityToDomain
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val localDataSource: LocalDataSource
) : IAppRepository {
    override fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean): Single<List<String>> =
        Single.fromCallable {
            val userApp = localDataSource.getInstalledApps(packageManager, false)
            val systemApp = if (getSystem) {
                localDataSource.getInstalledApps(packageManager, true)
            } else {
                arrayListOf()
            }
            userApp + systemApp
        }

    override fun getCpuFolders(): Single<List<String>> =
        Single.fromCallable { localDataSource.getCpuFolders() }

    override fun getScalingAvailableFreq(policy: String): Single<List<String>> =
        Single.fromCallable { localDataSource.getScalingAvailableFreq(policy) ?: listOf() }

    override fun getScalingAvailableGov(policy: String): Single<List<String>> =
        Single.fromCallable { localDataSource.getScalingAvailableGov(policy) ?: listOf() }

    override fun getCpuMaxFreq(policy: String): Single<String> =
        Single.fromCallable { localDataSource.getCpuMaxFreq(policy) ?: "" }

    override fun getCpuMinFreq(policy: String): Single<String> =
        Single.fromCallable { localDataSource.getCpuMinFreq(policy) ?: "" }

    override fun getCurrentCpuGovernor(policy: String): Single<String> =
        Single.fromCallable { localDataSource.getCurrentCpuGovernor(policy) ?: "" }

    override fun getGpuFrequencies(): Single<List<String>> =
        Single.fromCallable { localDataSource.getGpuFrequencies() ?: listOf() }

    override fun getGpuGovernors(): Single<List<String>> =
        Single.fromCallable { localDataSource.getGpuGovernors() ?: listOf() }

    override fun getCurrentGpuGovernor(): Single<String> =
        Single.fromCallable { localDataSource.getCurrentGpuGovernor() ?: "" }

    override fun getGpuMaxFreq(): Single<String> =
        Single.fromCallable { localDataSource.getGpuMaxFreq() ?: "" }

    override fun getGpuMinFreq(): Single<String> =
        Single.fromCallable { localDataSource.getGpuMinFreq() ?: "" }

    override fun getProfile(): Single<List<Profile>> =
        Single.fromCallable { localDataSource.getProfile().map { profileEntityToDomain(it) } }

    override fun getProfileByName(name: String): Single<Profile> =
        Single.fromCallable { profileEntityToDomain(localDataSource.getProfileByName(name)) }

    override fun insertProfile(profile: Profile): Completable =
        Completable.fromAction {
            localDataSource.insertProfile(profileDomainToEntity(profile))
        }.subscribeOn(Schedulers.io())

    override fun updateProfile(profile: Profile): Completable =
        Completable.fromAction {
            localDataSource.updateProfile(profileDomainToEntity(profile))
        }.subscribeOn(Schedulers.io())

    override fun deleteProfile(profile: Profile): Completable =
        Completable.fromAction {
            localDataSource.deleteProfile(profileDomainToEntity(profile))
        }.subscribeOn(Schedulers.io())

    override fun getAppProfile(): Single<List<AppProfile>> =
        Single.fromCallable {
            localDataSource.getAppProfile().map { appProfileEntityToDomain(it) }
        }.subscribeOn(Schedulers.io())

    override fun getAppProfileByName(packageId: String): Single<AppProfile> =
        Single.fromCallable {
            val appProfile = localDataSource.getAppProfileByName(packageId) ?: AppProfileEntity()
            appProfileEntityToDomain(appProfile)
        }

    override fun insertAppProfile(appProfile: AppProfile): Completable =
        Completable.fromAction {
            localDataSource.insertAppProfile(appProfileDomainToEntity(appProfile))
        }.subscribeOn(Schedulers.io())

    override fun deleteAppProfile(appProfile: AppProfile): Completable =
        Completable.fromAction {
            localDataSource.deleteAppProfile(appProfileDomainToEntity(appProfile))
        }.subscribeOn(Schedulers.io())

    override fun deleteAppProfileByProfile(profile: String): Completable =
        Completable.fromAction {
            localDataSource.deleteAppProfileByProfile(profile)
        }.subscribeOn(Schedulers.io())

    override fun setDefaultProfile(profile: String) {
        localDataSource.setDefaultProfile(profile)
    }

    override fun getDefaultProfile(): String =
        localDataSource.getDefaultProfile()

    override fun setChargingProfile(profile: String) {
        localDataSource.setChargingProfile(profile)
    }

    override fun getChargingProfile(): String =
        localDataSource.getChargingProfile()

    override fun setOvh40Profile(profile: String) {
        localDataSource.setOvh40Profile(profile)
    }

    override fun getOvh40Profile(): String =
        localDataSource.getOvh40Profile()

    override fun setOvh42Profile(profile: String) {
        localDataSource.setOvh42Profile(profile)
    }

    override fun getOvh42Profile(): String =
        localDataSource.getOvh42Profile()

    override fun setOvh45Profile(profile: String) {
        localDataSource.setOvh45Profile(profile)
    }

    override fun getOvh45Profile(): String =
        localDataSource.getOvh45Profile()

    override fun setForceProfileActive(force: Boolean) {
        localDataSource.setForceProfileActive(force)
    }

    override fun getForceProfileActive(): Boolean =
        localDataSource.getForceProfileActive()

    override fun setForceProfile(profile: String) {
        localDataSource.setForceProfile(profile)
    }

    override fun getForceProfile(): String =
        localDataSource.getForceProfile()

    override fun setBoostProfile(profile: String) {
        localDataSource.setBoostProfile(profile)
    }

    override fun getBoostProfile(): String =
        localDataSource.getBoostProfile()
}
