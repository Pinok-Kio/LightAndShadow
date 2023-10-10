package com.exyte.mobile.raycast.painter

import android.graphics.RecordingCanvas
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.nativeCanvas
import com.exyte.mobile.raycast.rays.Polygon
import kotlin.math.abs

/*
 * Created by Exyte on 03.10.2023.
 */
@RequiresApi(Build.VERSION_CODES.Q)
internal class RenderNodePainter(
    private val width: Int,
    private val height: Int,
) : AbstractPainter() {
    private val renderNode = RenderNode("light painter")
    override var lightRadius: Float = DEFAULT_RAY_LENGTH
    private var currentBlurRadius = 0f

    init {
        renderNode.apply {
            setPosition(0, 0, this@RenderNodePainter.width, this@RenderNodePainter.height)
            setUseCompositingLayer(true, null)
        }

        lightPaint.blendMode = BlendMode.Src
        shadowPaint.blendMode = BlendMode.Src
    }

    override fun setBlurRadius(r: Float) {
        if (isBlurSupported) {
            renderNode.setRenderEffect(android.graphics.RenderEffect.createBlurEffect(r, r, Shader.TileMode.CLAMP))
        }
    }

    override fun onLightSourcePositionChanged(x: Float, y: Float, shadowPolygon: Polygon, lightPolygon: Polygon) {
        val centerHeight = height / 2f
        if (isBlurSupported) {
            val blurRadius = 8f * (abs(y - centerHeight) / centerHeight).coerceAtLeast(0.15f)
            if (abs(currentBlurRadius - blurRadius) > 0.15) {
                setBlurRadius(blurRadius)
                currentBlurRadius = blurRadius
            }
        }

        val r = lightRadius
        val center = Offset(x, y)

        lightPaint.shader = RadialGradientShader(
            center = center,
            radius = r,
            colors = lightGradientColors,
        )

        shadowPaint.shader = RadialGradientShader(
            center = center,
            radius = r,
            colors = shadowGradientColors,
        )

        fun drawBackgroundShadow(c: RecordingCanvas) {
            c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), outerShadowPaint.asFrameworkPaint())
        }

        fun drawShadow(c: RecordingCanvas) {
            c.drawPath(shadowPolygon.path, shadowPaint.asFrameworkPaint())
        }

        fun drawLight(c: RecordingCanvas) {
            c.drawPath(lightPolygon.path, lightPaint.asFrameworkPaint())
        }

        renderNode.apply {
            try {
                val canvas = beginRecording()
                drawBackgroundShadow(canvas)
                drawShadow(canvas)
                drawLight(canvas)
            } finally {
                endRecording()
            }
        }
    }

    override fun draw(origin: Offset, shadowPolygon: Polygon, lightPolygon: Polygon, canvas: Canvas) {
        canvas.nativeCanvas.drawRenderNode(renderNode)
    }

    private companion object {
        private val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}