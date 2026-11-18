package com.darthleonard.shaka.domain.repository

import com.darthleonard.shaka.domain.model.FeedbackMode
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.EventLogLimit
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val scanMode: StateFlow<ScanMode>
    val feedbackMode: StateFlow<FeedbackMode>
    val eventLogLimit: StateFlow<EventLogLimit>
    val audioVolume: StateFlow<Float>

    fun updateScanMode(mode: ScanMode)
    fun updateFeedbackMode(mode: FeedbackMode)
    fun updateEventLogLimit(limit: EventLogLimit)
    fun updateAudioVolume(volume: Float)
}

