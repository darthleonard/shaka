package com.darthleonard.shaka.core.signal

import com.darthleonard.shaka.domain.model.SignalQuality
import kotlin.math.roundToLong

object SignalMapper {

    private const val MIN_RSSI_DBM = -100f
    private const val MAX_RSSI_DBM = -30f

    // Reference table for RSSI to beep interval in milliseconds
    private val INTERVAL_TABLE = listOf(
        -90f to 1200L,
        -80f to 1000L,
        -70f to 700L,
        -60f to 450L,
        -50f to 250L,
        -40f to 120L
    )

    /**
     * Maps an RSSI dBm value to a normalized visual percentage between 0.0f and 1.0f.
     * -100 dBm represents 0.0 (no usable signal)
     * -30 dBm represents 1.0 (maximum signal)
     */
    fun mapRssiToPercentage(rssi: Float): Float {
        val clamped = rssi.coerceIn(MIN_RSSI_DBM, MAX_RSSI_DBM)
        return (clamped - MIN_RSSI_DBM) / (MAX_RSSI_DBM - MIN_RSSI_DBM)
    }

    /**
     * Categorizes an RSSI value into a user-friendly SignalQuality.
     */
    fun mapRssiToQuality(rssi: Float): SignalQuality {
        return when {
            rssi >= -50f -> SignalQuality.EXCELLENT
            rssi >= -65f -> SignalQuality.GOOD
            rssi >= -75f -> SignalQuality.MODERATE
            rssi >= -85f -> SignalQuality.WEAK
            rssi > -100f -> SignalQuality.CRITICAL
            else -> SignalQuality.UNKNOWN
        }
    }

    /**
     * Interpolates the audio beep interval in milliseconds according to RSSI.
     * Weaker signal = longer interval (slower beeps).
     * Stronger signal = shorter interval (faster beeps).
     */
    fun mapRssiToBeepIntervalMs(rssi: Float): Long {
        if (rssi <= -90f) {
            val extra = ((-90f - rssi) * 20f).coerceAtMost(300f)
            return 1200L + extra.roundToLong()
        }
        if (rssi >= -40f) {
            val boost = ((rssi - (-40f)) * 4f).coerceAtMost(40f)
            return (120L - boost.roundToLong()).coerceAtLeast(80L)
        }

        for (i in 0 until INTERVAL_TABLE.size - 1) {
            val (r0, t0) = INTERVAL_TABLE[i]
            val (r1, t1) = INTERVAL_TABLE[i + 1]
            if (rssi in r0..r1) {
                val fraction = (rssi - r0) / (r1 - r0)
                val interpolated = t0 + fraction * (t1 - t0)
                return interpolated.roundToLong()
            }
        }

        return 1200L
    }
}

