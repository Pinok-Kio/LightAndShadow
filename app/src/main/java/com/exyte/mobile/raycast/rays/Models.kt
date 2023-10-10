package com.exyte.mobile.raycast.rays

import android.graphics.Path
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2

/*
 * Created by Exyte on 28.09.2023.
 */
data class LineSegment(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
)

fun PolygonPoint(x: Float, y: Float) = PolygonPoint(packFloats(x, y))

@JvmInline
value class PolygonPoint(private val packedValue: Long) {
    val x: Float get() = unpackFloat1(packedValue)
    val y: Float get() = unpackFloat2(packedValue)
}

class Polygon {
    val points = ArrayList<PolygonPoint>()
    val path = Path()

    fun addPoints(points: List<PolygonPoint>) {
        this.points += points
    }

    fun addPoint(point: PolygonPoint) {
        this.points += point
    }

    fun reset() {
        path.rewind()
        points.clear()
    }

    fun finish() {
        if (points.isEmpty()) return

        val start = points.first()
        path.moveTo(start.x, start.y)

        points.fastForEachIndexed { i, p ->
            if (i > 0) {
                path.lineTo(p.x, p.y)
            }
        }

        path.close()
    }
}