package com.juanarton.perfprofiler.ui.fragment.applist

import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.usecase.local.AppRepositoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepositoryUseCase: AppRepositoryUseCase
): ViewModel() {
    private val _appList = MutableLiveData<List<String>>()
    val appList: LiveData<List<String>> = _appList

    private val _profileList = MutableLiveData<List<Profile>>()
    val profileList: LiveData<List<Profile>> = _profileList

    private val _appProfileList = MutableLiveData<List<AppProfile>>()
    val appProfileList: LiveData<List<AppProfile>> = _appProfileList

    fun getInstalledApps(packageManager: PackageManager, getSystem: Boolean) {
        appRepositoryUseCase.getInstalledApps(packageManager, getSystem)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ apps ->
                _appList.value = apps
            }, { error ->
                Log.e("AppListViewModel", "Error fetching installed apps", error)
            })
            .addToDisposables()
    }

    fun getAppProfile() {
        appRepositoryUseCase.getAppProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ profiles ->
                _appProfileList.value = profiles
            }, { error ->
                Log.e("AppListViewModel", "Error fetching app profiles", error)
            })
            .addToDisposables()
    }

    fun getProfile() {
        appRepositoryUseCase.getProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ profiles ->
                _profileList.value = profiles
            }, { error ->
                Log.e("AppListViewModel", "Error fetching profiles", error)
            })
            .addToDisposables()
    }

    fun insertAppProfile(appProfile: AppProfile) {
        appRepositoryUseCase.insertAppProfile(appProfile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("AppListViewModel", "App profile inserted successfully")
            }, { error ->
                Log.e("AppListViewModel", "Error inserting app profile", error)
            })
            .addToDisposables()
    }

    fun deleteAppProfile(appProfile: AppProfile) {
        appRepositoryUseCase.deleteAppProfile(appProfile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d("AppListViewModel", "App profile deleted successfully")
            }, { error ->
                Log.e("AppListViewModel", "Error deleting app profile", error)
            })
            .addToDisposables()
    }

    fun getDefaultProfile(): String = appRepositoryUseCase.getDefaultProfile()

    fun setDefaultProfile(profile: String) {
        appRepositoryUseCase.setDefaultProfile(profile)
    }

    private val disposables = CompositeDisposable()

    private fun Disposable.addToDisposables() {
        disposables.add(this)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}