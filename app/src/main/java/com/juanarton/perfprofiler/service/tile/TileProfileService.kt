package com.juanarton.perfprofiler.service.tile

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.juanarton.perfprofiler.R
import com.juanarton.perfprofiler.core.data.domain.repository.IAppRepository
import com.juanarton.perfprofiler.service.perfservice.Action
import com.juanarton.perfprofiler.service.perfservice.PerfProfilerService
import com.juanarton.perfprofiler.service.perfservice.ServiceState
import com.juanarton.perfprofiler.service.perfservice.getServiceState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TileProfileService : TileService() {

    @Inject
    lateinit var iAppRepository: IAppRepository

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
            startPerfProfilerService(Action.FORCE)
            Tile.STATE_INACTIVE
        } else {
            iAppRepository.setForceProfileActive(true)
            startPerfProfilerService(Action.FORCE)
            Tile.STATE_ACTIVE
        }
        qsTile.updateTile()
    }

    private fun startPerfProfilerService(action: Action) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Action.STOP) return
        Intent(this, PerfProfilerService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("BatteryMonitorService", "Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            Log.d("BatteryMonitorService", "Starting the service in < 26 Mode")
            startService(it)
        }
    }
}