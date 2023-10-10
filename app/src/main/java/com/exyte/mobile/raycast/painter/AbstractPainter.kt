package com.exyte.mobile.raycast.painter

import android.os.Build
import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import com.exyte.mobile.raycast.rays.Polygon

/*
 * Created by Exyte on 03.10.2023.
 */

fun getPainter(width: Int, height: Int): Painter =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        RenderNodePainter(width, height)
    } else {
        LegacyPainter(width, height)
    }

interface Painter {
    var lightRadius: Float

    fun setBlurRadius(@FloatRange(from = 0.0, to = 25.0) r: Float) {

    }

    fun onLightSourcePositionChanged(x: Float, y: Float, shadowPolygon: Polygon, lightPolygon: Polygon) {

    }

    fun draw(origin: Offset, shadowPolygon: Polygon, lightPolygon: Polygon, canvas: Canvas)
}

internal abstract class AbstractPainter : Painter {
    protected val shadowPaint = Paint()
    protected val lightPaint = Paint()
    protected val outerShadowPaint = Paint()
    protected val outerShadowColor = Color.Black.copy(alpha = 0.7f)

    protected val lightGradientColors = listOf(
        Color.Yellow.copy(alpha = 0.35f),
        Color.Yellow.copy(alpha = 0.15f),
        Color.Yellow.copy(alpha = 0.05f),
        outerShadowColor,
    )

    protected val shadowGradientColors = listOf(
        Color.Black,
        Color.Black.copy(alpha = 0.82f),
        outerShadowColor,
    )

    init {
        outerShadowPaint.color = outerShadowColor
    }

    protected companion object {
        const val DEFAULT_RAY_LENGTH = 1500f
    }
}