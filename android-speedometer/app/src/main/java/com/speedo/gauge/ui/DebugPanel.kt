package com.speedo.gauge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speedo.gauge.SpeedState
import com.speedo.gauge.ui.theme.AccentDanger
import com.speedo.gauge.ui.theme.Surface
import com.speedo.gauge.ui.theme.TextPrimary
import com.speedo.gauge.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun DebugPanel(
    state: SpeedState,
    locationPermissionGranted: Boolean,
    modifier: Modifier = Modifier,
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    val lastFixAgeSeconds = if (state.lastFixAtMillis > 0) {
        (nowMillis - state.lastFixAtMillis) / 1000
    } else {
        null
    }

    val rows = buildList {
        add("Permission granted" to locationPermissionGranted.toString())
        add("Provider" to (state.provider ?: "none"))
        add("Provider available" to state.providerAvailable.toString())
        add("Has fix" to state.hasFix.toString())
        add("Fix count" to state.updateCount.toString())
        add("Last fix age" to (lastFixAgeSeconds?.let { "${it}s ago" } ?: "never"))
        add("Speed from provider" to state.hasSpeedFromProvider.toString())
        add("Accuracy" to if (state.accuracyMeters >= 0) "${"%.1f".format(state.accuracyMeters)} m" else "n/a")
        add(
            "Speed accuracy" to if (state.speedAccuracyMps >= 0) "${"%.2f".format(state.speedAccuracyMps)} m/s" else "n/a",
        )
        add("Bearing" to if (state.bearingDeg >= 0) "${"%.0f".format(state.bearingDeg)}°" else "n/a")
        add(
            "Altitude" to if (!state.altitudeMeters.isNaN()) "${"%.0f".format(state.altitudeMeters)} m" else "n/a",
        )
        add(
            "Latitude" to if (!state.latitude.isNaN()) "%.5f".format(state.latitude) else "n/a",
        )
        add(
            "Longitude" to if (!state.longitude.isNaN()) "%.5f".format(state.longitude) else "n/a",
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        state.errorMessage?.let { message ->
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .padding(16.dp),
                ) {
                    Text(text = "Error", color = AccentDanger, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = message, color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .padding(16.dp),
            ) {
                rows.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = label, color = TextSecondary, fontSize = 13.sp)
                        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
