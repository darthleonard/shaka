package com.darthleonard.shaka

import com.darthleonard.shaka.core.diagnostics.EventLogPolicy
import com.darthleonard.shaka.domain.model.DiagnosticEvent
import com.darthleonard.shaka.domain.model.EventCode
import com.darthleonard.shaka.domain.model.EventLogLevel
import com.darthleonard.shaka.domain.model.EventLogLimit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventLogPolicyTest {

    @Test
    fun testEvictionWhenLimitReached() {
        val policy = EventLogPolicy(EventLogLimit.LAST_5)

        for (i in 1..8) {
            policy.add(
                DiagnosticEvent(
                    id = i.toLong(),
                    level = EventLogLevel.INFO,
                    code = EventCode.WIFI_SCAN_COMPLETED,
                    timestampMs = 1000L * i,
                    message = "Event $i"
                )
            )
        }

        val snapshot = policy.snapshot()
        assertEquals(5, snapshot.size)
        // Oldest 3 events (1, 2, 3) must have been evicted; events 4..8 remain
        assertEquals(4L, snapshot.first().id)
        assertEquals(8L, snapshot.last().id)
    }

    @Test
    fun testDynamicResizeOnLimitChange() {
        val policy = EventLogPolicy(EventLogLimit.LAST_10)

        for (i in 1..10) {
            policy.add(
                DiagnosticEvent(
                    id = i.toLong(),
                    level = EventLogLevel.INFO,
                    code = EventCode.WIFI_SCAN_COMPLETED,
                    timestampMs = 1000L * i,
                    message = "Event $i"
                )
            )
        }

        assertEquals(10, policy.snapshot().size)

        // Lower limit to LAST_5
        val updated = policy.setLimit(EventLogLimit.LAST_5)
        assertEquals(5, updated.size)
        assertEquals(6L, updated.first().id)
        assertEquals(10L, updated.last().id)
    }

    @Test
    fun testClearRemovesAll() {
        val policy = EventLogPolicy(EventLogLimit.LAST_25)
        policy.add(
            DiagnosticEvent(
                id = 1L,
                level = EventLogLevel.INFO,
                code = EventCode.SONAR_STARTED,
                timestampMs = 1000L,
                message = "Started"
            )
        )
        assertTrue(policy.snapshot().isNotEmpty())

        val cleared = policy.clear()
        assertTrue(cleared.isEmpty())
        assertTrue(policy.snapshot().isEmpty())
    }
}

