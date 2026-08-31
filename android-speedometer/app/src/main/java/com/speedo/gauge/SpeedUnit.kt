package com.speedo.gauge

/**
 * mps-to-unit multiplier and the analog gauge's full-scale value in that unit.
 * maxScale is chosen so the arc rarely pins at typical road speeds.
 */
enum class SpeedUnit(val label: String, val fromMps: Float, val maxScale: Float) {
    MPH(label = "mph", fromMps = 2.236936f, maxScale = 140f),
    KMH(label = "km/h", fromMps = 3.6f, maxScale = 220f);

    fun convert(speedMps: Float): Float = speedMps * fromMps
}
