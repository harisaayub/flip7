package com.speedo.gauge.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speedo.gauge.SpeedUnit
import com.speedo.gauge.ui.theme.Accent
import com.speedo.gauge.ui.theme.AccentDanger
import com.speedo.gauge.ui.theme.AccentWarn
import com.speedo.gauge.ui.theme.Surface
import com.speedo.gauge.ui.theme.TextPrimary
import com.speedo.gauge.ui.theme.TextSecondary
import com.speedo.gauge.ui.theme.TrackColor
import kotlin.math.roundToInt

@Composable
fun SpeedometerScreen(
    speedMps: Float,
    hasFix: Boolean,
    accuracyMeters: Float,
    unit: SpeedUnit,
    maxSpeedInUnit: Float,
    onToggleUnit: () -> Unit,
    onResetMax: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displaySpeed = unit.convert(speedMps)
    val animatedSpeed by animateFloatAsState(
        targetValue = displaySpeed,
        animationSpec = tween(durationMillis = 350),
        label = "speed",
    )

    Column(modifier = modifier.fillMaxSize()) {
        TopStatusRow(
            hasFix = hasFix,
            accuracyMeters = accuracyMeters,
            unit = unit,
            onToggleUnit = onToggleUnit,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            SpeedGauge(
                value = animatedSpeed,
                maxScale = unit.maxScale,
                progressColor = gaugeColorFor(animatedSpeed, unit.maxScale),
                trackColor = TrackColor,
                tickColor = TextSecondary,
                modifier = Modifier.fillMaxSize(),
            )
            SpeedReadout(speedValue = animatedSpeed, unitLabel = unit.label, hasFix = hasFix)
        }

        Spacer(modifier = Modifier.height(12.dp))

        MaxSpeedRow(maxSpeed = maxSpeedInUnit, unitLabel = unit.label, onResetMax = onResetMax)
    }
}

@Composable
private fun TopStatusRow(
    hasFix: Boolean,
    accuracyMeters: Float,
    unit: SpeedUnit,
    onToggleUnit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GpsStatusPill(hasFix = hasFix, accuracyMeters = accuracyMeters)
        UnitToggle(unit = unit, onToggleUnit = onToggleUnit)
    }
}

@Composable
private fun GpsStatusPill(hasFix: Boolean, accuracyMeters: Float) {
    val (dotColor, label) = when {
        !hasFix -> AccentDanger to "Searching"
        accuracyMeters in 0f..10f -> Accent to "GPS · Strong"
        accuracyMeters in 10f..30f -> AccentWarn to "GPS · Fair"
        else -> AccentDanger to "GPS · Weak"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun UnitToggle(unit: SpeedUnit, onToggleUnit: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .clickable(onClick = onToggleUnit)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text = unit.label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SpeedReadout(speedValue: Float, unitLabel: String, hasFix: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (hasFix) speedValue.roundToInt().toString() else "--",
            color = TextPrimary,
            fontSize = 76.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = unitLabel,
            color = TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun MaxSpeedRow(maxSpeed: Float, unitLabel: String, onResetMax: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .clickable(onClick = onResetMax)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "MAX", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = "${maxSpeed.roundToInt()} $unitLabel",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(text = "Reset", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
    }
}

private fun gaugeColorFor(value: Float, maxScale: Float): androidx.compose.ui.graphics.Color {
    val fraction = value / maxScale
    return when {
        fraction < 0.55f -> Accent
        fraction < 0.8f -> AccentWarn
        else -> AccentDanger
    }
}
