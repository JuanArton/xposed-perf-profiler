package com.juanarton.perfprofiler.service.perfservice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import autodispose2.autoDispose
import com.github.kyuubiran.ezxhelper.BuildConfig
import com.github.kyuubiran.ezxhelper.utils.runOnMainThread
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.repository.IAppRepository
import com.juanarton.perfprofiler.core.util.IOUtils.writeToFile
import com.juanarton.perfprofiler.core.util.Path
import com.juanarton.perfprofiler.core.util.Path.BATTERY_UEVENT
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class PerfProfilerService : LifecycleService() {
    private var isServiceStarted = false

    @Inject
    lateinit var iAppRepository: IAppRepository
    private var isCharging = false
    private var temperature = 0
    private var isOvh = false
    private var monChargingExec = false
    private var monTempExec = false
    private var lastProfile: AppProfile? = null
    private var isChanging = false
    private var isBoosting = false
    private var isFirstBoost = true

    private val disposable = CompositeDisposable()
    private var profileDisposableRef: Disposable? = null
    private var applyDisposableRef: Disposable? = null
    private var pollingDisposableRef: Disposable? = null

    companion object {
        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10)
            )
        }
        var lastTopApp = ""
        var lastPackageIntent = ""
        var currentAppliedProfile = ""
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent != null) {
            when (intent.action) {
                Action.START.name -> startService()
                Action.STOP.name -> stopService()
                Action.CHANGE_PROFILE.name -> {
                    val packageId = intent.getStringExtra("PACKAGE_ID")
                    packageId?.let {
                        if (lastPackageIntent != it) {
                            if (!isServiceStarted) startService()
                            changeProfile(packageId)
                            lastPackageIntent = packageId
                        }
                    }
                }
                Action.BOOST.name -> {
                    if (isFirstBoost) {
                        isFirstBoost = false
                        if (!isServiceStarted) startService()
                        boostPerf()

                        lifecycleScope.launch {
                            delay(1000)
                            isFirstBoost = true
                        }
                    }
                }
                else -> Log.d("PerfProfilerService", "No action in received intent")
            }
        } else {
            Log.d("PerfProfilerService", "Null intent")
        }
        return START_STICKY
    }

    private fun changeProfile(packageId: String) {
        checkOvh()
        if (packageId != lastTopApp && !isChanging) {
            isChanging = true
            lastTopApp = packageId

            profileDisposableRef?.dispose()

            profileDisposableRef = iAppRepository.getAppProfileByName(packageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(AndroidLifecycleScopeProvider.from(this@PerfProfilerService))
                .subscribe({ appProfile ->
                    lastProfile = appProfile

                    if (!isCharging && !isOvh && !isBoosting) {
                        if (appProfile.packageId.isNotEmpty()) {
                            applyProfile(appProfile.profile)
                        } else {
                            applyProfile(iAppRepository.getDefaultProfile())
                        }
                    }

                    lastTopApp = packageId
                }, { error ->
                    Log.e("PerfProfilerService", "Error getting app profile: ${error.message}")
                })
            disposable.add(profileDisposableRef!!)
        }
    }

    @SuppressLint("CheckResult", "AutoDispose")
    private fun applyProfile(profileString: String) {
        if (!iAppRepository.getForceProfileActive()) {
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
                                        isChanging = false
                                        disposable.remove(applyDisposableRef!!)
                                    }
                                    .subscribe(
                                        { Log.d("PerfProfilerService", "All CPU and GPU settings applied successfully") },
                                        { error -> Log.e("PerfProfilerService", "Error applying settings: ${error.message}") },
                                    )
                            )
                        }, { error ->
                            Log.e("PerfProfilerService", "Error getting CPU folders: ${error.message}")
                            isChanging = false
                        })
                }, { error ->
                    Log.e("PerfProfilerService", "Error getting profile: ${error.message}")
                    isChanging = false
                })
            disposable.add(applyDisposableRef!!)
        }
    }


    private fun boostPerf() {
        if (!isOvh && !iAppRepository.getForceProfileActive()) {
            isBoosting = true
            lifecycleScope.launch {
                applyProfile(iAppRepository.getBoostProfile())
                delay(500)
                lastProfile?.let {
                    isBoosting = false
                    runOnMainThread {
                        applyDisposableRef?.dispose()
                        val profile = if (isCharging) iAppRepository.getChargingProfile() else it.profile
                        applyProfile(if (profile == "Not Set") iAppRepository.getDefaultProfile() else profile)
                    }
                }
            }
        }
    }

    @SuppressLint("AutoDispose")
    private fun startService() {
        try {
            if (!isServiceStarted) {
                isServiceStarted = true

                startForeground(1, createNotification())

                pollingDisposableRef?.dispose()

                pollingDisposableRef = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        checkOvh()
                    }

                reniceProcesses()
                disposable.add(pollingDisposableRef!!)
            }
        } catch (e: Exception) {
            Log.e("PerfProfilerService", "Error starting service: ${e.message}")
        }
    }

    private fun checkOvh() {
        val batteryInfo = getBatteryInfo()
        isCharging = batteryInfo.second
        temperature = batteryInfo.first

        if (isCharging && !isOvh && !monChargingExec) {
            val profile = iAppRepository.getChargingProfile()
            if (profile.isNotEmpty()) {
                applyProfile(profile)
            }
            isCharging = true
            monChargingExec = true
        }

        if (!isCharging) {
            lastProfile?.let {
                if (monChargingExec) {
                    applyProfile(it.profile)
                }
            }
            monChargingExec = false
            isCharging = false

        }

        if (temperature >= 45) {
            val profile = iAppRepository.getOvh45Profile()
            if (profile.isNotEmpty() && currentAppliedProfile != "Ovh45") {
                applyProfile(profile)
                currentAppliedProfile = "Ovh45"
            }
        } else if (temperature in 42..44) {
            val profile = iAppRepository.getOvh42Profile()
            if (profile.isNotEmpty() && currentAppliedProfile != "Ovh42") {
                applyProfile(profile)
                currentAppliedProfile = "Ovh42"
            }
        } else if (temperature in 40..41) {
            val profile = iAppRepository.getOvh40Profile()
            if (profile.isNotEmpty() && currentAppliedProfile != "Ovh40") {
                applyProfile(profile)
                currentAppliedProfile = "Ovh40"
            }
            monTempExec = false
            isOvh = true
        }

        if (!monTempExec && temperature < 40) {
            lastProfile?.let {
                if (currentAppliedProfile != "LastProfile") {
                    applyProfile(it.profile)
                    currentAppliedProfile = "LastProfile"
                }
            }
            monTempExec = true
            isOvh = false
            currentAppliedProfile = ""
        }
    }

    private fun createNotification(): Notification {
        val channelId = "my_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "My Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Service Running")
            .setContentText("This service is running in the background")
            .setSmallIcon(R.drawable.save_24dp_1f1f1f_fill0_wght400_grad0_opsz24)
            .build()
    }

    private fun getBatteryInfo(): Pair<Int, Boolean> {
        val result = Shell.cmd("cat $BATTERY_UEVENT").exec()
        if (!result.isSuccess) return Pair(0, false)

        var temperature = 0
        var isCharging = false

        result.out.forEach { line ->
            when {
                line.startsWith("POWER_SUPPLY_TEMP=") -> {
                    temperature = line.substringAfter("POWER_SUPPLY_TEMP=").toInt() / 10
                }
                line.startsWith("POWER_SUPPLY_STATUS=") -> {
                    val status = line.substringAfter("POWER_SUPPLY_STATUS=")
                    isCharging = status.equals("Charging", ignoreCase = true) || status.equals("Full", ignoreCase = true)
                }
            }
        }

        return Pair(temperature, isCharging)
    }

    private fun reniceProcesses() {
        try {
            Shell.cmd("renice -n -5 -p $(pidof com.android.systemui)").exec()

            if (lastTopApp.isNotEmpty()) {
                Shell.cmd("renice -n -5 -p $(pidof $lastTopApp)").exec()
            }
        } catch (e: Exception) {
            Log.e("PerfProfilerService", "Error renicing processes: ${e.message}")
        }
    }

    private fun stopService() {
        isServiceStarted = false
        disposable.clear()
        lastTopApp = ""
        lastPackageIntent = ""
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        lastTopApp = ""
        lastPackageIntent = ""
        disposable.clear()
    }
}
