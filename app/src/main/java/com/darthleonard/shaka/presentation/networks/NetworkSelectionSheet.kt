package com.darthleonard.shaka.presentation.networks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darthleonard.shaka.core.signal.SignalMapper
import com.darthleonard.shaka.domain.model.WifiNetwork
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
fun NetworkSelectionSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    networks: List<WifiNetwork>,
    selectedBssid: String?,
    onSelectNetwork: (WifiNetwork) -> Unit,
    onRefreshScan: () -> Unit,
    isScanning: Boolean,
    hasPermissions: Boolean = true,
    isWifiEnabled: Boolean = true,
    isLocationEnabled: Boolean = true,
    onRequestPermissions: () -> Unit = {}
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Seleccionar Red Wi-Fi",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Identificación precisa por BSSID (sin conectar)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF00E5FF),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onRefreshScan) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Escanear redes",
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (networks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        when {
                            !hasPermissions -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = SignalRed,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Permisos de ubicación requeridos",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Android exige permisos de ubicación para acceder a las redes Wi-Fi cercanas.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                Button(
                                    onClick = onRequestPermissions,
                                    colors = ButtonDefaults.buttonColors(containerColor = SignalRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Conceder permisos", color = Color.White)
                                }
                            }
                            !isWifiEnabled -> {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Wi-Fi desactivado",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Activa el Wi-Fi en los ajustes de tu dispositivo para detectar redes cercanas.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            !isLocationEnabled -> {
                                Icon(
                                    imageVector = Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ubicación (GPS) desactivada",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Android exige activar los Servicios de Ubicación en el sistema para que las aplicaciones puedan leer señales Wi-Fi.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            isScanning -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = Color(0xFF00E5FF),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Buscando redes Wi-Fi cercanas...",
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No se detectaron redes en este momento",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Pulsa el botón para solicitar un nuevo escaneo a Android",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )
                                OutlinedButton(
                                    onClick = onRefreshScan,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Escanear ahora", color = Color(0xFF00E5FF))
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(networks, key = { it.bssid }) { network ->
                        val isSelected = network.bssid.equals(selectedBssid, ignoreCase = true)
                        NetworkItemRow(
                            network = network,
                            isSelected = isSelected,
                            onSelect = {
                                onSelectNetwork(network)
                                onDismiss()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NetworkItemRow(
    network: WifiNetwork,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val quality = SignalMapper.mapRssiToQuality(network.rawRssi.toFloat())
    val qualityColor = when (quality) {
        com.darthleonard.shaka.domain.model.SignalQuality.EXCELLENT -> SignalGreen
        com.darthleonard.shaka.domain.model.SignalQuality.GOOD -> SignalGreen
        com.darthleonard.shaka.domain.model.SignalQuality.MODERATE -> SignalYellow
        com.darthleonard.shaka.domain.model.SignalQuality.WEAK -> SignalOrange
        else -> SignalRed
    }

    val borderColor = if (isSelected) Color(0xFF00E5FF) else DarkBorder
    val bgColor = if (isSelected) DarkSurfaceVariant else DarkSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
            tint = qualityColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = network.displayName,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = network.bssid,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "•",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "${network.formattedBand} (Ch ${network.channel})",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${network.rawRssi} dBm",
                color = qualityColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionada",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

