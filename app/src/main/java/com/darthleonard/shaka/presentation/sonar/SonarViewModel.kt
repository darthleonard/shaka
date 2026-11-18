package com.darthleonard.shaka.presentation.sonar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darthleonard.shaka.core.permissions.PermissionManager
import com.darthleonard.shaka.core.permissions.WifiCapabilityDetector
import com.darthleonard.shaka.core.session.SessionManager
import com.darthleonard.shaka.domain.model.DiagnosticEvent
import com.darthleonard.shaka.domain.model.EventLogLimit
import com.darthleonard.shaka.domain.model.FeedbackMode
import com.darthleonard.shaka.domain.model.ProcessedSignal
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.SessionStats
import com.darthleonard.shaka.domain.model.ThrottleStatus
import com.darthleonard.shaka.domain.model.WifiNetwork
import com.darthleonard.shaka.domain.repository.EventLogRepository
import com.darthleonard.shaka.domain.repository.SettingsRepository
import com.darthleonard.shaka.domain.repository.WifiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SonarViewModel(
    private val sessionManager: SessionManager,
    private val wifiRepository: WifiRepository,
    private val settingsRepository: SettingsRepository,
    private val eventLogRepository: EventLogRepository,
    private val capabilityDetector: WifiCapabilityDetector
) : ViewModel() {

    val isSessionActive: StateFlow<Boolean> = sessionManager.isSessionActive
    val currentSignal: StateFlow<ProcessedSignal> = sessionManager.currentSignal
    val sessionStats: StateFlow<SessionStats> = sessionManager.sessionStats

    val targetNetwork: StateFlow<WifiNetwork?> = wifiRepository.targetNetwork
    val nearbyNetworks: StateFlow<List<WifiNetwork>> = wifiRepository.nearbyNetworks
    val isScanning: StateFlow<Boolean> = wifiRepository.isScanning
    val scanCount: StateFlow<Int> = wifiRepository.scanCount
    val lastScanTimestamp: StateFlow<Long> = wifiRepository.lastScanTimestamp

    private val _isRefreshingNetworks = MutableStateFlow(false)
    val isRefreshingNetworks: StateFlow<Boolean> = _isRefreshingNetworks.asStateFlow()

    /** True only when a network is selected AND the sonar is not yet running. */
    val canStartSonar: StateFlow<Boolean> = combine(
        sessionManager.isSessionActive,
        wifiRepository.targetNetwork
    ) { active, target -> !active && target != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val scanMode: StateFlow<ScanMode> = settingsRepository.scanMode
    val feedbackMode: StateFlow<FeedbackMode> = settingsRepository.feedbackMode
    val eventLogLimit: StateFlow<EventLogLimit> = eventLogRepository.limit
    val audioVolume: StateFlow<Float> = settingsRepository.audioVolume
    val diagnosticEvents: StateFlow<List<DiagnosticEvent>> = eventLogRepository.events

    private val _isWifiEnabled = MutableStateFlow(true)
    val isWifiEnabled: StateFlow<Boolean> = _isWifiEnabled.asStateFlow()

    private val _isLocationEnabled = MutableStateFlow(true)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()

    private val _throttleStatus = MutableStateFlow(ThrottleStatus.UNKNOWN)
    val throttleStatus: StateFlow<ThrottleStatus> = _throttleStatus.asStateFlow()

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    init {
        refreshCapabilities()
        refreshNetworks()
    }

    fun refreshCapabilities() {
        _isWifiEnabled.value = capabilityDetector.isWifiEnabled()
        _isLocationEnabled.value = capabilityDetector.isLocationEnabled()
        _throttleStatus.value = capabilityDetector.getThrottleStatus()
    }

    fun updatePermissionStatus(granted: Boolean) {
        _hasPermissions.value = granted
        refreshCapabilities()
        if (granted) {
            refreshNetworks()
        }
    }

    fun toggleSonar() {
        if (isSessionActive.value) {
            sessionManager.stopSession()
        } else {
            val target = targetNetwork.value
            // Cannot start the sonar without a selected network target
            if (target == null) return
            sessionManager.startSession(
                mode = scanMode.value,
                feedbackMode = feedbackMode.value,
                targetBssid = target.bssid
            )
        }
    }

    fun setScanMode(mode: ScanMode) {
        settingsRepository.updateScanMode(mode)
        sessionManager.changeScanMode(mode)
    }

    fun setFeedbackMode(mode: FeedbackMode) {
        settingsRepository.updateFeedbackMode(mode)
        sessionManager.changeFeedbackMode(mode)
    }

    fun setVolume(volume: Float) {
        settingsRepository.updateAudioVolume(volume)
    }

    fun setLogLimit(limit: EventLogLimit) {
        settingsRepository.updateEventLogLimit(limit)
        eventLogRepository.setLimit(limit)
    }

    fun selectTargetNetwork(network: WifiNetwork) {
        sessionManager.changeTargetBssid(network.bssid)
    }

    fun refreshNetworks() {
        viewModelScope.launch {
            _isRefreshingNetworks.value = true
            try {
                wifiRepository.triggerSingleScan()
            } finally {
                delay(1200)
                _isRefreshingNetworks.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.stopSession()
    }
}

