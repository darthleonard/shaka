package com.darthleonard.shaka

import com.darthleonard.shaka.core.signal.SignalFilter
import com.darthleonard.shaka.core.signal.SignalMapper
import com.darthleonard.shaka.core.signal.SignalTrendDetector
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.SignalQuality
import com.darthleonard.shaka.domain.model.SignalTrend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalProcessingTest {

    @Test
    fun testRssiToPercentageMapping() {
        assertEquals(0.0f, SignalMapper.mapRssiToPercentage(-100f), 0.001f)
        assertEquals(1.0f, SignalMapper.mapRssiToPercentage(-30f), 0.001f)
        assertEquals(0.5f, SignalMapper.mapRssiToPercentage(-65f), 0.001f)

        // Clamping check
        assertEquals(0.0f, SignalMapper.mapRssiToPercentage(-120f), 0.001f)
        assertEquals(1.0f, SignalMapper.mapRssiToPercentage(-10f), 0.001f)
    }

    @Test
    fun testRssiToQualityMapping() {
        assertEquals(SignalQuality.EXCELLENT, SignalMapper.mapRssiToQuality(-45f))
        assertEquals(SignalQuality.GOOD, SignalMapper.mapRssiToQuality(-60f))
        assertEquals(SignalQuality.MODERATE, SignalMapper.mapRssiToQuality(-70f))
        assertEquals(SignalQuality.WEAK, SignalMapper.mapRssiToQuality(-80f))
        assertEquals(SignalQuality.CRITICAL, SignalMapper.mapRssiToQuality(-95f))
        assertEquals(SignalQuality.UNKNOWN, SignalMapper.mapRssiToQuality(-105f))
    }

    @Test
    fun testBeepIntervalInterpolation() {
        // Defined thresholds
        assertEquals(1200L, SignalMapper.mapRssiToBeepIntervalMs(-90f))
        assertEquals(1000L, SignalMapper.mapRssiToBeepIntervalMs(-80f))
        assertEquals(700L, SignalMapper.mapRssiToBeepIntervalMs(-70f))
        assertEquals(450L, SignalMapper.mapRssiToBeepIntervalMs(-60f))
        assertEquals(250L, SignalMapper.mapRssiToBeepIntervalMs(-50f))
        assertEquals(120L, SignalMapper.mapRssiToBeepIntervalMs(-40f))

        // Midpoint interpolation between -80 dBm (1000ms) and -70 dBm (700ms) -> at -75 dBm: 850ms
        assertEquals(850L, SignalMapper.mapRssiToBeepIntervalMs(-75f))

        // Weak signal beyond -90 dBm
        assertTrue(SignalMapper.mapRssiToBeepIntervalMs(-95f) > 1200L)

        // Strong signal beyond -40 dBm
        assertTrue(SignalMapper.mapRssiToBeepIntervalMs(-30f) <= 120L)
    }

    @Test
    fun testSignalFilterEma() {
        val filterHard = SignalFilter(ScanMode.HARD)
        val initial = filterHard.filter(-80)
        assertEquals(-80.0f, initial, 0.001f)

        // HARD mode alpha = 0.6: 0.6 * (-70) + 0.4 * (-80) = -42 + -32 = -74.0
        val second = filterHard.filter(-70)
        assertEquals(-74.0f, second, 0.001f)

        val filterSoft = SignalFilter(ScanMode.SOFT)
        filterSoft.filter(-80)
        // SOFT mode alpha = 0.25: 0.25 * (-70) + 0.75 * (-80) = -17.5 + -60 = -77.5
        val softSecond = filterSoft.filter(-70)
        assertEquals(-77.5f, softSecond, 0.001f)
    }

    @Test
    fun testSignalTrendDetector() {
        val trendDetector = SignalTrendDetector(thresholdDbm = 1.5f)

        assertEquals(SignalTrend.STABLE, trendDetector.updateTrend(-80f))
        // Small fluctuation (< 1.5 dBm) should be stable
        assertEquals(SignalTrend.STABLE, trendDetector.updateTrend(-79f))

        // Significant improvement
        assertEquals(SignalTrend.IMPROVING, trendDetector.updateTrend(-75f))

        // Significant deterioration
        assertEquals(SignalTrend.DETERIORATING, trendDetector.updateTrend(-82f))
    }
}

