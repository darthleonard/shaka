package com.darthleonard.shaka.data.wifi

import com.darthleonard.shaka.domain.model.ScanMode

interface ScanStrategy {
    val mode: ScanMode
    val baseScanIntervalMs: Long
    val maxBackoffMs: Long

    fun computeNextDelayMs(consecutiveRejections: Int): Long
}

class HardScanStrategy : ScanStrategy {
    override val mode: ScanMode = ScanMode.HARD
    override val baseScanIntervalMs: Long = 3_000L   // 3 s — tightest practical interval before throttling
    override val maxBackoffMs: Long = 20_000L

    override fun computeNextDelayMs(consecutiveRejections: Int): Long {
        if (consecutiveRejections <= 0) return baseScanIntervalMs
        val backoff = baseScanIntervalMs + (consecutiveRejections * 4_000L)
        return backoff.coerceAtMost(maxBackoffMs)
    }
}

class SoftScanStrategy : ScanStrategy {
    override val mode: ScanMode = ScanMode.SOFT
    override val baseScanIntervalMs: Long = 35_000L
    override val maxBackoffMs: Long = 60_000L

    override fun computeNextDelayMs(consecutiveRejections: Int): Long {
        if (consecutiveRejections <= 0) return baseScanIntervalMs
        val backoff = baseScanIntervalMs + (consecutiveRejections * 10_000L)
        return backoff.coerceAtMost(maxBackoffMs)
    }
}

object ScanStrategyFactory {
    fun create(mode: ScanMode): ScanStrategy = when (mode) {
        ScanMode.HARD -> HardScanStrategy()
        ScanMode.SOFT -> SoftScanStrategy()
    }
}

