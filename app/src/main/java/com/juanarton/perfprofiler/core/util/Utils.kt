package com.juanarton.perfprofiler.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction

object Utils {
    fun getAppName(context: Context, packageId: String): String {
        val packageManager: PackageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageId, 0)
        return packageManager.getApplicationLabel(applicationInfo).toString()
    }

    fun getAppIcon(context: Context, packageId: String): Drawable? {
        return try {
            val packageManager: PackageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageId, 0)
            packageManager.getApplicationIcon(applicationInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun fragmentBuilder(activity: FragmentActivity, fragment: Fragment, holder: Int) {
        activity.supportFragmentManager
            .beginTransaction()
            .add(holder, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    fun destroyFragment(activity: FragmentActivity, fragment: Fragment) {
        val fragmentManager = activity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        if (fragment.isAdded) {
            transaction.remove(fragment)
                .commit()
        }
    }

    fun formatStringMhz(value: String): String {
        return "${value.toInt()/1000} Mhz"
    }

    fun formatGpuStringMhz(value: String): String {
        return "${value.toInt()/1000000} Mhz"
    }
}