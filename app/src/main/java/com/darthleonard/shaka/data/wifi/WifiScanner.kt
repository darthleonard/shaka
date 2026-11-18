package com.darthleonard.shaka.data.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import com.darthleonard.shaka.core.permissions.PermissionManager
import com.darthleonard.shaka.domain.model.WifiNetwork

class WifiScanner(
    private val context: Context,
    private val onScanResultsParsed: (networks: List<WifiNetwork>, wasUpdated: Boolean) -> Unit
) {
    private val wifiManager: WifiManager? by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }

    private val scanReceiver = WifiScanReceiver { resultsUpdated ->
        parseAndDeliverResults(resultsUpdated)
    }

    private var isListening = false

    fun startListening() {
        if (!isListening) {
            scanReceiver.register(context)
            isListening = true
            // Read initial/cached results immediately
            parseAndDeliverResults(wasUpdated = false)
        }
    }

    fun stopListening() {
        if (isListening) {
            scanReceiver.unregister(context)
            isListening = false
        }
    }

    @SuppressLint("MissingPermission")
    fun requestScan(): Boolean {
        val manager = wifiManager ?: return false
        return try {
            @Suppress("DEPRECATION")
            manager.startScan()
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun parseAndDeliverResults(wasUpdated: Boolean) {
        val manager = wifiManager ?: return
        val rawResults = try {
            manager.scanResults ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val nowMicros = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            SystemClock.elapsedRealtimeNanos() / 1000
        } else {
            SystemClock.elapsedRealtime() * 1000
        }

        val parsedNetworks = rawResults.mapNotNull { scanResult ->
            mapToDomain(scanResult, nowMicros)
        }

        onScanResultsParsed(parsedNetworks, wasUpdated)
    }

    private fun mapToDomain(scanResult: ScanResult, nowMicros: Long): WifiNetwork? {
        val bssid = scanResult.BSSID ?: return null
        if (bssid.isBlank() || bssid == "02:00:00:00:00:00") {
            // Masked or invalid BSSID
            return null
        }

        @Suppress("DEPRECATION")
        val legacySsid = scanResult.SSID.orEmpty()
        val ssid = if (legacySsid.isNotBlank()) {
            legacySsid
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            scanResult.wifiSsid?.toString()?.removeSurrounding("\"").orEmpty()
        } else {
            ""
        }

        val timestamp = scanResult.timestamp
        val ageMs = if (timestamp > 0) {
            ((nowMicros - timestamp) / 1000).coerceAtLeast(0L)
        } else {
            0L
        }

        // Stale if older than 30 seconds
        val isStale = ageMs > 30_000L

        val freq = scanResult.frequency
        val channel = frequencyToChannel(freq)
        val band = when {
            freq in 2400..2499 -> "2.4 GHz"
            freq in 4900..5900 -> "5 GHz"
            freq in 5925..7125 -> "6 GHz"
            else -> "$freq MHz"
        }

        return WifiNetwork(
            bssid = bssid.uppercase(),
            ssid = ssid,
            rawRssi = scanResult.level,
            frequencyMhz = freq,
            channel = channel,
            band = band,
            timestampMicros = timestamp,
            ageMs = ageMs,
            isStale = isStale
        )
    }

    private fun frequencyToChannel(freq: Int): Int {
        return when {
            freq == 2484 -> 14
            freq in 2412..2472 -> (freq - 2412) / 5 + 1
            freq in 5170..5825 -> (freq - 5170) / 5 + 34
            freq in 5955..7115 -> (freq - 5955) / 5 + 1
            else -> 0
        }
    }
}

