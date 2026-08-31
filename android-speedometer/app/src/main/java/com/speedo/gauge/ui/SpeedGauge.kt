package com.speedo.gauge.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val START_ANGLE = 135f
private const val SWEEP_ANGLE = 270f
private const val TICK_STEP_FRACTION = 0.05f

/**
 * Analog-style arc gauge: dark track, a colored progress sweep, and radial tick marks.
 * Angle convention follows Canvas.drawArc: 0deg = 3 o'clock, clockwise.
 */
@Composable
fun SpeedGauge(
    value: Float,
    maxScale: Float,
    progressColor: Color,
    trackColor: Color,
    tickColor: Color,
    modifier: Modifier = Modifier,
) {
    val fraction = (value / maxScale).coerceIn(0f, 1f)

    Canvas(modifier = modifier.fillMaxSize().aspectRatio(1f)) {
        val strokeWidth = size.minDimension * 0.045f
        val diameter = min(size.width, size.height) - strokeWidth
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f,
        )
        val arcSize = Size(diameter, diameter)
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Background track.
        drawArc(
            color = trackColor,
            startAngle = START_ANGLE,
            sweepAngle = SWEEP_ANGLE,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Progress sweep representing current speed.
        drawArc(
            color = progressColor,
            startAngle = START_ANGLE,
            sweepAngle = SWEEP_ANGLE * fraction,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Radial tick marks around the outside of the track.
        var t = 0f
        while (t <= 1f + 1e-4f) {
            val angleDeg = START_ANGLE + SWEEP_ANGLE * t
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val isMajor = (t / TICK_STEP_FRACTION).let { it.mod(2f) < 0.01f }
            val outerR = radius + strokeWidth * 0.75f
            val innerR = outerR - (if (isMajor) strokeWidth * 0.6f else strokeWidth * 0.3f)
            val cosA = cos(angleRad).toFloat()
            val sinA = sin(angleRad).toFloat()
            drawLine(
                color = tickColor,
                start = Offset(center.x + innerR * cosA, center.y + innerR * sinA),
                end = Offset(center.x + outerR * cosA, center.y + outerR * sinA),
                strokeWidth = if (isMajor) 3f else 1.5f,
            )
            t += TICK_STEP_FRACTION
        }
    }
}
