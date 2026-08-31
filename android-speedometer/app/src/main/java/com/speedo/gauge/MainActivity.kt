package com.speedo.gauge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speedo.gauge.ui.AppTab
import com.speedo.gauge.ui.AppTabRow
import com.speedo.gauge.ui.DebugPanel
import com.speedo.gauge.ui.HistoryScreen
import com.speedo.gauge.ui.SpeedometerScreen
import com.speedo.gauge.ui.theme.Accent
import com.speedo.gauge.ui.theme.Background
import com.speedo.gauge.ui.theme.SpeedometerTheme
import com.speedo.gauge.ui.theme.TextPrimary
import com.speedo.gauge.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var speedTracker: SpeedTracker
    private lateinit var unitsStore: UnitsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        speedTracker = SpeedTracker(this)
        unitsStore = UnitsStore(this)

        setContent {
            SpeedometerTheme {
                AppRoot(speedTracker = speedTracker, unitsStore = unitsStore)
            }
        }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun AppRoot(speedTracker: SpeedTracker, unitsStore: UnitsStore) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var permissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        permissionGranted = results.values.any { it }
    }

    val speedState by speedTracker.state.collectAsStateWithLifecycle()
    val history by speedTracker.history.collectAsStateWithLifecycle()
    val unit by unitsStore.unitFlow.collectAsStateWithLifecycle(initialValue = SpeedUnit.MPH)

    var maxSpeedMps by remember { mutableFloatStateOf(0f) }
    if (speedState.speedMps > maxSpeedMps) {
        maxSpeedMps = speedState.speedMps
    }

    var selectedTab by remember { mutableStateOf(AppTab.SPEED) }

    DisposableEffect(lifecycleOwner, permissionGranted) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (!permissionGranted) return@LifecycleEventObserver
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> speedTracker.start()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> speedTracker.stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (permissionGranted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .systemBarsPadding()
                .padding(20.dp),
        ) {
            AppTabRow(selected = selectedTab, onSelect = { selectedTab = it })
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    AppTab.SPEED -> SpeedometerScreen(
                        speedMps = speedState.speedMps,
                        hasFix = speedState.hasFix,
                        accuracyMeters = speedState.accuracyMeters,
                        unit = unit,
                        maxSpeedInUnit = unit.convert(maxSpeedMps),
                        onToggleUnit = {
                            scope.launch {
                                unitsStore.setUnit(if (unit == SpeedUnit.MPH) SpeedUnit.KMH else SpeedUnit.MPH)
                            }
                        },
                        onResetMax = { maxSpeedMps = 0f },
                    )
                    AppTab.HISTORY -> HistoryScreen(
                        samples = history,
                        unit = unit,
                        onResetSession = {
                            speedTracker.resetSession()
                            maxSpeedMps = 0f
                        },
                    )
                    AppTab.DEBUG -> DebugPanel(
                        state = speedState,
                        locationPermissionGranted = permissionGranted,
                    )
                }
            }
        }
    } else {
        LocationPermissionScreen(
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            },
        )
    }
}

@Composable
private fun LocationPermissionScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Background).padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Location access needed",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "This speedometer reads your speed from GPS. Grant location access to start.",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Text(text = "Enable location", color = Background, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
