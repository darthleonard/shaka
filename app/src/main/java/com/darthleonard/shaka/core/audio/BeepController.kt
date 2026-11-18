package com.darthleonard.shaka.core.audio

import com.darthleonard.shaka.domain.model.EventCode
import com.darthleonard.shaka.domain.model.EventLogLevel
import com.darthleonard.shaka.domain.model.FeedbackMode
import com.darthleonard.shaka.domain.repository.EventLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BeepController(
    private val eventLogger: EventLogRepository? = null,
    private val generator: BeepGenerator = BeepGenerator()
) {
    private var activeScope: CoroutineScope? = null
    private var playbackJob: Job? = null
    private var currentIntervalMs: Long = 1200L
    private var currentFeedbackMode: FeedbackMode = FeedbackMode.VISUAL_AND_AUDIO
    private var currentVolume: Float = 1.0f
    private var isSonarActive: Boolean = false

    fun setFeedbackMode(mode: FeedbackMode) {
        currentFeedbackMode = mode
        if (mode == FeedbackMode.VISUAL_ONLY) {
            stopPlayback()
        } else if (isSonarActive && playbackJob == null) {
            activeScope?.let { startLoop(it) }
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume
        generator.setVolume(volume)
    }

    fun updateInterval(intervalMs: Long) {
        currentIntervalMs = intervalMs.coerceIn(80L, 2500L)
    }

    fun start(scope: CoroutineScope, initialIntervalMs: Long = 1200L) {
        activeScope = scope
        isSonarActive = true
        currentIntervalMs = initialIntervalMs

        if (currentFeedbackMode == FeedbackMode.VISUAL_ONLY) {
            return
        }

        try {
            generator.initialize(currentVolume)
        } catch (e: Exception) {
            eventLogger?.log(
                level = EventLogLevel.ERROR,
                code = EventCode.AUDIO_INITIALIZATION_FAILED,
                message = "Fallo al inicializar el subsistema de audio",
                details = e.localizedMessage
            )
            return
        }

        startLoop(scope)
    }

    private fun startLoop(scope: CoroutineScope) {
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.Default) {
            try {
                while (isActive && isSonarActive && currentFeedbackMode != FeedbackMode.VISUAL_ONLY) {
                    generator.playBeep()
                    val sleepTime = currentIntervalMs
                    delay(sleepTime)
                }
            } catch (_: Exception) {
            } finally {
                generator.stop()
            }
        }
    }

    fun stop() {
        isSonarActive = false
        stopPlayback()
        activeScope = null
        generator.release()
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        generator.stop()
    }
}
