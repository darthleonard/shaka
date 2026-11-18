package com.darthleonard.shaka.core.permissions

import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.location.LocationManagerCompat
import com.darthleonard.shaka.domain.model.ThrottleStatus

class WifiCapabilityDetector(
    private val context: Context
) {
    private val wifiManager: WifiManager? by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }

    private val locationManager: LocationManager? by lazy {
        context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    fun isWifiEnabled(): Boolean {
        return try {
            wifiManager?.isWifiEnabled ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun isLocationEnabled(): Boolean {
        val manager = locationManager ?: return false
        return try {
            LocationManagerCompat.isLocationEnabled(manager)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Inspects scan throttling capability.
     * On API 30+, attempts to query WifiManager.isScanThrottleEnabled().
     * On API 29 or when restricted by Android, returns ThrottleStatus.UNKNOWN.
     */
    fun getThrottleStatus(): ThrottleStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return ThrottleStatus.UNKNOWN
        }

        val manager = wifiManager ?: return ThrottleStatus.UNKNOWN
        return try {
            val method = manager.javaClass.getMethod("isScanThrottleEnabled")
            val result = method.invoke(manager) as? Boolean
            when (result) {
                true -> ThrottleStatus.ENABLED
                false -> ThrottleStatus.DISABLED
                null -> ThrottleStatus.UNKNOWN
            }
        } catch (_: SecurityException) {
            // Android may restrict access to system apps; throttling is active by default on Android 9+
            ThrottleStatus.UNKNOWN
        } catch (_: Exception) {
            ThrottleStatus.UNKNOWN
        }
    }
}

