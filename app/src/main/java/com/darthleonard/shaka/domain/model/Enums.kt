package com.darthleonard.shaka.domain.model

enum class ScanMode {
    HARD,
    SOFT
}

enum class FeedbackMode {
    VISUAL_AND_AUDIO,
    VISUAL_ONLY,
    AUDIO_ONLY
}

enum class SignalQuality {
    EXCELLENT,
    GOOD,
    MODERATE,
    WEAK,
    CRITICAL,
    UNKNOWN
}

enum class SignalTrend {
    IMPROVING,
    DETERIORATING,
    STABLE
}

enum class ThrottleStatus {
    ENABLED,
    DISABLED,
    UNKNOWN
}

enum class EventLogLevel {
    INFO,
    WARNING,
    ERROR,
    DEBUG
}

enum class EventCode {
    SONAR_STARTED,
    SONAR_STOPPED,
    WIFI_SCAN_REQUESTED,
    WIFI_SCAN_COMPLETED,
    WIFI_SCAN_THROTTLED,
    WIFI_SCAN_FAILED,
    STALE_SCAN_RESULTS,
    TARGET_BSSID_NOT_FOUND,
    TARGET_BSSID_DETECTED,
    PERMISSION_DENIED,
    PERMISSION_GRANTED,
    WIFI_DISABLED,
    WIFI_ENABLED,
    LOCATION_DISABLED,
    AUDIO_INITIALIZATION_FAILED,
    AUDIO_STOPPED,
    SCAN_MODE_CHANGED,
    FEEDBACK_MODE_CHANGED,
    NETWORK_SELECTED,
    LOG_LIMIT_CHANGED
}

enum class EventLogLimit(val maxEvents: Int, val label: String) {
    LAST_5(5, "Últimos 5"),
    LAST_10(10, "Últimos 10"),
    LAST_25(25, "Últimos 25"),
    LAST_50(50, "Últimos 50"),
    LAST_100(100, "Últimos 100"),
    UNLIMITED(500, "Sin límite (máx. 500)");

    companion object {
        fun fromMaxEvents(count: Int): EventLogLimit =
            entries.find { it.maxEvents == count } ?: LAST_50
    }
}

