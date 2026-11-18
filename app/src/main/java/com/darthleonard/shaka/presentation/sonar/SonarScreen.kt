package com.darthleonard.shaka.presentation.sonar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darthleonard.shaka.domain.model.FeedbackMode
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.domain.model.SignalQuality
import com.darthleonard.shaka.domain.model.SignalTrend
import com.darthleonard.shaka.domain.model.WifiNetwork
import com.darthleonard.shaka.presentation.components.AnalogMeterGauge
import com.darthleonard.shaka.presentation.components.BannerType
import com.darthleonard.shaka.presentation.components.SignalBarIndicator
import com.darthleonard.shaka.presentation.components.StatusBanner
import com.darthleonard.shaka.presentation.networks.NetworkSelectionSheet
import com.darthleonard.shaka.presentation.settings.SettingsSheet
import com.darthleonard.shaka.presentation.theme.DarkBackground
import com.darthleonard.shaka.presentation.theme.DarkBorder
import com.darthleonard.shaka.presentation.theme.DarkSurface
import com.darthleonard.shaka.presentation.theme.DarkSurfaceVariant
import com.darthleonard.shaka.presentation.theme.SignalGreen
import com.darthleonard.shaka.presentation.theme.SignalOrange
import com.darthleonard.shaka.presentation.theme.SignalRed
import com.darthleonard.shaka.presentation.theme.SignalYellow
import com.darthleonard.shaka.presentation.theme.TextMuted
import com.darthleonard.shaka.presentation.theme.TextPrimary
import com.darthleonard.shaka.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SonarScreen(
    viewModel: SonarViewModel,
    onRequestPermissions: () -> Unit
) {
    val isSessionActive by viewModel.isSessionActive.collectAsState()
    val signal by viewModel.currentSignal.collectAsState()
    val targetNetwork by viewModel.targetNetwork.collectAsState()
    val nearbyNetworks by viewModel.nearbyNetworks.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isRefreshingNetworks by viewModel.isRefreshingNetworks.collectAsState()
    val canStartSonar by viewModel.canStartSonar.collectAsState()
    val scanCount by viewModel.scanCount.collectAsState()
    val lastScanTimestamp by viewModel.lastScanTimestamp.collectAsState()

    val scanMode by viewModel.scanMode.collectAsState()
    val feedbackMode by viewModel.feedbackMode.collectAsState()
    val stats by viewModel.sessionStats.collectAsState()
    val events by viewModel.diagnosticEvents.collectAsState()
    val logLimit by viewModel.eventLogLimit.collectAsState()
    val volume by viewModel.audioVolume.collectAsState()

    val isWifiEnabled by viewModel.isWifiEnabled.collectAsState()
    val isLocationEnabled by viewModel.isLocationEnabled.collectAsState()
    val throttleStatus by viewModel.throttleStatus.collectAsState()
    val hasPermissions by viewModel.hasPermissions.collectAsState()

    var showNetworkSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Collapsible secondary panels states (collapsed by default)
    var isDiagExpanded by remember { mutableStateOf(false) }
    var isLogExpanded by remember { mutableStateOf(false) }
    var isStatsExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSessionActive) Color(0xFF00E5FF) else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHAKA SONAR",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Status Banners for Warnings
            StatusBanner(
                visible = !hasPermissions,
                title = "Permisos de escaneo requeridos",
                message = "Android requiere permisos de ubicación para escanear redes Wi-Fi.",
                type = BannerType.ERROR,
                actionButton = {
                    Button(
                        onClick = onRequestPermissions,
                        colors = ButtonDefaults.buttonColors(containerColor = SignalRed),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = "Conceder", fontSize = 11.sp)
                    }
                }
            )

            StatusBanner(
                visible = hasPermissions && !isWifiEnabled,
                title = "Wi-Fi desactivado",
                message = "Activa el Wi-Fi en los ajustes de tu dispositivo para usar el sonar.",
                type = BannerType.ERROR
            )

            StatusBanner(
                visible = hasPermissions && isWifiEnabled && !isLocationEnabled,
                title = "Servicios de ubicación desactivados",
                message = "Android exige activar el GPS/Ubicación para que las apps lean señales Wi-Fi.",
                type = BannerType.WARNING
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Selected Target Network Card
            TargetNetworkCard(
                target = targetNetwork,
                isSonarActive = isSessionActive,
                onClickSelect = {
                    // Must stop the sonar before changing the target network
                    if (isSessionActive) {
                        viewModel.toggleSonar()
                    }
                    viewModel.refreshNetworks()
                    showNetworkSheet = true
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Main Multimeter-Style Analog Gauge
            AnalogMeterGauge(
                visualPercentage = signal.visualPercentage,
                isStale = signal.isStale,
                isScanning = isSessionActive || isRefreshingNetworks,
                scanCount = scanCount,
                lastScanTimestamp = lastScanTimestamp,
                size = 280.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Secondary Horizontal Bar Indicator
            SignalBarIndicator(
                visualPercentage = signal.visualPercentage,
                isStale = signal.isStale,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Numeric RSSI, Quality & Trend Card
            SignalMetricsCard(
                rawRssi = signal.rawRssi,
                smoothedRssi = signal.smoothedRssi,
                quality = signal.quality,
                trend = signal.trend,
                ageMs = signal.ageMs,
                isStale = signal.isStale,
                isSonarActive = isSessionActive,
                scanCount = scanCount
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Mode & Feedback Selectors
            ControlBar(
                scanMode = scanMode,
                onScanModeChange = { viewModel.setScanMode(it) },
                feedbackMode = feedbackMode,
                onFeedbackModeChange = { viewModel.setFeedbackMode(it) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Start / Stop Button
            StartStopButton(
                isActive = isSessionActive,
                canStart = canStartSonar,
                onClick = { viewModel.toggleSonar() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 8. Secondary Collapsible Panels (Collapsed by default)
            DiagnosticsPanel(
                isExpanded = isDiagExpanded,
                onToggleExpand = { isDiagExpanded = !isDiagExpanded },
                isWifiEnabled = isWifiEnabled,
                isLocationEnabled = isLocationEnabled,
                throttleStatus = throttleStatus,
                hasPermissions = hasPermissions
            )

            Spacer(modifier = Modifier.height(10.dp))

            SessionStatsPanel(
                isExpanded = isStatsExpanded,
                onToggleExpand = { isStatsExpanded = !isStatsExpanded },
                stats = stats,
                isSonarActive = isSessionActive
            )

            Spacer(modifier = Modifier.height(10.dp))

            EventLogPanel(
                isExpanded = isLogExpanded,
                onToggleExpand = { isLogExpanded = !isLogExpanded },
                events = events,
                limit = logLimit
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Sheets
    NetworkSelectionSheet(
        isVisible = showNetworkSheet,
        onDismiss = { showNetworkSheet = false },
        networks = nearbyNetworks,
        selectedBssid = targetNetwork?.bssid,
        onSelectNetwork = { network ->
            viewModel.selectTargetNetwork(network)
        },
        onRefreshScan = { viewModel.refreshNetworks() },
        isScanning = isRefreshingNetworks,
        hasPermissions = hasPermissions,
        isWifiEnabled = isWifiEnabled,
        isLocationEnabled = isLocationEnabled,
        onRequestPermissions = onRequestPermissions
    )

    SettingsSheet(
        isVisible = showSettingsSheet,
        onDismiss = { showSettingsSheet = false },
        currentScanMode = scanMode,
        onScanModeChange = { viewModel.setScanMode(it) },
        currentFeedbackMode = feedbackMode,
        onFeedbackModeChange = { viewModel.setFeedbackMode(it) },
        currentLogLimit = logLimit,
        onLogLimitChange = { viewModel.setLogLimit(it) },
        currentVolume = volume,
        onVolumeChange = { viewModel.setVolume(it) }
    )
}

@Composable
private fun TargetNetworkCard(
    target: WifiNetwork?,
    isSonarActive: Boolean,
    onClickSelect: () -> Unit
) {
    val isLocked = isSonarActive && target != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(
                1.dp,
                when {
                    target == null -> DarkBorder
                    isLocked -> SignalOrange.copy(alpha = 0.6f)
                    else -> Color(0xFF00E5FF).copy(alpha = 0.5f)
                },
                RoundedCornerShape(10.dp)
            )
            .clickable { onClickSelect() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = if (target != null) Color(0xFF00E5FF) else TextMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                if (target == null) {
                    Text(
                        text = "Ninguna red seleccionada",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Toca aquí para elegir BSSID objetivo",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = target.displayName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${target.bssid} • ${target.formattedBand}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isLocked) SignalOrange.copy(alpha = 0.15f) else DarkSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = when {
                    target == null -> "Elegir"
                    isLocked -> "Detener y cambiar"
                    else -> "Cambiar"
                },
                color = if (isLocked) SignalOrange else Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SignalMetricsCard(
    rawRssi: Int,
    smoothedRssi: Float,
    quality: SignalQuality,
    trend: SignalTrend,
    ageMs: Long,
    isStale: Boolean,
    isSonarActive: Boolean,
    scanCount: Int = 0
) {
    val (qualityLabel, qualityColor) = when (quality) {
        SignalQuality.EXCELLENT -> "Excelente" to SignalGreen
        SignalQuality.GOOD -> "Buena" to SignalGreen
        SignalQuality.MODERATE -> "Aceptable" to SignalYellow
        SignalQuality.WEAK -> "Débil" to SignalOrange
        SignalQuality.CRITICAL -> "Crítica" to SignalRed
        SignalQuality.UNKNOWN -> "Sin señal" to Color(0xFF64748B)
    }

    val (trendLabel, trendIcon, trendColor) = when (trend) {
        SignalTrend.IMPROVING -> Triple("Mejorando", Icons.Default.ArrowUpward, SignalGreen)
        SignalTrend.DETERIORATING -> Triple("Deteriorando", Icons.Default.ArrowDownward, SignalRed)
        SignalTrend.STABLE -> Triple("Estable", Icons.Default.Remove, TextSecondary)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numeric RSSI readout
            Column {
                Text(
                    text = if (isSonarActive && rawRssi > -100) "$rawRssi dBm" else "-- dBm",
                    color = if (isStale) TextMuted else TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isSonarActive && rawRssi > -100) String.format(java.util.Locale.US, "Filtro: %.1f dBm", smoothedRssi) else "Esperando señal...",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quality Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(qualityColor.copy(alpha = 0.2f))
                    .border(1.dp, qualityColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = qualityLabel,
                    color = qualityColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trend
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " Tendencia: $trendLabel",
                    color = trendColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Age, staleness & scan counter
            val ageSeconds = (ageMs / 1000).toInt()
            val ageText = when {
                !isSonarActive -> "Inactivo"
                isStale -> "Desactualizado ($ageSeconds s)"
                ageSeconds <= 1 -> "Ahora mismo"
                else -> "Hace $ageSeconds s"
            }
            val ageColor = if (isStale) SignalOrange else TextSecondary

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSonarActive && scanCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isStale) SignalOrange else Color(0xFF00E5FF))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "#$scanCount • ",
                        color = if (isStale) TextMuted else Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = ageText,
                    color = ageColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ControlBar(
    scanMode: ScanMode,
    onScanModeChange: (ScanMode) -> Unit,
    feedbackMode: FeedbackMode,
    onFeedbackModeChange: (FeedbackMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode HARD / SOFT switch
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                .padding(3.dp)
        ) {
            ModeChip(
                label = "HARD",
                isSelected = scanMode == ScanMode.HARD,
                onClick = { onScanModeChange(ScanMode.HARD) },
                modifier = Modifier.weight(1f)
            )
            ModeChip(
                label = "SOFT",
                isSelected = scanMode == ScanMode.SOFT,
                onClick = { onScanModeChange(ScanMode.SOFT) },
                modifier = Modifier.weight(1f)
            )
        }

        // Feedback Mode Cycle
        val feedbackLabel = when (feedbackMode) {
            FeedbackMode.VISUAL_AND_AUDIO -> "Visual + Audio"
            FeedbackMode.VISUAL_ONLY -> "Solo Visual"
            FeedbackMode.AUDIO_ONLY -> "Solo Audio"
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                .clickable {
                    val next = when (feedbackMode) {
                        FeedbackMode.VISUAL_AND_AUDIO -> FeedbackMode.VISUAL_ONLY
                        FeedbackMode.VISUAL_ONLY -> FeedbackMode.AUDIO_ONLY
                        FeedbackMode.AUDIO_ONLY -> FeedbackMode.VISUAL_AND_AUDIO
                    }
                    onFeedbackModeChange(next)
                }
                .padding(vertical = 10.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = feedbackLabel,
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) Color(0xFF00E5FF) else Color.Transparent
    val textColor = if (isSelected) Color(0xFF0A0E17) else TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StartStopButton(
    isActive: Boolean,
    canStart: Boolean,
    onClick: () -> Unit
) {
    // Button is enabled when: sonar is running (to stop it), OR we can start (target selected)
    val isEnabled = isActive || canStart

    val buttonColor by animateColorAsState(
        targetValue = when {
            isActive -> SignalRed
            !canStart -> Color(0xFF334155)  // disabled / no target
            else -> Color(0xFF00E5FF)
        },
        label = "ButtonColor"
    )

    val contentColor = when {
        isActive -> Color.White
        !canStart -> TextMuted
        else -> Color(0xFF0A0E17)
    }

    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    isActive -> "DETENER SONAR"
                    !canStart -> "ELIGE UNA RED PRIMERO"
                    else -> "INICIAR SONAR"
                },
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

