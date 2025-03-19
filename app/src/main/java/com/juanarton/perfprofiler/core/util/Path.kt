package com.juanarton.perfprofiler.core.util

object Path {
    const val CPU_PATH = "/sys/devices/system/cpu/cpufreq"
    const val GPU_PATH = "/sys/bus/platform/drivers/kgsl-3d/module/drivers/platform:kgsl-3d/3d00000.qcom,kgsl-3d0/devfreq/3d00000.qcom,kgsl-3d0"

    const val SCALING_AVAILABLE_FREQ = "scaling_available_frequencies"
    const val SCALING_BOOST_FREQ = "scaling_boost_frequencies"
    const val SCALING_AVAILABLE_GOV = "scaling_available_governors"
    const val SCALING_MAX_FREQ = "scaling_max_freq"
    const val SCALING_MIN_FREQ = "scaling_min_freq"
    const val SCALING_GOVERNOR = "scaling_governor"

    const val GPU_AVAILABLE_FREQ = "available_frequencies"
    const val GPU_AVAILABLE_GOV = "available_governors"
    const val GPU_CURRENT_GOV = "governor"
    const val GPU_MAX_FREQ = "max_freq"
    const val GPU_MIN_FREQ = "min_freq"

    const val BATTERY_UEVENT = "/sys/class/power_supply/battery/uevent"
}