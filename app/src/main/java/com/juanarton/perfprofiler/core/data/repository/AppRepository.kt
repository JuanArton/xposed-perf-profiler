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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val localDataSource: LocalDataSource
): IAppRepository {
    override fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean): Flow<List<String>> =
        flow {
            val userApp = localDataSource.getInstalledApps(packageManager, false)

            val systemApp = if(getSystem) {
                localDataSource.getInstalledApps(packageManager, true)
            } else {
                arrayListOf()
            }

            emit(userApp+systemApp)
        }

    override fun getCpuFolders(): Flow<List<String>> =
        flow {
            emit(localDataSource.getCpuFolders())
        }.flowOn(Dispatchers.IO)

    override fun getScalingAvailableFreq(policy: String): Flow<List<String>?> =
        flow {
            emit(localDataSource.getScalingAvailableFreq(policy))
        }.flowOn(Dispatchers.IO)

    override fun getScalingAvailableGov(policy: String): Flow<List<String>?> =
        flow {
            emit(localDataSource.getScalingAvailableGov(policy))
        }.flowOn(Dispatchers.IO)

    override fun getCpuMaxFreq(policy: String): Flow<String?> =
        flow {
            emit(localDataSource.getCpuMaxFreq(policy))
        }.flowOn(Dispatchers.IO)

    override fun getCpuMinFreq(policy: String): Flow<String?> =
        flow {
            emit(localDataSource.getCpuMinFreq(policy))
    }.flowOn(Dispatchers.IO)

    override fun getCurrentCpuGovernor(policy: String): Flow<String?> =
        flow {
            emit(localDataSource.getCurrentCpuGovernor(policy))
        }.flowOn(Dispatchers.IO)

    override fun getGpuFrequencies(): Flow<List<String>?> =
        flow {
            emit(localDataSource.getGpuFrequencies())
        }.flowOn(Dispatchers.IO)

    override fun getGpuGovernors(): Flow<List<String>?> =
        flow {
            emit(localDataSource.getGpuGovernors())
        }.flowOn(Dispatchers.IO)

    override fun getCurrentGpuGovernor(): Flow<String?> =
        flow {
            emit(localDataSource.getCurrentGpuGovernor())
        }.flowOn(Dispatchers.IO)

    override fun getGpuMaxFreq(): Flow<String?> =
        flow {
            emit(localDataSource.getGpuMaxFreq())
        }.flowOn(Dispatchers.IO)

    override fun getGpuMinFreq(): Flow<String?> =
        flow {
            emit(localDataSource.getGpuMinFreq())
        }.flowOn(Dispatchers.IO)

    override fun getProfile(): Flow<List<Profile>> =
        flow {
            emit(
                localDataSource.getProfile().map {
                    profileEntityToDomain(it)
                }
            )
        }.flowOn(Dispatchers.IO)

    override fun getProfileByName(name: String): Flow<Profile> =
        flow {
            emit(
                profileEntityToDomain(localDataSource.getProfileByName(name))
            )
        }.flowOn(Dispatchers.IO)

    override fun insertProfile(profile: Profile) {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.insertProfile(profileDomainToEntity(profile))
        }
    }

    override fun updateProfile(profile: Profile) {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.updateProfile(profileDomainToEntity(profile))
        }
    }

    override fun deleteProfile(profile: Profile) {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.deleteProfile(profileDomainToEntity(profile))
        }
    }

    override fun getAppProfile(): Flow<List<AppProfile>> =
        flow {
            emit(
                localDataSource.getAppProfile().map {
                    appProfileEntityToDomain(it)
                }
            )
        }.flowOn(Dispatchers.IO)

    override fun getAppProfileByName(packageId: String): Flow<AppProfile?> =
        flow {
            emit(
                localDataSource.getAppProfileByName(packageId)?.let { appProfileEntityToDomain(it) }
            )
        }.flowOn(Dispatchers.IO)

    override fun insertAppProfile(appProfile: AppProfile) {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.insertAppProfile(appProfileDomainToEntity(appProfile))
        }
    }

    override fun deleteAppProfile(appProfile: AppProfile) {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.deleteAppProfile(appProfileDomainToEntity(appProfile))
        }
    }

    override fun deleteAppProfileByProfile(profile: String) {
        CoroutineScope(Dispatchers.IO).launch {
            localDataSource.deleteAppProfileByProfile(profile)
        }
    }

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
}