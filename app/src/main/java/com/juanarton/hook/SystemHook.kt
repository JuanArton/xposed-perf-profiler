package com.juanarton.hook

import android.app.AndroidAppHelper
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.util.Log
import com.juanarton.perfprofiler.core.service.Action
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage

class SystemHook : IXposedHookLoadPackage {
    private var lastTopApp = ""

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "android") return

        try {
            findAndHookMethod(
                "com.android.server.wm.ActivityTaskManagerService", lpparam.classLoader,
                "getFocusedRootTaskInfo", object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val result = param.result ?: return
                            val topActivity = XposedHelpers.getObjectField(result, "topActivity") ?: return
                            val packageName = extractPackageName(topActivity.toString())

                            if (packageName != null && packageName != lastTopApp) {
                                XposedBridge.log("Foreground App: $packageName")
                                startPerfProfilerService(packageName)
                                lastTopApp = packageName
                            }
                        } catch (e: Throwable) {
                            XposedBridge.log("Error in afterHookedMethod: ${Log.getStackTraceString(e)}")
                        }
                    }

                })
        } catch (e: Throwable) {
            Log.e("XposedHook", "Failed to hook ActivityThread", e)
        }
    }

    private fun startPerfProfilerService(packageName: String) {
        try {
            val serviceIntent = Intent().apply {
                action = Action.CHANGE_PROFILE.name
                setComponent(
                    ComponentName(
                        "com.juanarton.perfprofiler",
                        "com.juanarton.perfprofiler.core.service.PerfProfilerService"
                    )
                )
                putExtra("PACKAGE_ID", packageName)
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

    private fun extractPackageName(input: String): String? {
        return try {
            val regex = Regex("ComponentInfo\\{([^/]+)")
            return regex.find(input)?.groupValues?.get(1)
        } catch (e: Exception) {
            "-"
        }
    }
}