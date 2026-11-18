package com.darthleonard.shaka.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darthleonard.shaka.presentation.theme.SignalOrange
import com.darthleonard.shaka.presentation.theme.SignalRed
import com.darthleonard.shaka.presentation.theme.TextPrimary

enum class BannerType {
    WARNING,
    ERROR,
    INFO
}

@Composable
fun StatusBanner(
    visible: Boolean,
    title: String,
    message: String,
    type: BannerType = BannerType.WARNING,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val (bgColor, borderColor, iconColor) = when (type) {
            BannerType.ERROR -> Triple(Color(0xFF2A1215), SignalRed, SignalRed)
            BannerType.WARNING -> Triple(Color(0xFF2B2010), SignalOrange, SignalOrange)
            BannerType.INFO -> Triple(Color(0xFF132338), Color(0xFF00E5FF), Color(0xFF00E5FF))
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (type == BannerType.INFO) Icons.Default.Info else Icons.Default.Warning,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            if (actionButton != null) {
                Spacer(modifier = Modifier.width(8.dp))
                actionButton()
            }
        }
    }
}

