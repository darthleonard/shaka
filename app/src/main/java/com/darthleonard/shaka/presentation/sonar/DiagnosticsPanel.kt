package com.darthleonard.shaka.presentation.sonar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darthleonard.shaka.domain.model.ThrottleStatus
import com.darthleonard.shaka.presentation.theme.DarkBorder
import com.darthleonard.shaka.presentation.theme.DarkSurface
import com.darthleonard.shaka.presentation.theme.DarkSurfaceVariant
import com.darthleonard.shaka.presentation.theme.SignalGreen
import com.darthleonard.shaka.presentation.theme.SignalOrange
import com.darthleonard.shaka.presentation.theme.SignalRed
import com.darthleonard.shaka.presentation.theme.TextMuted
import com.darthleonard.shaka.presentation.theme.TextPrimary
import com.darthleonard.shaka.presentation.theme.TextSecondary

@Composable
fun DiagnosticsPanel(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isWifiEnabled: Boolean,
    isLocationEnabled: Boolean,
    throttleStatus: ThrottleStatus,
    hasPermissions: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = " Diagnóstico de Capacidades",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiagnosticRow(
                    label = "Estado de Wi-Fi",
                    statusText = if (isWifiEnabled) "Activado" else "Desactivado",
                    statusColor = if (isWifiEnabled) SignalGreen else SignalRed,
                    icon = if (isWifiEnabled) Icons.Default.CheckCircle else Icons.Default.Error
                )

                DiagnosticRow(
                    label = "Servicio de Ubicación",
                    statusText = if (isLocationEnabled) "Activado" else "Desactivado (requerido)",
                    statusColor = if (isLocationEnabled) SignalGreen else SignalRed,
                    icon = if (isLocationEnabled) Icons.Default.CheckCircle else Icons.Default.Error
                )

                val (throttleText, throttleColor) = when (throttleStatus) {
                    ThrottleStatus.ENABLED -> "Activa (4 escaneos / 2 min)" to SignalOrange
                    ThrottleStatus.DISABLED -> "Desactivada" to SignalGreen
                    ThrottleStatus.UNKNOWN -> "No expuesta por el SO (API < 30)" to TextMuted
                }

                DiagnosticRow(
                    label = "Limitación de Escaneo",
                    statusText = throttleText,
                    statusColor = throttleColor,
                    icon = if (throttleStatus == ThrottleStatus.UNKNOWN) Icons.Default.Help else Icons.Default.CheckCircle
                )

                DiagnosticRow(
                    label = "Permisos de Escaneo",
                    statusText = if (hasPermissions) "Concedidos" else "Faltantes",
                    statusColor = if (hasPermissions) SignalGreen else SignalRed,
                    icon = if (hasPermissions) Icons.Default.CheckCircle else Icons.Default.Error
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Important Technical Disclaimer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Aviso técnico:",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Android no provee mediciones angulares para ubicar físicamente el Punto de Acceso con RSSI ordinario. SHAKA mide exclusivamente intensidad de señal para guiarte por aproximación, sin estimar dirección ni brújula.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    statusText: String,
    statusColor: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = " $statusText",
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

