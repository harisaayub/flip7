package com.speedo.gauge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speedo.gauge.SpeedUnit
import com.speedo.gauge.TelemetrySample
import com.speedo.gauge.ui.theme.Accent
import com.speedo.gauge.ui.theme.AccentDanger
import com.speedo.gauge.ui.theme.AccentWarn
import com.speedo.gauge.ui.theme.Surface
import com.speedo.gauge.ui.theme.TextPrimary
import com.speedo.gauge.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    samples: List<TelemetrySample>,
    unit: SpeedUnit,
    onResetSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ChartCard(
                title = "Distance",
                unitLabel = unit.distanceLabel,
                latestValue = unit.convertDistance(samples.lastOrNull()?.distanceMeters ?: 0.0).toFloat(),
                values = samples.map { unit.convertDistance(it.distanceMeters).toFloat() },
                color = Accent,
            )
        }
        item {
            ChartCard(
                title = "Speed",
                unitLabel = unit.label,
                latestValue = unit.convert(samples.lastOrNull()?.speedMps ?: 0f),
                values = samples.map { unit.convert(it.speedMps) },
                color = AccentWarn,
            )
        }
        item {
            ChartCard(
                title = "Acceleration",
                unitLabel = "m/s²",
                latestValue = samples.lastOrNull()?.accelMps2 ?: 0f,
                values = samples.map { it.accelMps2 },
                color = AccentDanger,
            )
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .clickable(onClick = onResetSession)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Reset session history", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    unitLabel: String,
    latestValue: Float,
    values: List<Float>,
    color: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${formatValue(latestValue)} $unitLabel",
                color = TextSecondary,
                fontSize = 13.sp,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (values.size < 2) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Not enough data yet", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LineChart(
                values = values,
                color = color,
                modifier = Modifier.fillMaxWidth().height(80.dp),
            )
        }
    }
}

private fun formatValue(value: Float): String {
    val rounded = (value * 10).roundToInt() / 10f
    return if (rounded == rounded.roundToInt().toFloat()) {
        rounded.roundToInt().toString()
    } else {
        rounded.toString()
    }
}
