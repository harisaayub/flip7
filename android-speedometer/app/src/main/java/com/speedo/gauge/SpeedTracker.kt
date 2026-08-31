package com.speedo.gauge

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SpeedState(
    val speedMps: Float = 0f,
    val hasFix: Boolean = false,
    val accuracyMeters: Float = -1f,
    val provider: String? = null,
    val hasSpeedFromProvider: Boolean = false,
    val speedAccuracyMps: Float = -1f,
    val bearingDeg: Float = -1f,
    val altitudeMeters: Double = Double.NaN,
    val latitude: Double = Double.NaN,
    val longitude: Double = Double.NaN,
    val updateCount: Int = 0,
    val lastFixAtMillis: Long = 0L,
    val distanceMeters: Double = 0.0,
    val providerAvailable: Boolean = true,
    val errorMessage: String? = null,
)

private const val HISTORY_MAX_SAMPLES = 240

/**
 * Reads device speed straight from LocationManager (no Play Services dependency).
 * Prefers the GPS-reported speed (Doppler-derived, more accurate at low speed);
 * falls back to distance/time between fixes when a provider doesn't report speed.
 * Also accumulates distance and a bounded speed/accel history for the debug and
 * history views.
 */
class SpeedTracker(context: Context) {

    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _state = MutableStateFlow(SpeedState())
    val state: StateFlow<SpeedState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<TelemetrySample>>(emptyList())
    val history: StateFlow<List<TelemetrySample>> = _history.asStateFlow()

    private var lastLocation: Location? = null
    private var activeProvider: String? = null
    private var totalDistanceMeters = 0.0
    private var updateCount = 0

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val previous = lastLocation
            val dtSeconds = previous?.let { (location.time - it.time) / 1000f } ?: 0f
            val distanceDeltaMeters = previous?.distanceTo(location)?.toDouble() ?: 0.0
            totalDistanceMeters += distanceDeltaMeters

            val speed = when {
                location.hasSpeed() && location.speed >= 0f -> location.speed
                dtSeconds > 0f -> (distanceDeltaMeters / dtSeconds).toFloat()
                else -> 0f
            }.coerceAtLeast(0f)

            val previousSpeed = _state.value.speedMps
            val accel = if (dtSeconds > 0f) (speed - previousSpeed) / dtSeconds else 0f

            updateCount += 1
            lastLocation = location
            val nowMillis = System.currentTimeMillis()

            _state.value = SpeedState(
                speedMps = speed,
                hasFix = true,
                accuracyMeters = if (location.hasAccuracy()) location.accuracy else -1f,
                provider = location.provider,
                hasSpeedFromProvider = location.hasSpeed(),
                speedAccuracyMps = if (location.hasSpeedAccuracy()) location.speedAccuracyMetersPerSecond else -1f,
                bearingDeg = if (location.hasBearing()) location.bearing else -1f,
                altitudeMeters = if (location.hasAltitude()) location.altitude else Double.NaN,
                latitude = location.latitude,
                longitude = location.longitude,
                updateCount = updateCount,
                lastFixAtMillis = nowMillis,
                distanceMeters = totalDistanceMeters,
                providerAvailable = true,
                errorMessage = null,
            )

            appendHistorySample(
                TelemetrySample(
                    atMillis = nowMillis,
                    speedMps = speed,
                    distanceMeters = totalDistanceMeters,
                    accelMps2 = accel,
                ),
            )
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderEnabled(provider: String) {
            if (provider == activeProvider) {
                _state.value = _state.value.copy(providerAvailable = true, errorMessage = null)
            }
        }

        override fun onProviderDisabled(provider: String) {
            if (provider == activeProvider) {
                _state.value = _state.value.copy(
                    hasFix = false,
                    providerAvailable = false,
                    errorMessage = "$provider was turned off",
                )
            }
        }
    }

    private fun appendHistorySample(sample: TelemetrySample) {
        val updated = _history.value + sample
        _history.value = if (updated.size > HISTORY_MAX_SAMPLES) {
            updated.takeLast(HISTORY_MAX_SAMPLES)
        } else {
            updated
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        activeProvider = provider
        if (provider == null) {
            _state.value = _state.value.copy(
                hasFix = false,
                providerAvailable = false,
                errorMessage = "No location provider is enabled on this device",
            )
            return
        }
        try {
            locationManager.requestLocationUpdates(
                provider,
                /* minTimeMs = */ 500L,
                /* minDistanceM = */ 0f,
                listener,
                Looper.getMainLooper(),
            )
        } catch (e: SecurityException) {
            _state.value = _state.value.copy(errorMessage = "Location permission denied: ${e.message}")
        }
    }

    fun stop() {
        locationManager.removeUpdates(listener)
        lastLocation = null
        activeProvider = null
    }

    /** Clears accumulated distance and history, keeping the tracker running. */
    fun resetSession() {
        totalDistanceMeters = 0.0
        updateCount = 0
        lastLocation = null
        _history.value = emptyList()
        _state.value = _state.value.copy(distanceMeters = 0.0, updateCount = 0)
    }
}
