package com.juanarton.perfprofiler.ui.fragment.profilesetting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.usecase.local.AppRepositoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSettingViewModel @Inject constructor(
    private val appRepositoryUseCase: AppRepositoryUseCase
): ViewModel() {

    private val _profileList: MutableLiveData<List<Profile>> = MutableLiveData()
    val profileList: LiveData<List<Profile>> = _profileList

    fun getProfile() {
        viewModelScope.launch {
            appRepositoryUseCase.getProfile().collect {
                _profileList.value = it
            }
        }
    }

    fun setChargingProfile(profile: String) {
        appRepositoryUseCase.setChargingProfile(profile)
    }

    fun getChargingProfile(): String =
        appRepositoryUseCase.getChargingProfile()

    fun setOvh40Profile(profile: String) {
        appRepositoryUseCase.setOvh40Profile(profile)
    }

    fun getOvh40Profile(): String =
        appRepositoryUseCase.getOvh40Profile()

    fun setOvh42Profile(profile: String) {
        appRepositoryUseCase.setOvh42Profile(profile)
    }

    fun getOvh42Profile(): String =
        appRepositoryUseCase.getOvh42Profile()

    fun setOvh45Profile(profile: String) {
        appRepositoryUseCase.setOvh45Profile(profile)
    }

    fun getOvh45Profile(): String =
        appRepositoryUseCase.getOvh45Profile()

    fun deleteProfile(profile: Profile) {
        appRepositoryUseCase.deleteProfile(profile)
    }

    fun deleteAppProfileByProfile(profile: String) {
        appRepositoryUseCase.deleteAppProfileByProfile(profile)
    }
}