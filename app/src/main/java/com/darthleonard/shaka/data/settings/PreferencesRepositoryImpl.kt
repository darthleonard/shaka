package com.darthleonard.shaka.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.darthleonard.shaka.domain.model.FeedbackMode
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.EventLogLimit
import com.darthleonard.shaka.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepositoryImpl(
    context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _scanMode = MutableStateFlow(readScanMode())
    override val scanMode: StateFlow<ScanMode> = _scanMode.asStateFlow()

    private val _feedbackMode = MutableStateFlow(readFeedbackMode())
    override val feedbackMode: StateFlow<FeedbackMode> = _feedbackMode.asStateFlow()

    private val _eventLogLimit = MutableStateFlow(readEventLogLimit())
    override val eventLogLimit: StateFlow<EventLogLimit> = _eventLogLimit.asStateFlow()

    private val _audioVolume = MutableStateFlow(readAudioVolume())
    override val audioVolume: StateFlow<Float> = _audioVolume.asStateFlow()

    override fun updateScanMode(mode: ScanMode) {
        _scanMode.value = mode
        prefs.edit().putString(KEY_SCAN_MODE, mode.name).apply()
    }

    override fun updateFeedbackMode(mode: FeedbackMode) {
        _feedbackMode.value = mode
        prefs.edit().putString(KEY_FEEDBACK_MODE, mode.name).apply()
    }

    override fun updateEventLogLimit(limit: EventLogLimit) {
        _eventLogLimit.value = limit
        prefs.edit().putInt(KEY_EVENT_LOG_LIMIT, limit.maxEvents).apply()
    }

    override fun updateAudioVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _audioVolume.value = clamped
        prefs.edit().putFloat(KEY_AUDIO_VOLUME, clamped).apply()
    }

    private fun readScanMode(): ScanMode {
        val raw = prefs.getString(KEY_SCAN_MODE, ScanMode.HARD.name)
        return try {
            ScanMode.valueOf(raw ?: ScanMode.HARD.name)
        } catch (_: Exception) {
            ScanMode.HARD
        }
    }

    private fun readFeedbackMode(): FeedbackMode {
        val raw = prefs.getString(KEY_FEEDBACK_MODE, FeedbackMode.VISUAL_AND_AUDIO.name)
        return try {
            FeedbackMode.valueOf(raw ?: FeedbackMode.VISUAL_AND_AUDIO.name)
        } catch (_: Exception) {
            FeedbackMode.VISUAL_AND_AUDIO
        }
    }

    private fun readEventLogLimit(): EventLogLimit {
        val count = prefs.getInt(KEY_EVENT_LOG_LIMIT, EventLogLimit.LAST_50.maxEvents)
        return EventLogLimit.fromMaxEvents(count)
    }

    private fun readAudioVolume(): Float {
        return prefs.getFloat(KEY_AUDIO_VOLUME, 1.0f).coerceIn(0f, 1f)
    }

    companion object {
        private const val PREFS_NAME = "shaka_settings"
        private const val KEY_SCAN_MODE = "key_scan_mode"
        private const val KEY_FEEDBACK_MODE = "key_feedback_mode"
        private const val KEY_EVENT_LOG_LIMIT = "key_event_log_limit"
        private const val KEY_AUDIO_VOLUME = "key_audio_volume"
    }
}

