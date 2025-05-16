package com.juanarton.perfprofiler.core.util

import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.source.local.room.entity.AppProfileEntity
import com.juanarton.perfprofiler.core.data.source.local.room.entity.ProfileEntity

object DataMapper {
    fun profileEntityToDomain(profileEntity: ProfileEntity): Profile =
        Profile(
            profileEntity.name,
            profileEntity.c1MaxFreq,
            profileEntity.c1MinFreq,
            profileEntity.c1Governor,
            profileEntity.c2MaxFreq,
            profileEntity.c2MinFreq,
            profileEntity.c2Governor,
            profileEntity.c3MaxFreq,
            profileEntity.c3MinFreq,
            profileEntity.c3Governor,
            profileEntity.gpuMaxFreq,
            profileEntity.gpuMinFreq,
            profileEntity.gpuGovernor,
            profileEntity.cpusOnline
        )

    fun profileDomainToEntity(profile: Profile): ProfileEntity =
        ProfileEntity(
            profile.name,
            profile.c1MaxFreq,
            profile.c1MinFreq,
            profile.c1Governor,
            profile.c2MaxFreq,
            profile.c2MinFreq,
            profile.c2Governor,
            profile.c3MaxFreq,
            profile.c3MinFreq,
            profile.c3Governor,
            profile.gpuMaxFreq,
            profile.gpuMinFreq,
            profile.gpuGovernor,
            profile.cpusOnline
        )

    fun appProfileEntityToDomain(appProfileEntity: AppProfileEntity): AppProfile =
        AppProfile(
            appProfileEntity.packageId,
            appProfileEntity.profile
        )

    fun appProfileDomainToEntity(appProfile: AppProfile): AppProfileEntity =
        AppProfileEntity(
            appProfile.packageId,
            appProfile.profile
        )
}