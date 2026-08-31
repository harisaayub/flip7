package com.speedo.gauge

/**
 * mps-to-unit multiplier and the analog gauge's full-scale value in that unit.
 * maxScale is chosen so the arc rarely pins at typical road speeds.
 */
enum class SpeedUnit(
    val label: String,
    val fromMps: Float,
    val maxScale: Float,
    val distanceLabel: String,
    val fromMeters: Double,
) {
    MPH(label = "mph", fromMps = 2.236936f, maxScale = 140f, distanceLabel = "mi", fromMeters = 1.0 / 1609.344),
    KMH(label = "km/h", fromMps = 3.6f, maxScale = 220f, distanceLabel = "km", fromMeters = 1.0 / 1000.0);

    fun convert(speedMps: Float): Float = speedMps * fromMps

    fun convertDistance(distanceMeters: Double): Double = distanceMeters * fromMeters
}
