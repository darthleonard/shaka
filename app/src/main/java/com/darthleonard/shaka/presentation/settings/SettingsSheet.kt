package com.darthleonard.shaka.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darthleonard.shaka.domain.model.EventLogLimit
import com.darthleonard.shaka.domain.model.FeedbackMode
import com.darthleonard.shaka.domain.model.ScanMode
import com.darthleonard.shaka.presentation.theme.DarkBackground
import com.darthleonard.shaka.presentation.theme.DarkBorder
import com.darthleonard.shaka.presentation.theme.DarkSurface
import com.darthleonard.shaka.presentation.theme.DarkSurfaceVariant
import com.darthleonard.shaka.presentation.theme.TextMuted
import com.darthleonard.shaka.presentation.theme.TextPrimary
import com.darthleonard.shaka.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    currentScanMode: ScanMode,
    onScanModeChange: (ScanMode) -> Unit,
    currentFeedbackMode: FeedbackMode,
    onFeedbackModeChange: (FeedbackMode) -> Unit,
    currentLogLimit: EventLogLimit,
    onLogLimitChange: (EventLogLimit) -> Unit,
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configuración",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Scanning Mode
            SettingsSectionTitle(title = "MODO DE ESCANEO")
            SettingsOptionCard {
                ScanModeOption(
                    title = "HARD (Ágil)",
                    description = "Escaneo activo con mayor frecuencia permitida y filtro RSSI rápido",
                    selected = currentScanMode == ScanMode.HARD,
                    onSelect = { onScanModeChange(ScanMode.HARD) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ScanModeOption(
                    title = "SOFT (Conservador)",
                    description = "Escaneo cada 30-60s para mitigar throttling y filtro RSSI suavizado",
                    selected = currentScanMode == ScanMode.SOFT,
                    onSelect = { onScanModeChange(ScanMode.SOFT) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Feedback Mode
            SettingsSectionTitle(title = "MODO DE RESPUESTA")
            SettingsOptionCard {
                FeedbackModeOption(
                    title = "Visual + Audio (Predeterminado)",
                    selected = currentFeedbackMode == FeedbackMode.VISUAL_AND_AUDIO,
                    onSelect = { onFeedbackModeChange(FeedbackMode.VISUAL_AND_AUDIO) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                FeedbackModeOption(
                    title = "Solo Visual",
                    selected = currentFeedbackMode == FeedbackMode.VISUAL_ONLY,
                    onSelect = { onFeedbackModeChange(FeedbackMode.VISUAL_ONLY) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                FeedbackModeOption(
                    title = "Solo Audio",
                    selected = currentFeedbackMode == FeedbackMode.AUDIO_ONLY,
                    onSelect = { onFeedbackModeChange(FeedbackMode.AUDIO_ONLY) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Audio Volume
            SettingsSectionTitle(title = "VOLUMEN DE SONAR")
            SettingsOptionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Slider(
                        value = currentVolume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = DarkBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${(currentVolume * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. In-Memory Event Log Limit
            SettingsSectionTitle(title = "LÍMITE DE EVENTOS EN MEMORIA")
            SettingsOptionCard {
                Text(
                    text = "Los eventos se almacenan únicamente en memoria volátil y se eliminan al detener el sonar.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                EventLogLimit.entries.forEach { limit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogLimitChange(limit) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLogLimit == limit,
                            onClick = { onLogLimitChange(limit) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = limit.label,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. App Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SHAKA – Wi-Fi Signal Sonar v1.0",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "100% Local • Sin internet • Sin cuentas",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFF00E5FF),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SettingsOptionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
private fun ScanModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) DarkSurfaceVariant else Color.Transparent)
            .clickable { onSelect() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun FeedbackModeOption(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) DarkSurfaceVariant else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 13.sp
        )
    }
}

