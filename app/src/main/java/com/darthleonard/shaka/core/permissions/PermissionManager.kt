package com.darthleonard.shaka.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

data class PermissionState(
    val hasFineLocation: Boolean,
    val hasCoarseLocation: Boolean,
    val hasNearbyDevices: Boolean,
    val isWifiScanningAllowed: Boolean,
    val missingPermissions: List<String>
)

object PermissionManager {

    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        return permissions
    }

    fun checkPermissions(context: Context): PermissionState {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val nearbyDevices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val missing = mutableListOf<String>()
        if (!fineLocation) missing.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !nearbyDevices) {
            missing.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        // Location permission is strictly required to read BSSID and RSSI without redaction
        val isAllowed = fineLocation && nearbyDevices

        return PermissionState(
            hasFineLocation = fineLocation,
            hasCoarseLocation = coarseLocation,
            hasNearbyDevices = nearbyDevices,
            isWifiScanningAllowed = isAllowed,
            missingPermissions = missing
        )
    }
}

