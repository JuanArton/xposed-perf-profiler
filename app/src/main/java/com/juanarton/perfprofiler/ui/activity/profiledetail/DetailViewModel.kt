package com.juanarton.perfprofiler.ui.activity.profiledetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.usecase.local.AppRepositoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val appRepositoryUseCase: AppRepositoryUseCase
): ViewModel() {
    private val _cpuFolders: MutableLiveData<List<String>> = MutableLiveData()
    val cpuFolders: LiveData<List<String>> = _cpuFolders

    fun getCpuFolders() {
        viewModelScope.launch {
            appRepositoryUseCase.getCpuFolders().collect {
                _cpuFolders.value = it
            }
        }
    }

    suspend fun getCpuMaxFreq(policy: String): String? {
        return appRepositoryUseCase.getCpuMaxFreq(policy).first()
    }

    suspend fun getCpuMinFreq(policy: String): String? {
        return appRepositoryUseCase.getCpuMinFreq(policy).first()
    }

    suspend fun getCurrentCpuGovernor(policy: String): String? {
        return appRepositoryUseCase.getCurrentCpuGovernor(policy).first()
    }

    suspend fun getGpuMaxFreq(): String? {
        return appRepositoryUseCase.getGpuMaxFreq().first()
    }

    suspend fun getGpuMinFreq(): String? {
        return appRepositoryUseCase.getGpuMinFreq().first()
    }

    suspend fun getCurrentGpuGovernor(): String? {
        return appRepositoryUseCase.getCurrentGpuGovernor().first()
    }

    suspend fun getScalingAvailableFreq(policy: String): List<String>? {
        return appRepositoryUseCase.getScalingAvailableFreq(policy).first()
    }

    suspend fun getScalingAvailableGov(policy: String): List<String>? {
        return appRepositoryUseCase.getScalingAvailableGov(policy).first()
    }

    suspend fun getGpuFrequencies(): List<String>? {
        return appRepositoryUseCase.getGpuFrequencies().first()
    }

    suspend fun getGpuGovernors(): List<String>? {
        return appRepositoryUseCase.getGpuGovernors().first()
    }

    fun saveProfile(profile: Profile) {
        appRepositoryUseCase.insertProfile(profile)
    }
}