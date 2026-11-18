package com.darthleonard.shaka.domain.model

data class DiagnosticEvent(
    val id: Long,
    val level: EventLogLevel,
    val code: EventCode,
    val timestampMs: Long,
    val message: String,
    val details: String? = null
)

