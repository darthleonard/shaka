package com.darthleonard.shaka.core.signal

import com.darthleonard.shaka.domain.model.SignalTrend

/**
 * Detects whether the Wi-Fi signal is IMPROVING, DETERIORATING, or STABLE.
 * Uses a small moving window of smoothed RSSI values to prevent micro-fluctuation jitter.
 */
class SignalTrendDetector(
    private val thresholdDbm: Float = 1.5f
) {
    private var baselineRssi: Float? = null

    fun updateTrend(smoothedRssi: Float): SignalTrend {
        val base = baselineRssi
        if (base == null) {
            baselineRssi = smoothedRssi
            return SignalTrend.STABLE
        }

        val delta = smoothedRssi - base
        return when {
            delta >= thresholdDbm -> {
                baselineRssi = smoothedRssi
                SignalTrend.IMPROVING
            }
            delta <= -thresholdDbm -> {
                baselineRssi = smoothedRssi
                SignalTrend.DETERIORATING
            }
            else -> {
                // Decay baseline slowly towards current value to adapt to gradual changes
                baselineRssi = base + 0.15f * delta
                SignalTrend.STABLE
            }
        }
    }

    fun reset() {
        baselineRssi = null
    }
}

