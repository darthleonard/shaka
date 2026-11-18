package com.darthleonard.shaka

import com.darthleonard.shaka.data.wifi.HardScanStrategy
import com.darthleonard.shaka.data.wifi.SoftScanStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanStrategyTest {

    @Test
    fun testHardScanStrategyBackoff() {
        val strategy = HardScanStrategy()

        // No rejections: base interval 3000ms
        assertEquals(3000L, strategy.computeNextDelayMs(0))

        // 1 rejection: 3000 + 4000 = 7000ms
        assertEquals(7000L, strategy.computeNextDelayMs(1))

        // 2 rejections: 3000 + 2*4000 = 11000ms
        assertEquals(11000L, strategy.computeNextDelayMs(2))

        // Large number of rejections: capped at maxBackoffMs (20000ms)
        assertEquals(20000L, strategy.computeNextDelayMs(10))
    }

    @Test
    fun testSoftScanStrategyBackoff() {
        val strategy = SoftScanStrategy()

        // Base interval ~35000ms
        assertEquals(35000L, strategy.computeNextDelayMs(0))

        // 1 rejection: 35000 + 10000 = 45000ms
        assertEquals(45000L, strategy.computeNextDelayMs(1))

        // Capped at maxBackoffMs (60000ms)
        assertEquals(60000L, strategy.computeNextDelayMs(5))
    }
}

