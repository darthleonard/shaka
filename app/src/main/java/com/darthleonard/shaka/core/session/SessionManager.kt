package com.darthleonard.shaka.core.session

import com.darthleonard.shaka.core.audio.BeepController
import com.darthleonard.shaka.core.signal.SignalProcessor
import com.darthleonard.shaka.domain.model.EventCode
import com.darthleonard.shaka.domain.model.EventLogLevel
import com.darthleonard.shaka.domain.model.FeedbackMode
import com.darthleonard.shaka.domain.model.ProcessedSignal
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.SessionStats
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

class SessionManager(
    private val wifiRepository: WifiRepository,
    private val beepController: BeepController,
    private val eventLogger: EventLogRepository,
    private val signalProcessor: SignalProcessor = SignalProcessor()
) {
    private val sessionScope = CoroutineScope(Dispatchers.Default)
    private var tickerJob: Job? = null
    private var signalCollectorJob: Job? = null

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _currentSignal = MutableStateFlow<ProcessedSignal>(signalProcessor.process(null))
    val currentSignal: StateFlow<ProcessedSignal> = _currentSignal.asStateFlow()

    private val _sessionStats = MutableStateFlow(SessionStats())
    val sessionStats: StateFlow<SessionStats> = _sessionStats.asStateFlow()

    private var sessionStartTimeMs: Long = 0L
    private var rssiSum: Long = 0L
    private var rssiCount: Int = 0
    private var minRecordedRssi: Int? = null
    private var maxRecordedRssi: Int? = null

    fun startSession(
        mode: ScanMode,
        feedbackMode: FeedbackMode,
        targetBssid: String?
    ) {
        if (_isSessionActive.value) return

        _isSessionActive.value = true
        sessionStartTimeMs = System.currentTimeMillis()
        rssiSum = 0L
        rssiCount = 0
        minRecordedRssi = null
        maxRecordedRssi = null
        _sessionStats.value = SessionStats(startTimeMs = sessionStartTimeMs)

        signalProcessor.reset()
        signalProcessor.setScanMode(mode)

        eventLogger.log(
            level = EventLogLevel.INFO,
            code = EventCode.SONAR_STARTED,
            message = "Sesión de sonar iniciada",
            details = "Modo: ${mode.name}, Respuesta: ${feedbackMode.name}, BSSID objetivo: ${targetBssid ?: "Ninguno"}"
        )

        // Start scanning in repository
        wifiRepository.startContinuousScanning(mode, targetBssid)

        // Start audio
        beepController.setFeedbackMode(feedbackMode)
        beepController.start(sessionScope)

        // Listen for target network updates
        signalCollectorJob?.cancel()
        signalCollectorJob = sessionScope.launch {
            wifiRepository.targetNetwork.collect { network ->
                if (_isSessionActive.value) {
                    processIncomingNetwork(network)
                }
            }
        }

        // Ticker for duration and staleness updates every 200 ms (smooth age counter + stale detection)
        tickerJob?.cancel()
        tickerJob = sessionScope.launch {
            while (isActive && _isSessionActive.value) {
                delay(200)
                updateSessionTick()
            }
        }
    }

    private fun processIncomingNetwork(network: WifiNetwork?) {
        val processed = signalProcessor.process(network)
        _currentSignal.value = processed

        if (network != null && !processed.isStale) {
            val rssi = network.rawRssi
            rssiSum += rssi
            rssiCount++

            minRecordedRssi = minRecordedRssi?.let { minOf(it, rssi) } ?: rssi
            maxRecordedRssi = maxRecordedRssi?.let { maxOf(it, rssi) } ?: rssi

            _sessionStats.value = _sessionStats.value.copy(
                measurementCount = rssiCount,
                minRssi = minRecordedRssi,
                maxRssi = maxRecordedRssi,
                avgRssi = if (rssiCount > 0) rssiSum.toFloat() / rssiCount else null
            )

            // Update audio beep interval
            beepController.updateInterval(processed.beepIntervalMs)
        }
    }

    private fun updateSessionTick() {
        val now = System.currentTimeMillis()
        val duration = if (sessionStartTimeMs > 0) now - sessionStartTimeMs else 0L

        _sessionStats.value = _sessionStats.value.copy(
            durationMs = duration
        )

        // Refresh age and staleness of current signal even if no new measurement arrived
        val target = wifiRepository.targetNetwork.value
        if (target != null) {
            val updatedSignal = signalProcessor.process(target)
            _currentSignal.value = updatedSignal
            if (updatedSignal.isStale) {
                // Signal is stale — slow beeps to signal the problem
                beepController.updateInterval(2000L)
            }
        }
    }

    fun stopSession() {
        if (!_isSessionActive.value) return

        _isSessionActive.value = false

        tickerJob?.cancel()
        tickerJob = null
        signalCollectorJob?.cancel()
        signalCollectorJob = null

        // Stop scanning
        wifiRepository.stopScanning()

        // Stop audio
        beepController.stop()

        // Clear transient event logs and signal
        eventLogger.log(
            level = EventLogLevel.INFO,
            code = EventCode.SONAR_STOPPED,
            message = "Sesión de sonar detenida. Liberando recursos y limpiando registros temporales."
        )

        // Clear event logs when session ends as specified in section 10 & 12
        eventLogger.clearEvents()

        signalProcessor.reset()
        _currentSignal.value = signalProcessor.process(null)
        _sessionStats.value = SessionStats()
        sessionStartTimeMs = 0L
    }

    fun changeScanMode(mode: ScanMode) {
        signalProcessor.setScanMode(mode)
        wifiRepository.setScanMode(mode)
    }

    fun changeFeedbackMode(mode: FeedbackMode) {
        beepController.setFeedbackMode(mode)
    }

    fun changeTargetBssid(bssid: String?) {
        wifiRepository.setTargetBssid(bssid)
        signalProcessor.reset()
    }
}

