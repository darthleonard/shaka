package com.darthleonard.shaka.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.darthleonard.shaka.presentation.theme.DarkBorder
import com.darthleonard.shaka.presentation.theme.DarkSurfaceVariant
import com.darthleonard.shaka.presentation.theme.SignalGreen
import com.darthleonard.shaka.presentation.theme.SignalOrange
import com.darthleonard.shaka.presentation.theme.SignalRed
import com.darthleonard.shaka.presentation.theme.SignalYellow

@Composable
fun SignalBarIndicator(
    visualPercentage: Float,
    isStale: Boolean,
    modifier: Modifier = Modifier,
    segments: Int = 18
) {
    val animatedPercent by animateFloatAsState(
        targetValue = visualPercentage.coerceIn(0f, 1f),
        label = "BarAnimation"
    )

    val activeCount = (animatedPercent * segments).toInt()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(DarkSurfaceVariant),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (i in 0 until segments) {
            val isActive = i < activeCount
            val segmentRatio = i / segments.toFloat()

            val activeColor = when {
                isStale -> Color(0xFF64748B)
                segmentRatio < 0.25f -> SignalRed
                segmentRatio < 0.50f -> SignalOrange
                segmentRatio < 0.75f -> SignalYellow
                else -> SignalGreen
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isActive) activeColor else DarkBorder.copy(alpha = 0.5f))
            )
        }
    }
}

