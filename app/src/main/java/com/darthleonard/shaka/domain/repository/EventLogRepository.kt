package com.darthleonard.shaka.domain.repository

import com.darthleonard.shaka.domain.model.DiagnosticEvent
import com.darthleonard.shaka.domain.model.EventCode
import com.darthleonard.shaka.domain.model.EventLogLevel
import com.darthleonard.shaka.domain.model.EventLogLimit
import kotlinx.coroutines.flow.StateFlow

interface EventLogRepository {
    val events: StateFlow<List<DiagnosticEvent>>
    val limit: StateFlow<EventLogLimit>

    fun log(level: EventLogLevel, code: EventCode, message: String, details: String? = null)
    fun setLimit(newLimit: EventLogLimit)
    fun clearEvents()
}

