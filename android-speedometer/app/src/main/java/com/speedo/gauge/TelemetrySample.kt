package com.speedo.gauge

/** One point in the session's speed/distance/acceleration history. */
data class TelemetrySample(
    val atMillis: Long,
    val speedMps: Float,
    val distanceMeters: Double,
    val accelMps2: Float,
)
