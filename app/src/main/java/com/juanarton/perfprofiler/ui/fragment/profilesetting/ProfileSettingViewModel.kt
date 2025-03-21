package com.juanarton.perfprofiler.ui.fragment.profilesetting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.usecase.local.AppRepositoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class ProfileSettingViewModel @Inject constructor(
    private val appRepositoryUseCase: AppRepositoryUseCase
) : ViewModel() {

    private val _profileList = MutableLiveData<List<Profile>>()
    val profileList: LiveData<List<Profile>> = _profileList

    fun getProfile() {
        appRepositoryUseCase.getProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ profiles ->
                _profileList.value = profiles
            }, { error ->
                Log.e("ProfileSettingViewModel", "Error fetching profiles", error)
            })
            .addToDisposables()
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

    fun setForceProfile(profile: String) {
        appRepositoryUseCase.setForceProfile(profile)
    }

    fun getForceProfile(): String =
        appRepositoryUseCase.getForceProfile()

    fun setBoostProfile(profile: String) {
        Log.d("test", "work")
        appRepositoryUseCase.setBoostProfile(profile)
    }

    fun getBoostProfile(): String =
        appRepositoryUseCase.getBoostProfile()

    fun deleteProfile(profile: Profile): Completable =
        appRepositoryUseCase.deleteProfile(profile)

    fun deleteAppProfileByProfile(profile: String): Completable =
        appRepositoryUseCase.deleteAppProfileByProfile(profile)

    private val disposables = CompositeDisposable()

    private fun Disposable.addToDisposables() {
        disposables.add(this)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}