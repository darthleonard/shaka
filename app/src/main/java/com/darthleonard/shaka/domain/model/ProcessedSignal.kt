package com.darthleonard.shaka.domain.model

data class ProcessedSignal(
    val rawRssi: Int,
    val smoothedRssi: Float,
    val visualPercentage: Float,
    val quality: SignalQuality,
    val trend: SignalTrend,
    val ageMs: Long,
    val isStale: Boolean,
    val beepIntervalMs: Long
)

