package com.darthleonard.shaka.domain.model

data class SessionStats(
    val startTimeMs: Long = 0L,
    val durationMs: Long = 0L,
    val measurementCount: Int = 0,
    val activeScansRequested: Int = 0,
    val activeScansRejected: Int = 0,
    val minRssi: Int? = null,
    val maxRssi: Int? = null,
    val avgRssi: Float? = null
)

