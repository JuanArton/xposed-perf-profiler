package com.juanarton.perfprofiler.ui.activity.profiledetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.juanarton.perfprofiler.core.data.domain.model.Profile
import com.juanarton.perfprofiler.core.data.domain.usecase.local.AppRepositoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val appRepositoryUseCase: AppRepositoryUseCase
) : ViewModel() {

    private val _cpuFolders: MutableLiveData<List<String>> = MutableLiveData()
    val cpuFolders: LiveData<List<String>> = _cpuFolders

    private val compositeDisposable = CompositeDisposable()

    fun getCpuFolders() {
        val disposable = appRepositoryUseCase.getCpuFolders()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                _cpuFolders.value = result
            }, { error ->
                error.printStackTrace()
            })

        compositeDisposable.add(disposable)
    }

    fun getCpuMaxFreq(policy: String): String {
        return appRepositoryUseCase.getCpuMaxFreq(policy).blockingGet()
    }

    fun getCpuMinFreq(policy: String): String {
        return appRepositoryUseCase.getCpuMinFreq(policy).blockingGet()
    }

    fun getCurrentCpuGovernor(policy: String): String {
        return appRepositoryUseCase.getCurrentCpuGovernor(policy).blockingGet()
    }

    fun getGpuMaxFreq(): String {
        return appRepositoryUseCase.getGpuMaxFreq().blockingGet()
    }

    fun getGpuMinFreq(): String {
        return appRepositoryUseCase.getGpuMinFreq().blockingGet()
    }

    fun getCurrentGpuGovernor(): String {
        return appRepositoryUseCase.getCurrentGpuGovernor().blockingGet()
    }

    fun getScalingAvailableFreq(policy: String): List<String> {
        return appRepositoryUseCase.getScalingAvailableFreq(policy).blockingGet()
    }

    fun getScalingAvailableGov(policy: String): List<String> {
        return appRepositoryUseCase.getScalingAvailableGov(policy).blockingGet()
    }

    fun getGpuFrequencies(): List<String> {
        return appRepositoryUseCase.getGpuFrequencies().blockingGet()
    }

    fun getGpuGovernors(): List<String> {
        return appRepositoryUseCase.getGpuGovernors().blockingGet()
    }

    fun saveProfile(profile: Profile) {
        appRepositoryUseCase.insertProfile(profile)
    }
}