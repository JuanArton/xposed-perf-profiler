package com.juanarton.perfprofiler.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.kyuubiran.ezxhelper.BuildConfig
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.data.domain.model.AppProfile
import com.juanarton.perfprofiler.core.data.domain.repository.IAppRepository
import com.juanarton.perfprofiler.core.util.IOUtils.writeToFile
import com.juanarton.perfprofiler.core.util.Path
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.math.floor

@AndroidEntryPoint
class PerfProfilerService : Service() {
    private var isServiceStarted = false

    @Inject
    lateinit var iAppRepository: IAppRepository
    private val serviceScopeChangeProfile = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val serviceScopeBattMonitor = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @OptIn(ExperimentalCoroutinesApi::class)
    private val limitedDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val profileMutex = Mutex()
    private var lastTopApp = ""
    private var isCharging = false
    private var temperature = 0
    private var isOvh = false
    private var monChargingExec = false
    private var monTempExec = false
    private var lastProfile: AppProfile? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private var debounceJob: Job? = null
    private val debounceDelay = 50L

    companion object {
        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10)
            )
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                Action.START.name -> {
                    startService()
                }
                Action.STOP.name -> stopService()
                Action.CHANGE_PROFILE.name -> {
                    debounceJob?.cancel()
                    debounceJob = scope.launch {
                        delay(debounceDelay)
                        if (!isServiceStarted) startService()
                        val packageId = intent.getStringExtra("PACKAGE_ID")
                        packageId?.let {
                            changeProfile(packageId)
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
        if (packageId != lastTopApp ) {

            serviceScopeChangeProfile.coroutineContext.cancelChildren()
            serviceScopeChangeProfile.launch(limitedDispatcher) {
                profileMutex.withLock {
                    val appProfile = iAppRepository.getAppProfileByName(packageId).first()
                    lastProfile = appProfile

                    if (!isCharging && !isOvh) {
                        if (appProfile != null) {
                            applyProfile(appProfile.profile)
                        } else {
                            val defProfile = iAppRepository.getDefaultProfile()
                            if (defProfile.isNotEmpty()) {
                                applyProfile(defProfile)
                            }
                        }
                    }

                    lastTopApp = packageId
                }
            }
        }
    }

    private suspend fun applyProfile(profileString: String) {
        val profile = iAppRepository.getProfileByName(profileString).first()
        val cpuFolder = iAppRepository.getCpuFolders().first()

        cpuFolder.forEachIndexed { index, policy ->
            val maxFreq = when (index) {
                0 -> profile.c1MaxFreq
                1 -> profile.c2MaxFreq
                2 -> profile.c3MaxFreq
                else -> profile.c1MaxFreq
            }

            val minFreq = when (index) {
                0 -> profile.c1MinFreq
                1 -> profile.c2MinFreq
                2 -> profile.c3MinFreq
                else -> profile.c1MinFreq
            }

            val gov = when (index) {
                0 -> profile.c1Governor
                1 -> profile.c2Governor
                2 -> profile.c3Governor
                else -> profile.c1Governor
            }

            writeToFile("${Path.CPU_PATH}/$policy", Path.SCALING_MAX_FREQ, maxFreq)
            writeToFile("${Path.CPU_PATH}/$policy", Path.SCALING_MIN_FREQ, minFreq)
            writeToFile("${Path.CPU_PATH}/$policy", Path.SCALING_GOVERNOR, gov)
        }

        writeToFile(Path.GPU_PATH, Path.GPU_MAX_FREQ, profile.gpuMaxFreq)
        writeToFile(Path.GPU_PATH, Path.GPU_MIN_FREQ, profile.gpuMinFreq)
        writeToFile(Path.GPU_PATH, Path.GPU_CURRENT_GOV, profile.gpuGovernor)
    }

    private fun startService() {
        try {
            if (!isServiceStarted) {
                isServiceStarted = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        1, createNotification(),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                            } else {
                                0
                            }
                        } else { 0 }
                    )
                } else {
                    startForeground(1, createNotification())
                }

                serviceScopeBattMonitor.coroutineContext.cancelChildren()
                serviceScopeBattMonitor.launch(limitedDispatcher) {
                    while (true) {
                        val batteryInfo = getBatteryInfo(this@PerfProfilerService)
                        isCharging = batteryInfo.second
                        temperature = batteryInfo.first

                        if (isCharging && !isOvh && !monChargingExec) {
                            val profile = iAppRepository.getChargingProfile()
                            if (profile.isNotEmpty()) {
                                applyProfile(profile)
                            }
                            monChargingExec = true
                        }

                        if (!isCharging) {
                            monChargingExec = false
                            lastProfile?.let {
                                applyProfile(it.profile)
                            }
                        }

                        if (temperature >= 45) {
                            val profile = iAppRepository.getOvh45Profile()
                            if (profile.isNotEmpty()) {
                                applyProfile(profile)
                            }
                        } else if (temperature in 42..44) {
                            val profile = iAppRepository.getOvh42Profile()
                            if (profile.isNotEmpty()) {
                                applyProfile(profile)
                            }
                        } else if (temperature in 40..41) {
                            val profile = iAppRepository.getOvh40Profile()
                            if (profile.isNotEmpty()) {
                                applyProfile(profile)
                            }
                            monTempExec = false
                            isOvh = true
                        }

                        if (!monTempExec && temperature < 40) {
                            lastProfile?.let {
                                applyProfile(it.profile)
                            }
                            monTempExec = true
                            isOvh = false
                        }

                        delay(1000)
                    }
                }
            }
        } catch (e: Exception) {

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

    private fun stopService() {
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (e: Exception) {
            Log.d("PerfProfilerService", "Service stopped without being started: $e")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    private fun getBatteryInfo(context: Context): Pair<Int, Boolean> {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.div(10f) ?: 0f

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)

        val isCharging = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL -> true
            else -> false
        }

        return Pair(floor(temperature).toInt(), isCharging)
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScopeChangeProfile.cancel()
        serviceScopeBattMonitor.cancel()
    }
}