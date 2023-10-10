package com.exyte.mobile.raycast

import android.content.res.Resources
import android.graphics.PointF
import android.text.TextPaint
import android.util.TypedValue
import androidx.core.os.BuildCompat
import androidx.graphics.path.PathSegment
import androidx.graphics.path.iterator
import com.exyte.mobile.raycast.rays.Polygon
import com.exyte.mobile.raycast.rays.PolygonPoint

import android.graphics.Path as PlatformPath

/*
 * Created by Exyte on 01.10.2023.
 */
data class TextToPolyResult(
    val textPath: PlatformPath,
    val polygons: List<Polygon>,
)

@OptIn(BuildCompat.PrereleaseSdkCheck::class)
class TextToPolygons(
    private val width: Int,
    private val height: Int,
) {
    fun convert(text: String, textSizeSp: Float = 80f): TextToPolyResult {
        val textPaint = TextPaint().apply {
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, Resources.getSystem().displayMetrics)
            textAlign = android.graphics.Paint.Align.CENTER
            letterSpacing = 0.09f
        }
        val textPath = PlatformPath()

        val bounds = android.graphics.Rect()
        textPaint.getTextBounds(
            text,
            0,
            text.length,
            bounds,
        )
        textPaint.getTextPath(
            text,
            0,
            text.length,
            width / 2f,
            (height + bounds.height()) / 2f + textSizeSp / 9f,
            textPath,
        )

        val polys = pathToPolys(textPath)

        return TextToPolyResult(textPath, polys)
    }

    companion object {

        @OptIn(BuildCompat.PrereleaseSdkCheck::class)
        fun pathToPolys(path: PlatformPath): List<Polygon> {
            val polys = mutableListOf<Polygon>()
            val points = mutableListOf<PointF>()

            fun addPolygon() {
                if (points.isNotEmpty()) {
                    val pol = Polygon().apply {
                        addPoints(points.map { PolygonPoint(it.x, it.y) })
                        finish()
                    }

                    points.clear()

                    polys.add(pol)
                }
            }

            for (s in path) {
                when (s.type) {
                    PathSegment.Type.Line -> {
                        val p1 = s.points[0]
                        val p2 = s.points[1]
                        points += p1
                        points += p2
                    }

                    PathSegment.Type.Quadratic -> {
                        val p1 = s.points[0]
                        val p2 = s.points[2]
                        points += p1
                        points += p2
                    }

                    PathSegment.Type.Conic -> {
                        val p1 = s.points[0]
                        val p2 = s.points[2]
                        points += p1
                        points += p2
                    }

                    PathSegment.Type.Cubic -> {
                        val p1 = s.points[0]
                        val p2 = s.points[3]
                        points += p1
                        points += p2
                    }

                    PathSegment.Type.Close -> {
                        addPolygon()
                    }

                    else -> {}
                }
            }

            addPolygon()

            return polys
        }
    }
}