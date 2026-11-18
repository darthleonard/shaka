package com.darthleonard.shaka.data.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build

class WifiScanReceiver(
    private val onScanResultsAvailable: (resultsUpdated: Boolean) -> Unit
) : BroadcastReceiver() {

    private var isRegistered = false

    override fun onReceive(context: Context, intent: Intent) {
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == intent.action) {
            val updated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            onScanResultsAvailable(updated)
        }
    }

    @Synchronized
    fun register(context: Context) {
        if (isRegistered) return
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(this, filter)
        }
        isRegistered = true
    }

    @Synchronized
    fun unregister(context: Context) {
        if (!isRegistered) return
        try {
            context.unregisterReceiver(this)
        } catch (_: IllegalArgumentException) {
            // Already unregistered
        } finally {
            isRegistered = false
        }
    }
}

