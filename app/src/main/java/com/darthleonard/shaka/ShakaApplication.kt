package com.darthleonard.shaka

import android.app.Application
import com.darthleonard.shaka.core.audio.BeepController
import com.darthleonard.shaka.core.diagnostics.EventLogger
import com.darthleonard.shaka.core.permissions.WifiCapabilityDetector
import com.darthleonard.shaka.core.session.SessionManager
import com.darthleonard.shaka.data.settings.PreferencesRepositoryImpl
import com.darthleonard.shaka.data.wifi.WifiRepositoryImpl
import com.darthleonard.shaka.domain.repository.EventLogRepository
import com.darthleonard.shaka.domain.repository.SettingsRepository
import com.darthleonard.shaka.domain.repository.WifiRepository

class ShakaApplication : Application() {

    lateinit var eventLogRepository: EventLogRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var wifiRepository: WifiRepository
        private set

    lateinit var capabilityDetector: WifiCapabilityDetector
        private set

    lateinit var beepController: BeepController
        private set

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()

        // 1. Diagnostics & Preferences
        settingsRepository = PreferencesRepositoryImpl(this)
        val initialLimit = settingsRepository.eventLogLimit.value
        eventLogRepository = EventLogger(initialLimit)

        // 2. Wi-Fi & Audio Subsystems
        capabilityDetector = WifiCapabilityDetector(this)
        wifiRepository = WifiRepositoryImpl(
            context = this,
            eventLogger = eventLogRepository,
            capabilityDetector = capabilityDetector
        )

        beepController = BeepController(eventLogger = eventLogRepository)
        beepController.setVolume(settingsRepository.audioVolume.value)
        beepController.setFeedbackMode(settingsRepository.feedbackMode.value)

        // 3. Session Coordinator
        sessionManager = SessionManager(
            wifiRepository = wifiRepository,
            beepController = beepController,
            eventLogger = eventLogRepository
        )
    }
}

