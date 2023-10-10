package com.exyte.mobile.raycast.painter

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.util.fastForEachIndexed
import com.exyte.mobile.raycast.rays.PolygonPoint

/*
 * Created by Exyte on 03.10.2023.
 */
internal class RaysPainter(raysCount: Int) {
    private val rayPaint = Paint()
    private var lines = FloatArray(raysCount * 4)

    init {
        rayPaint.apply {
            color = Color.Blue
            style = PaintingStyle.Stroke
            strokeWidth = 2f
        }
    }

    fun draw(rays: List<PolygonPoint>, origin: Offset, canvas: Canvas) {
        ensureLinesSize(rays.size)
        val lines = lines
        rays.fastForEachIndexed { i, polygonPoint ->
            lines[i * 4] = origin.x
            lines[i * 4 + 1] = origin.y
            lines[i * 4 + 2] = polygonPoint.x
            lines[i * 4 + 3] = polygonPoint.y
        }
        canvas.nativeCanvas.drawLines(
            lines,
            rayPaint.asFrameworkPaint()
        )
    }

    private fun ensureLinesSize(raysCount: Int) {
        if (lines.size != raysCount * 4) { //Because we have 2 point for one ray (x1, y1, x2, y2)
            lines = FloatArray(raysCount * 4)
        }
    }
}