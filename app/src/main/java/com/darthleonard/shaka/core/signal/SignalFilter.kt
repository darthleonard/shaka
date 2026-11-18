package com.darthleonard.shaka.core.signal

import com.darthleonard.shaka.domain.model.ScanMode

/**
 * Exponential Moving Average (EMA) filter for RSSI smoothing.
 *
 * S_t = alpha * Y_t + (1 - alpha) * S_{t-1}
 *
 * In HARD mode: alpha is higher (more responsive to rapid physical movements).
 * In SOFT mode: alpha is lower (more conservative, heavy noise dampening).
 */
class SignalFilter(
    private var scanMode: ScanMode = ScanMode.HARD
) {
    private var smoothedRssi: Float? = null

    val currentAlpha: Float
        get() = when (scanMode) {
            ScanMode.HARD -> 0.60f
            ScanMode.SOFT -> 0.25f
        }

    fun setMode(mode: ScanMode) {
        this.scanMode = mode
    }

    fun filter(rawRssi: Int): Float {
        val current = smoothedRssi
        val next = if (current == null) {
            rawRssi.toFloat()
        } else {
            val alpha = currentAlpha
            alpha * rawRssi.toFloat() + (1f - alpha) * current
        }
        smoothedRssi = next
        return next
    }

    fun reset() {
        smoothedRssi = null
    }
}

