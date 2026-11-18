package com.darthleonard.shaka.presentation.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.darthleonard.shaka.presentation.theme.DarkBorder
import com.darthleonard.shaka.presentation.theme.MeterArcModerate
import com.darthleonard.shaka.presentation.theme.MeterArcStrong
import com.darthleonard.shaka.presentation.theme.MeterArcWeak
import com.darthleonard.shaka.presentation.theme.MeterFaceColor
import com.darthleonard.shaka.presentation.theme.MeterNeedleCap
import com.darthleonard.shaka.presentation.theme.MeterNeedleColor
import com.darthleonard.shaka.presentation.theme.TextMuted
import com.darthleonard.shaka.presentation.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * An analog multimeter-style gauge representing Wi-Fi signal strength (RSSI).
 *
 * NOTE: This gauge represents signal intensity, NOT physical direction or location.
 */
@Composable
fun AnalogMeterGauge(
    visualPercentage: Float,
    isStale: Boolean,
    modifier: Modifier = Modifier,
    isScanning: Boolean = false,
    scanCount: Int = 0,
    lastScanTimestamp: Long = 0L,
    size: Dp = 260.dp
) {
    // 0.0 -> 150 deg, 1.0 -> 390 deg (sweep of 240 degrees)
    val startAngle = 150f
    val sweepAngle = 240f
    val targetAngle = startAngle + (visualPercentage.coerceIn(0f, 1f) * sweepAngle)

    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NeedleAngleAnimation"
    )

    // Spinner rotation around the center hub when scanning
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSpinner")
    val spinnerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinnerAngle"
    )

    // Ping wave pulse expanding outwards whenever network state is refreshed
    val pingAnim = remember { Animatable(0f) }
    LaunchedEffect(lastScanTimestamp) {
        if (lastScanTimestamp > 0L) {
            pingAnim.snapTo(1f)
            pingAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.toPx()
            val canvasHeight = size.toPx()
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            val radius = (minOf(canvasWidth, canvasHeight) / 2f) * 0.88f

            // 1. Draw outer meter face and border
            drawCircle(
                color = MeterFaceColor,
                radius = radius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = DarkBorder,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3.dp.toPx())
            )

            // Arc parameters
            val arcPadding = 26.dp.toPx()
            val arcRadius = radius - arcPadding
            val arcRect = Size(arcRadius * 2, arcRadius * 2)
            val arcTopLeft = Offset(centerX - arcRadius, centerY - arcRadius)
            val arcStrokeWidth = 7.dp.toPx()

            // 2. Colored zones along the scale:
            // -100 to -85 dBm: Weak (0% to 21.4%) -> 51.4 deg
            // -85 to -65 dBm: Moderate (21.4% to 50%) -> 68.6 deg
            // -65 to -30 dBm: Strong (50% to 100%) -> 120.0 deg
            val weakSweep = 51.4f
            val moderateSweep = 68.6f
            val strongSweep = 120.0f

            drawArc(
                color = MeterArcWeak,
                startAngle = startAngle,
                sweepAngle = weakSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = MeterArcModerate,
                startAngle = startAngle + weakSweep,
                sweepAngle = moderateSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Butt)
            )

            drawArc(
                color = MeterArcStrong,
                startAngle = startAngle + weakSweep + moderateSweep,
                sweepAngle = strongSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Round)
            )

            // 3. Scale graduations and dBm text markings
            val tickValues = listOf(-100, -90, -80, -70, -60, -50, -40, -30)
            val textPaint = Paint().apply {
                color = TextSecondary.toArgb()
                textSize = 9.dp.toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }

            for (i in 0..14) {
                val frac = i / 14f
                val angleDeg = startAngle + (frac * sweepAngle)
                val angleRad = angleDeg * (PI / 180f).toFloat()

                val isMajor = (i % 2 == 0)
                val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val tickColor = if (isMajor) TextSecondary else TextMuted
                val tickStroke = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                val startR = arcRadius - (arcStrokeWidth / 2f) - 3.dp.toPx()
                val endR = startR - tickLength

                val startTickX = centerX + startR * cos(angleRad)
                val startTickY = centerY + startR * sin(angleRad)
                val endTickX = centerX + endR * cos(angleRad)
                val endTickY = centerY + endR * sin(angleRad)

                drawLine(
                    color = tickColor,
                    start = Offset(startTickX, startTickY),
                    end = Offset(endTickX, endTickY),
                    strokeWidth = tickStroke
                )

                if (isMajor) {
                    val labelIndex = i / 2
                    if (labelIndex in tickValues.indices) {
                        val textRadius = endR - 10.dp.toPx()
                        val textX = centerX + textRadius * cos(angleRad)
                        val textY = centerY + textRadius * sin(angleRad) + (textPaint.textSize / 3f)

                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                tickValues[labelIndex].toString(),
                                textX,
                                textY,
                                textPaint
                            )
                        }
                    }
                }
            }

            // 4. Center meter face typography
            val centerLabelPaint = Paint().apply {
                color = TextMuted.toArgb()
                textSize = 10.dp.toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                letterSpacing = 0.15f
                isAntiAlias = true
            }

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "dBm",
                    centerX,
                    centerY - 22.dp.toPx(),
                    centerLabelPaint
                )
                canvas.nativeCanvas.drawText(
                    "SIGNAL INTENSITY",
                    centerX,
                    centerY + 32.dp.toPx(),
                    Paint().apply {
                        color = Color(0xFF475569).toArgb()
                        textSize = 8.dp.toPx()
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                        letterSpacing = 0.12f
                        isAntiAlias = true
                    }
                )

                if (scanCount > 0) {
                    val counterColor = if (isStale) TextMuted else Color(0xFF00E5FF).copy(alpha = 0.8f)
                    canvas.nativeCanvas.drawText(
                        "SCAN #$scanCount",
                        centerX,
                        centerY + 46.dp.toPx(),
                        Paint().apply {
                            color = counterColor.toArgb()
                            textSize = 8.dp.toPx()
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                            letterSpacing = 0.1f
                            isAntiAlias = true
                        }
                    )
                }
            }

            // 5. Radar spinner around the center hub & Ping pulse on refresh
            val spinnerRadius = 18.dp.toPx()
            // Ambient track
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = if (isScanning) 0.15f else 0.05f),
                radius = spinnerRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )

            // Rotating active arc
            if (isScanning) {
                drawArc(
                    color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                    startAngle = spinnerAngle,
                    sweepAngle = 75f,
                    useCenter = false,
                    topLeft = Offset(centerX - spinnerRadius, centerY - spinnerRadius),
                    size = Size(spinnerRadius * 2, spinnerRadius * 2),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Expanding ping wave when a new reading arrives
            if (pingAnim.value > 0f) {
                val progress = 1f - pingAnim.value
                val pingRadius = 12.dp.toPx() + (progress * 26.dp.toPx())
                val pingAlpha = (pingAnim.value * 0.7f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = pingAlpha),
                    radius = pingRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 6. Moving needle
            val needleAngleRad = animatedAngle * (PI / 180f).toFloat()
            val needleLength = arcRadius - 8.dp.toPx()
            val needleTipX = centerX + needleLength * cos(needleAngleRad)
            val needleTipY = centerY + needleLength * sin(needleAngleRad)

            // Needle shadow / base
            val needleColor = if (isStale) Color(0xFF64748B) else MeterNeedleColor
            drawLine(
                color = needleColor,
                start = Offset(centerX, centerY),
                end = Offset(needleTipX, needleTipY),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center pivot cap
            drawCircle(
                color = MeterNeedleCap,
                radius = 10.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = 6.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }
    }
}

