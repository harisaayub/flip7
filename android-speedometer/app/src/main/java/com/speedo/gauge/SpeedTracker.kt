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
)

/**
 * Reads device speed straight from LocationManager (no Play Services dependency).
 * Prefers the GPS-reported speed (Doppler-derived, more accurate at low speed);
 * falls back to distance/time between fixes when a provider doesn't report speed.
 */
class SpeedTracker(context: Context) {

    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _state = MutableStateFlow(SpeedState())
    val state: StateFlow<SpeedState> = _state.asStateFlow()

    private var lastLocation: Location? = null
    private var activeProvider: String? = null

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val speed = when {
                location.hasSpeed() && location.speed >= 0f -> location.speed
                else -> speedFromDelta(location)
            }
            lastLocation = location
            _state.value = SpeedState(
                speedMps = speed.coerceAtLeast(0f),
                hasFix = true,
                accuracyMeters = if (location.hasAccuracy()) location.accuracy else -1f,
                provider = location.provider,
            )
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) {
            if (provider == activeProvider) {
                _state.value = _state.value.copy(hasFix = false)
            }
        }
    }

    private fun speedFromDelta(location: Location): Float {
        val previous = lastLocation ?: return 0f
        val dtSeconds = (location.time - previous.time) / 1000f
        if (dtSeconds <= 0f) return 0f
        return previous.distanceTo(location) / dtSeconds
    }

    @SuppressLint("MissingPermission")
    fun start() {
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        activeProvider = provider
        if (provider == null) return
        locationManager.requestLocationUpdates(
            provider,
            /* minTimeMs = */ 500L,
            /* minDistanceM = */ 0.5f,
            listener,
            Looper.getMainLooper(),
        )
    }

    fun stop() {
        locationManager.removeUpdates(listener)
        lastLocation = null
        activeProvider = null
    }
}
