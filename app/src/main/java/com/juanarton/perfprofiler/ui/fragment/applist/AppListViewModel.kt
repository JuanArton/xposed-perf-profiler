package com.juanarton.perfprofiler.ui.fragment.applist

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.usecase.local.AppRepositoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepositoryUseCase: AppRepositoryUseCase
): ViewModel() {
    private val _appList: MutableLiveData<List<String>> = MutableLiveData()
    val appList: LiveData<List<String>> = _appList

    private val _profileList: MutableLiveData<List<Profile>> = MutableLiveData()
    val profileList: LiveData<List<Profile>> = _profileList

    private val _appProfileList: MutableLiveData<List<AppProfile>> = MutableLiveData()
    val appProfileList: LiveData<List<AppProfile>> = _appProfileList

    fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean) {
        viewModelScope.launch {
            appRepositoryUseCase.getInstalledApps(packageManager, getSystem).collect {
                _appList.value = it
            }
        }
    }

    fun getAppProfile() {
        viewModelScope.launch {
            appRepositoryUseCase.getAppProfile().collect {
                _appProfileList.value = it
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            appRepositoryUseCase.getProfile().collect {
                _profileList.value = it
            }
        }
    }

    fun insertAppProfile(appProfile: AppProfile) {
        appRepositoryUseCase.insertAppProfile(appProfile)
    }

    fun deleteAppProfile(appProfile: AppProfile) {
        appRepositoryUseCase.deleteAppProfile(appProfile)
    }

    fun getDefaultProfile(): String = appRepositoryUseCase.getDefaultProfile()

    fun setDefaultProfile(profile: String) {
        appRepositoryUseCase.setDefaultProfile(profile)
    }
}