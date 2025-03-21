package com.juanarton.perfprofiler.service.tile

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.data.domain.repository.IAppRepository
import com.juanarton.perfprofiler.core.util.IOUtils.writeToFile
import com.juanarton.perfprofiler.core.util.Path
import com.juanarton.perfprofiler.service.perfservice.Action
import com.juanarton.perfprofiler.service.perfservice.PerfProfilerService
import com.juanarton.perfprofiler.service.perfservice.PerfProfilerService.Companion.currentAppliedProfile
import com.juanarton.perfprofiler.service.perfservice.PerfProfilerService.Companion.lastPackageIntent
import com.juanarton.perfprofiler.service.perfservice.PerfProfilerService.Companion.lastTopApp
import com.juanarton.perfprofiler.service.perfservice.ServiceState
import com.juanarton.perfprofiler.service.perfservice.getServiceState
import dagger.hilt.android.AndroidEntryPoint
import de.robv.android.xposed.XposedBridge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TileProfileService : TileService() {

    @Inject
    lateinit var iAppRepository: IAppRepository
    private val disposable = CompositeDisposable()
    private var applyDisposableRef: Disposable? = null

    override fun onStartListening() {
        super.onStartListening()
        qsTile.icon = Icon.createWithResource(this, R.drawable.outline_bolt_24)

        qsTile.state = if (iAppRepository.getForceProfileActive()) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()

        qsTile.state = if (qsTile.state == Tile.STATE_ACTIVE) {
            iAppRepository.setForceProfileActive(false)
            lastPackageIntent = ""
            val packageId = lastTopApp
            lastTopApp = ""
            currentAppliedProfile = ""
            startPerfProfilerService(Action.STOP, packageId)
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                startPerfProfilerService(Action.CHANGE_PROFILE, packageId)
            }
            Tile.STATE_INACTIVE
        } else {
            iAppRepository.setForceProfileActive(true)
            applyProfile(iAppRepository.getForceProfile())
            Tile.STATE_ACTIVE
        }
        qsTile.updateTile()
    }

    private fun startPerfProfilerService(action: Action, packageId: String) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Action.STOP) return
        Intent(this, PerfProfilerService::class.java).also {
            it.action = action.name
            it.putExtra("PACKAGE_ID", packageId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("BatteryMonitorService", "Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            Log.d("BatteryMonitorService", "Starting the service in < 26 Mode")
            startService(it)
        }
    }

    @SuppressLint("CheckResult")
    private fun applyProfile(profileString: String) {
        applyDisposableRef?.dispose()
        applyDisposableRef = iAppRepository.getProfileByName(profileString)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ profile ->
                iAppRepository.getCpuFolders()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ cpuFolder ->
                        val cpuTasks = cpuFolder.mapIndexed { index, policy ->
                            Completable.fromCallable {
                                val maxFreq = listOf(profile.c1MaxFreq, profile.c2MaxFreq, profile.c3MaxFreq)
                                    .getOrElse(index) { profile.c1MaxFreq }
                                val minFreq = listOf(profile.c1MinFreq, profile.c2MinFreq, profile.c3MinFreq)
                                    .getOrElse(index) { profile.c1MinFreq }
                                val gov = listOf(profile.c1Governor, profile.c2Governor, profile.c3Governor)
                                    .getOrElse(index) { profile.c1Governor }

                                writeToFile("${Path.CPU_PATH}/$policy", Path.SCALING_MAX_FREQ, maxFreq, false)
                                writeToFile("${Path.CPU_PATH}/$policy", Path.SCALING_MIN_FREQ, minFreq, false)
                                writeToFile("${Path.CPU_PATH}/$policy", Path.SCALING_GOVERNOR, gov, false)
                            }.subscribeOn(Schedulers.io())
                        }

                        val gpuTasks = listOf(
                            Completable.fromCallable { writeToFile(Path.GPU_PATH, Path.GPU_MAX_FREQ, profile.gpuMaxFreq, true) }
                                .subscribeOn(Schedulers.io()),
                            Completable.fromCallable { writeToFile(Path.GPU_PATH, Path.GPU_MIN_FREQ, profile.gpuMinFreq, true) }
                                .subscribeOn(Schedulers.io()),
                            Completable.fromCallable { writeToFile(Path.GPU_PATH, Path.GPU_CURRENT_GOV, profile.gpuGovernor, true) }
                                .subscribeOn(Schedulers.io())
                        )

                        disposable.add(
                            Completable.mergeArray(*(cpuTasks + gpuTasks).toTypedArray())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doFinally {
                                    disposable.remove(applyDisposableRef!!)
                                }
                                .subscribe(
                                    { Log.d("PerfProfilerService", "All CPU and GPU settings applied successfully") },
                                    { error -> Log.e("PerfProfilerService", "Error applying settings: ${error.message}") },
                                )
                        )
                    }, { error ->
                        Log.e("PerfProfilerService", "Error getting CPU folders: ${error.message}")
                    })
            }, { error ->
                Log.e("PerfProfilerService", "Error getting profile: ${error.message}")
            })

        disposable.add(applyDisposableRef!!)
    }
}