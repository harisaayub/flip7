package com.speedo.gauge.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * A minimal sparkline-style line chart: normalizes [values] to the canvas height and
 * draws a single polyline. No axes/labels — callers add those around it.
 */
@Composable
fun LineChart(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas

        val minValue = values.min()
        val maxValue = values.max()
        val range = (maxValue - minValue).let { if (it < 0.0001f) 1f else it }
        val stepX = size.width / (values.size - 1)

        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val normalized = (value - minValue) / range
            val y = size.height - normalized * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
