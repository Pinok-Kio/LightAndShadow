package com.exyte.mobile.raycast.painter

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.asComposePath
import com.exyte.mobile.raycast.rays.Polygon

/*
 * Created by Exyte on 03.10.2023.
 */
internal class LegacyPainter(
    private val width: Int,
    private val height: Int,
) : AbstractPainter() {
    override var lightRadius: Float = DEFAULT_RAY_LENGTH
    private var shadowPath1: Path? = null
    private var shadowPath2: Path? = null
    private var lightPath1: Path? = null
    private var lightPath2: Path? = null

    init {
        lightPaint.blendMode = BlendMode.Src
        shadowPaint.blendMode = BlendMode.Src
    }

    override fun onLightSourcePositionChanged(x: Float, y: Float, shadowPolygon: Polygon, lightPolygon: Polygon) {
        if (lightPath1 == null) {
            lightPath1 = lightPolygon.path.asComposePath()
            lightPath2 = null
        } else {
            lightPath1 = null
            lightPath2 = lightPolygon.path.asComposePath()
        }

        if (shadowPath1 == null) {
            shadowPath1 = shadowPolygon.path.asComposePath()
            shadowPath2 = null
        } else {
            shadowPath1 = null
            shadowPath2 = shadowPolygon.path.asComposePath()
        }
    }

    override fun draw(origin: Offset, shadowPolygon: Polygon, lightPolygon: Polygon, canvas: Canvas) {
        val lp = lightPath1 ?: lightPath2 ?: return
        val sp = shadowPath1 ?: shadowPath2 ?: return
        val r = lightRadius

        lightPaint.shader = RadialGradientShader(
            center = origin,
            radius = r,
            colors = lightGradientColors,
        )

        shadowPaint.shader = RadialGradientShader(
            center = origin,
            radius = r,
            colors = shadowGradientColors,
        )

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), outerShadowPaint)

        canvas.drawPath(sp, shadowPaint)

        canvas.drawPath(lp, lightPaint)
    }
}