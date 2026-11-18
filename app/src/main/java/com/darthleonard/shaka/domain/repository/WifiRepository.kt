package com.darthleonard.shaka.domain.repository

import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.ThrottleStatus
import com.darthleonard.shaka.domain.model.WifiNetwork
import kotlinx.coroutines.flow.StateFlow

interface WifiRepository {
    val nearbyNetworks: StateFlow<List<WifiNetwork>>
    val targetNetwork: StateFlow<WifiNetwork?>
    val isScanning: StateFlow<Boolean>
    val scanCount: StateFlow<Int>
    val lastScanTimestamp: StateFlow<Long>

    fun startContinuousScanning(mode: ScanMode, targetBssid: String?)
    fun stopScanning()
    fun setScanMode(mode: ScanMode)
    fun setTargetBssid(bssid: String?)
    suspend fun triggerSingleScan(): Boolean
    fun isWifiEnabled(): Boolean
    fun isLocationEnabled(): Boolean
    fun getThrottleStatus(): ThrottleStatus
}

