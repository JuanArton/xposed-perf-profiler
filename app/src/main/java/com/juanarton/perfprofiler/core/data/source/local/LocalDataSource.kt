package com.juanarton.perfprofiler.core.data.source.local

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.juanarton.perfprofiler.core.data.source.local.room.dao.AppProfileDAO
import com.juanarton.perfprofiler.core.data.source.local.room.dao.ProfileDAO
import com.juanarton.perfprofiler.core.data.source.local.room.entity.AppProfileEntity
import com.juanarton.perfprofiler.core.data.source.local.room.entity.ProfileEntity
import com.juanarton.perfprofiler.core.util.IOUtils.readFileAsList
import com.juanarton.perfprofiler.core.util.IOUtils.readFileAsString
import com.juanarton.perfprofiler.core.util.Path
import com.juanarton.perfprofiler.core.util.Path.CPU_PATH
import com.juanarton.perfprofiler.core.util.Path.GPU_AVAILABLE_FREQ
import com.juanarton.perfprofiler.core.util.Path.GPU_AVAILABLE_GOV
import com.juanarton.perfprofiler.core.util.Path.GPU_CURRENT_GOV
import com.juanarton.perfprofiler.core.util.Path.GPU_MAX_FREQ
import com.juanarton.perfprofiler.core.util.Path.GPU_MIN_FREQ
import com.juanarton.perfprofiler.core.util.Path.GPU_PATH
import com.juanarton.perfprofiler.core.util.Path.SCALING_BOOST_FREQ
import com.juanarton.perfprofiler.core.util.Path.SCALING_AVAILABLE_GOV
import com.juanarton.perfprofiler.core.util.Path.SCALING_MAX_FREQ
import com.juanarton.perfprofiler.core.util.Path.SCALING_MIN_FREQ
import com.juanarton.perfprofiler.core.util.Path.SCALING_GOVERNOR
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.github.kyuubiran.ezxhelper.utils.Log

@Singleton
class LocalDataSource @Inject constructor(
    context: Context,
    private val profileDAO: ProfileDAO,
    private val appProfileDAO: AppProfileDAO
) {
    private val sharedPref = context.getSharedPreferences("APP_SETTING", Context.MODE_PRIVATE)

    fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean): List<String> {
        val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        return (
            apps.filter {
                    (it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) == if (getSystem) 1 else 0
                }
                .map { it.packageName }
        )
    }

    fun getCpuFolders(): List<String> {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "ls -d $CPU_PATH/policy[0-9]*"))
            val reader = process.inputStream.bufferedReader()
            val output = reader.readLines()
            reader.close()
            process.waitFor()
            output.map { it.substringAfterLast("/") }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getScalingAvailableFreq(policy: String): List<String>? {
        val normalFreq = readFileAsList("$CPU_PATH/$policy", Path.SCALING_AVAILABLE_FREQ)?.filter { it.isNotEmpty() }
        val boostFreq = getCpuBoostFreq(policy)?.filter { it.isNotEmpty() }
        return normalFreq.orEmpty() + boostFreq.orEmpty()
    }

    private fun getCpuBoostFreq(policy: String): List<String>? =
        readFileAsList("${CPU_PATH}/$policy", SCALING_BOOST_FREQ)

    fun getScalingAvailableGov(policy: String): List<String>? =
        readFileAsList("${CPU_PATH}/$policy", SCALING_AVAILABLE_GOV)

    fun getCpuMaxFreq(policy: String): String? =
        readFileAsString("${CPU_PATH}/$policy", SCALING_MAX_FREQ)

    fun getCpuMinFreq(policy: String): String? =
        readFileAsString("${CPU_PATH}/$policy", SCALING_MIN_FREQ)

    fun getCurrentCpuGovernor(policy: String): String? =
        readFileAsString("${CPU_PATH}/$policy", SCALING_GOVERNOR)

    fun getGpuFrequencies(): List<String>? =
        readFileAsList(GPU_PATH, GPU_AVAILABLE_FREQ)

    fun getGpuGovernors(): List<String>? =
        readFileAsList(GPU_PATH, GPU_AVAILABLE_GOV)

    fun getCurrentGpuGovernor(): String? =
        readFileAsString(GPU_PATH, GPU_CURRENT_GOV)

    fun getGpuMaxFreq(): String? =
        readFileAsString(GPU_PATH, GPU_MAX_FREQ)

    fun getGpuMinFreq(): String? =
        readFileAsString(GPU_PATH, GPU_MIN_FREQ)

    fun getProfile(): List<ProfileEntity> = profileDAO.getProfile()

    fun getProfileByName(name: String): ProfileEntity = profileDAO.getProfileByName(name)

    fun insertProfile(profile: ProfileEntity) {
        profileDAO.insertProfile(profile)
    }

    fun updateProfile(profile: ProfileEntity) {
        profileDAO.updateProfile(profile)
    }

    fun deleteProfile(profile: ProfileEntity) {
        profileDAO.deleteProfile(profile)
    }

    fun getAppProfile(): List<AppProfileEntity> = appProfileDAO.getAppProfile()

    fun getAppProfileByName(packageId: String) = appProfileDAO.getAppProfileByName(packageId)

    fun insertAppProfile(appProfile: AppProfileEntity) {
        appProfileDAO.insertAppProfile(appProfile)
    }

    fun deleteAppProfile(appProfile: AppProfileEntity) {
        appProfileDAO.deleteAppProfile(appProfile)
    }

    fun deleteAppProfileByProfile(profile: String) {
        appProfileDAO.deleteAppProfileByProfile(profile)
    }

    fun setDefaultProfile(profile: String) {
        sharedPref.edit { putString("DEFAULT", profile) }
    }

    fun getDefaultProfile(): String =
        sharedPref.getString("DEFAULT", "") ?: ""

    fun setChargingProfile(profile: String) {
        sharedPref.edit { putString("CHARGING", profile) }
    }

    fun getChargingProfile(): String =
        sharedPref.getString("CHARGING", "") ?: ""

    fun setOvh40Profile(profile: String) {
        sharedPref.edit { putString("OVH40", profile) }
    }

    fun getOvh40Profile(): String =
        sharedPref.getString("OVH40", "") ?: ""

    fun setOvh42Profile(profile: String) {
        sharedPref.edit { putString("OVH42", profile) }
    }

    fun getOvh42Profile(): String =
        sharedPref.getString("OVH42", "") ?: ""

    fun setOvh45Profile(profile: String) {
        sharedPref.edit { putString("OVH45", profile) }
    }

    fun getOvh45Profile(): String =
        sharedPref.getString("OVH45", "") ?: ""

    fun setForceProfileActive(force: Boolean) {
        sharedPref.edit { putBoolean("FORCE", force) }
    }

    fun getForceProfileActive(): Boolean =
        sharedPref.getBoolean("FORCE", false)

    fun setForceProfile(profile: String) {
        sharedPref.edit { putString("FORCE_PROFILE", profile) }
    }

    fun getForceProfile(): String =
        sharedPref.getString("FORCE_PROFILE", "") ?: ""

    fun setBoostProfile(profile: String) {
        sharedPref.edit { putString("BOOST", profile) }
    }

    fun getBoostProfile(): String =
        sharedPref.getString("BOOST", "") ?: ""
}