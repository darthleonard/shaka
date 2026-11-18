package com.darthleonard.shaka.core.signal

import com.darthleonard.shaka.domain.model.ProcessedSignal
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.SignalQuality
import com.darthleonard.shaka.domain.model.SignalTrend
import com.darthleonard.shaka.domain.model.WifiNetwork

class SignalProcessor(
    initialMode: ScanMode = ScanMode.HARD
) {
    private val filter = SignalFilter(initialMode)
    private val trendDetector = SignalTrendDetector()
    private var scanMode: ScanMode = initialMode

    // Staleness threshold in milliseconds
    // HARD: mark stale after 6 s (Android's throttle window is ~30 s, but passive results arrive faster)
    // SOFT: mark stale after 20 s (conservative scanning policy)
    val staleThresholdMs: Long
        get() = when (scanMode) {
            ScanMode.HARD -> 6_000L
            ScanMode.SOFT -> 20_000L
        }

    fun setScanMode(mode: ScanMode) {
        this.scanMode = mode
        filter.setMode(mode)
    }

    fun process(network: WifiNetwork?, nowAgeMs: Long? = null): ProcessedSignal {
        if (network == null) {
            return ProcessedSignal(
                rawRssi = -100,
                smoothedRssi = -100f,
                visualPercentage = 0f,
                quality = SignalQuality.UNKNOWN,
                trend = SignalTrend.STABLE,
                ageMs = 0L,
                isStale = true,
                beepIntervalMs = 1400L
            )
        }

        val age = nowAgeMs ?: network.ageMs
        val isStale = age > staleThresholdMs

        val raw = network.rawRssi
        val smoothed = filter.filter(raw)
        val percentage = SignalMapper.mapRssiToPercentage(smoothed)
        val quality = if (isStale) SignalQuality.UNKNOWN else SignalMapper.mapRssiToQuality(smoothed)
        val trend = if (isStale) SignalTrend.STABLE else trendDetector.updateTrend(smoothed)
        val beepInterval = SignalMapper.mapRssiToBeepIntervalMs(smoothed)

        return ProcessedSignal(
            rawRssi = raw,
            smoothedRssi = smoothed,
            visualPercentage = percentage,
            quality = quality,
            trend = trend,
            ageMs = age,
            isStale = isStale,
            beepIntervalMs = beepInterval
        )
    }

    fun reset() {
        filter.reset()
        trendDetector.reset()
    }
}

