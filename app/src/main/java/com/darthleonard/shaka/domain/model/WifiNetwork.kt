package com.darthleonard.shaka.domain.model

data class WifiNetwork(
    val bssid: String,
    val ssid: String,
    val rawRssi: Int,
    val frequencyMhz: Int,
    val channel: Int,
    val band: String,
    val timestampMicros: Long,
    val ageMs: Long,
    val isStale: Boolean
) {
    val displayName: String
        get() = if (ssid.isBlank()) "<Red Oculta>" else ssid

    val formattedBand: String
        get() = when {
            frequencyMhz in 2400..2499 -> "2.4 GHz"
            frequencyMhz in 4900..5900 -> "5 GHz"
            frequencyMhz in 5925..7125 -> "6 GHz"
            else -> "$frequencyMhz MHz"
        }
}

