package com.darthleonard.shaka.core.diagnostics

import com.darthleonard.shaka.domain.model.DiagnosticEvent
import com.darthleonard.shaka.domain.model.EventCode
import com.darthleonard.shaka.domain.model.EventLogLevel
import com.darthleonard.shaka.domain.model.EventLogLimit
import com.darthleonard.shaka.domain.repository.EventLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

class EventLogPolicy(
    private var limit: EventLogLimit = EventLogLimit.LAST_50
) {
    private val lock = Any()
    private val buffer = ArrayDeque<DiagnosticEvent>()

    fun setLimit(newLimit: EventLogLimit): List<DiagnosticEvent> = synchronized(lock) {
        limit = newLimit
        enforceLimitLocked()
        buffer.toList()
    }

    fun add(event: DiagnosticEvent): List<DiagnosticEvent> = synchronized(lock) {
        buffer.addLast(event)
        enforceLimitLocked()
        buffer.toList()
    }

    fun clear(): List<DiagnosticEvent> = synchronized(lock) {
        buffer.clear()
        emptyList()
    }

    fun snapshot(): List<DiagnosticEvent> = synchronized(lock) {
        buffer.toList()
    }

    private fun enforceLimitLocked() {
        val maxAllowed = limit.maxEvents
        while (buffer.size > maxAllowed) {
            buffer.removeFirst()
        }
    }
}

class EventLogger(
    initialLimit: EventLogLimit = EventLogLimit.LAST_50
) : EventLogRepository {

    private val policy = EventLogPolicy(initialLimit)
    private val idCounter = AtomicLong(1L)

    private val _limit = MutableStateFlow(initialLimit)
    override val limit: StateFlow<EventLogLimit> = _limit.asStateFlow()

    private val _events = MutableStateFlow<List<DiagnosticEvent>>(emptyList())
    override val events: StateFlow<List<DiagnosticEvent>> = _events.asStateFlow()

    override fun log(
        level: EventLogLevel,
        code: EventCode,
        message: String,
        details: String?
    ) {
        val event = DiagnosticEvent(
            id = idCounter.getAndIncrement(),
            level = level,
            code = code,
            timestampMs = System.currentTimeMillis(),
            message = message,
            details = details
        )
        _events.value = policy.add(event)
    }

    override fun setLimit(newLimit: EventLogLimit) {
        _limit.value = newLimit
        _events.value = policy.setLimit(newLimit)
    }

    override fun clearEvents() {
        _events.value = policy.clear()
    }
}

