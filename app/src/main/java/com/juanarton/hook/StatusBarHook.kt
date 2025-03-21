package com.juanarton.hook

import android.app.AndroidAppHelper
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.juanarton.Initiator
import com.juanarton.perfprofiler.service.perfservice.Action
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class StatusBarHook : IXposedHookZygoteInit, IXposedHookLoadPackage {

    private val handlerThread = HandlerThread("ProfilerThread").apply { start() }
    private val backgroundHandler = Handler(handlerThread.looper)

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return
        EzXHelperInit.initHandleLoadPackage(lpparam)
        Initiator.init(lpparam.classLoader)

        findMethod("com.android.systemui.shade.NotificationPanelViewController") {
            name == "fling"
        }.hookBefore { param ->
            backgroundHandler.post {
                val handlerThread = HandlerThread("ProfilerThread").apply { start() }
                val threadHandler = Handler(handlerThread.looper)

                threadHandler.post {
                    try {
                        XposedBridge.log("Expand: ${param.args[2]}")
                        if (param.args[2] as Boolean) {
                            startPerfProfilerService()
                        }
                    } catch (e: Exception) {
                        XposedBridge.log("Error starting profiler: $e")
                    } finally {
                        handlerThread.quitSafely()
                    }
                }
            }
        }
    }

    private fun startPerfProfilerService() {
        try {
            val serviceIntent = Intent().apply {
                action = Action.BOOST.name
                setComponent(
                    ComponentName(
                        "com.juanarton.perfprofiler",
                        "com.juanarton.perfprofiler.service.perfservice.PerfProfilerService"
                    )
                )
            }

            val app = AndroidAppHelper.currentApplication()
            if (app != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    app.startForegroundService(serviceIntent)
                } else {
                    app.startService(serviceIntent)
                }
            } else {
                XposedBridge.log("currentApplication() returned null")
            }
        } catch (e: Exception) {

        }
    }
}