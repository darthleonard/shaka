package com.darthleonard.shaka.data.wifi

import android.content.Context
import com.darthleonard.shaka.core.permissions.WifiCapabilityDetector
import com.darthleonard.shaka.domain.model.EventCode
import com.darthleonard.shaka.domain.model.EventLogLevel
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.ThrottleStatus
import com.darthleonard.shaka.domain.model.WifiNetwork
import com.darthleonard.shaka.domain.repository.EventLogRepository
import com.darthleonard.shaka.domain.repository.WifiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WifiRepositoryImpl(
    private val context: Context,
    private val eventLogger: EventLogRepository? = null,
    private val capabilityDetector: WifiCapabilityDetector = WifiCapabilityDetector(context)
) : WifiRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Default)
    private var scanLoopJob: Job? = null

    private var activeStrategy: ScanStrategy = HardScanStrategy()
    private var selectedBssid: String? = null
    private var consecutiveRejections: Int = 0

    private val _nearbyNetworks = MutableStateFlow<List<WifiNetwork>>(emptyList())
    override val nearbyNetworks: StateFlow<List<WifiNetwork>> = _nearbyNetworks.asStateFlow()

    private val _targetNetwork = MutableStateFlow<WifiNetwork?>(null)
    override val targetNetwork: StateFlow<WifiNetwork?> = _targetNetwork.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanCount = MutableStateFlow(0)
    override val scanCount: StateFlow<Int> = _scanCount.asStateFlow()

    private val _lastScanTimestamp = MutableStateFlow(0L)
    override val lastScanTimestamp: StateFlow<Long> = _lastScanTimestamp.asStateFlow()

    private val scanner = WifiScanner(context) { networks, wasUpdated ->
        handleScanResults(networks, wasUpdated)
    }

    init {
        // Start passive listening and load cached results immediately upon creation
        scanner.startListening()
    }

    override fun startContinuousScanning(mode: ScanMode, targetBssid: String?) {
        setScanMode(mode)
        setTargetBssid(targetBssid)
        _isScanning.value = true

        if (!capabilityDetector.isWifiEnabled()) {
            eventLogger?.log(
                level = EventLogLevel.WARNING,
                code = EventCode.WIFI_DISABLED,
                message = "El Wi-Fi está desactivado en el dispositivo"
            )
        }

        if (!capabilityDetector.isLocationEnabled()) {
            eventLogger?.log(
                level = EventLogLevel.WARNING,
                code = EventCode.LOCATION_DISABLED,
                message = "Servicios de ubicación desactivados; requeridos para escanear Wi-Fi"
            )
        }

        scanner.startListening()
        startActiveScanLoop()
    }

    override fun stopScanning() {
        _isScanning.value = false
        scanLoopJob?.cancel()
        scanLoopJob = null
        // Keep receiver listening so passive network updates continue to be received
        consecutiveRejections = 0
    }

    override fun setScanMode(mode: ScanMode) {
        activeStrategy = ScanStrategyFactory.create(mode)
        eventLogger?.log(
            level = EventLogLevel.INFO,
            code = EventCode.SCAN_MODE_CHANGED,
            message = "Modo de escaneo cambiado a ${mode.name}"
        )
    }

    override fun setTargetBssid(bssid: String?) {
        selectedBssid = bssid?.uppercase()
        if (selectedBssid != null) {
            val matched = _nearbyNetworks.value.find { it.bssid.equals(selectedBssid, ignoreCase = true) }
            _targetNetwork.value = matched
            eventLogger?.log(
                level = EventLogLevel.INFO,
                code = EventCode.NETWORK_SELECTED,
                message = "Red objetivo seleccionada: ${matched?.displayName ?: selectedBssid}",
                details = "BSSID: $selectedBssid"
            )
        } else {
            _targetNetwork.value = null
        }
    }

    override suspend fun triggerSingleScan(): Boolean {
        // Ensure broadcast receiver is active
        scanner.startListening()
        // Immediately deliver currently cached results from OS without waiting
        scanner.parseAndDeliverResults(wasUpdated = false)
        // Request fresh active scan from OS
        return performActiveScan()
    }

    override fun isWifiEnabled(): Boolean = capabilityDetector.isWifiEnabled()

    override fun isLocationEnabled(): Boolean = capabilityDetector.isLocationEnabled()

    override fun getThrottleStatus(): ThrottleStatus = capabilityDetector.getThrottleStatus()

    private fun startActiveScanLoop() {
        scanLoopJob?.cancel()
        scanLoopJob = repositoryScope.launch {
            while (isActive && _isScanning.value) {
                val success = performActiveScan()
                val delayTime = activeStrategy.computeNextDelayMs(consecutiveRejections)
                delay(delayTime)
            }
        }
    }

    private fun performActiveScan(): Boolean {
        val success = scanner.requestScan()
        if (success) {
            consecutiveRejections = 0
            eventLogger?.log(
                level = EventLogLevel.DEBUG,
                code = EventCode.WIFI_SCAN_REQUESTED,
                message = "Solicitud de escaneo activo aceptada por el SO"
            )
        } else {
            consecutiveRejections++
            val nextDelay = activeStrategy.computeNextDelayMs(consecutiveRejections)
            eventLogger?.log(
                level = EventLogLevel.WARNING,
                code = EventCode.WIFI_SCAN_THROTTLED,
                message = "Escaneo rechazado por Android (Throttled/Limitado)",
                details = "Rechazos consecutivos: $consecutiveRejections. Próximo intento en ${nextDelay / 1000}s"
            )
        }
        return success
    }

    private fun handleScanResults(networks: List<WifiNetwork>, wasUpdated: Boolean) {
        val sortedNetworks = networks.sortedByDescending { it.rawRssi }
        _nearbyNetworks.value = sortedNetworks
        _scanCount.value += 1
        _lastScanTimestamp.value = System.currentTimeMillis()

        val targetBssid = selectedBssid
        if (targetBssid != null) {
            val foundTarget = sortedNetworks.find { it.bssid.equals(targetBssid, ignoreCase = true) }
            if (foundTarget != null) {
                _targetNetwork.value = foundTarget
            } else {
                // Keep the target in memory with staleness flag, do not silently deselect!
                val current = _targetNetwork.value
                if (current != null) {
                    _targetNetwork.value = current.copy(isStale = true)
                }
                eventLogger?.log(
                    level = EventLogLevel.WARNING,
                    code = EventCode.TARGET_BSSID_NOT_FOUND,
                    message = "BSSID objetivo no detectado en el último escaneo",
                    details = "BSSID: $targetBssid"
                )
            }
        }

        if (wasUpdated) {
            eventLogger?.log(
                level = EventLogLevel.DEBUG,
                code = EventCode.WIFI_SCAN_COMPLETED,
                message = "Resultados de escaneo actualizados (${sortedNetworks.size} redes)"
            )
        }
    }
}

