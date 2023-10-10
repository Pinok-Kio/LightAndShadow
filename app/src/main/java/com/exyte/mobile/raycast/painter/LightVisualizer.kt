package com.exyte.mobile.raycast.painter

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.util.fastForEach
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.exyte.mobile.raycast.TextToPolygons
import com.exyte.mobile.raycast.rays.LineSegment
import com.exyte.mobile.raycast.rays.Polygon
import com.exyte.mobile.raycast.rays.PolygonPoint
import com.exyte.mobile.raycast.rays.RayCaster
import com.exyte.mobile.raycast.ui.theme.starColor
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.Path as PlatformPath


/*
 * Created by Exyte on 30.09.2023.
 */

@Composable
fun rememberSightVisualizer(
    width: Int,
    height: Int,
    labelText: String,
    @FloatRange(from = 0.0, to = 360.0) coneAngle: Float,
    @IntRange(from = 0L) raysCount: Int = 1000,
    @FloatRange(from = 0.0) lightRadius: Float = 1800f,
    showRays: Boolean = false,
) = remember {
    LightVisualizer(width = width, height = height, labelText = labelText)
}.apply {
    lightConeAngle = coneAngle.toDouble()
    this.raysCount = raysCount
    this.lightRadius = lightRadius
    this.showRays = showRays
}

class LightVisualizer(
    width: Int,
    height: Int,
    labelText: String,
    textSize: Float = 48f,
) {
    private val activePolygons: MutableList<Polygon> = mutableListOf()
    private val activeSegments = mutableListOf<LineSegment>()
    private val activePoints = mutableListOf<PolygonPoint>()
    private val lightPolygon = Polygon()
    private val shadowPolygon = Polygon()
    private val starPath = mutableListOf<PlatformPath>()
    private val textPathSizeInternal: Size
    private val starPaint = Paint()
    private val painter: Painter = getPainter(width, height)
    private val scope = MainScope()
    private val channel = Channel<Offset>(onBufferOverflow = BufferOverflow.DROP_LATEST)

    private val textPath: PlatformPath
    private val rayCaster: RayCaster

    private var currentLightSourceX = 0f
    private var currentLightSourceY = 0f

    val textPathSize: Size get() = textPathSizeInternal

    var lightConeAngle = 45.0
    var raysCount: Int = 250
    var lightRadius: Float = 1800f
        set(value) {
            field = value
            painter.lightRadius = lightRadius
        }
    var showRays: Boolean = false
    private val debugRaysPainter = RaysPainter(raysCount)

    init {
        val (tp, polys) = TextToPolygons(width, height).convert(labelText, textSizeSp = textSize)
        textPath = tp
        activePolygons += polys

        val b = RectF()
        tp.computeBounds(b, true)
        textPathSizeInternal = Size(b.width(), b.height())

        createStars(width, height)

        initInternalData()

        rayCaster = RayCaster(width, height, activeSegments)

        starPaint.color = starColor
    }

    private fun createStars(width: Int, height: Int) {
        val sp = RoundedPolygon.star(
            numVerticesPerRadius = 5,
            radius = 30f,
            innerRadius = 15f,
            center = PointF(width / 2f, height / 2f - 300f),
        ).toPath()
        starPath += sp
        activePolygons += TextToPolygons.pathToPolys(sp)

        val sp1 = RoundedPolygon.star(
            numVerticesPerRadius = 6,
            radius = 35f,
            innerRadius = 15f,
            center = PointF(width / 2f - 212f, height / 2f - 150f),
        ).toPath()
        starPath += sp1
        activePolygons += TextToPolygons.pathToPolys(sp1)

        val sp2 = RoundedPolygon.star(
            numVerticesPerRadius = 7,
            radius = 35f,
            innerRadius = 15f,
            center = PointF(width / 2f + 286f, height / 2f + 220f),
        ).toPath()
        starPath += sp2
        activePolygons += TextToPolygons.pathToPolys(sp2)
    }

    private fun initInternalData() {
        for (p in activePolygons) {
            p.points.forEachIndexed { index, point ->
                activePoints.add(point)

                val end = if (index == p.points.lastIndex) {
                    point
                } else {
                    p.points[index + 1]
                }
                activeSegments.add(LineSegment(point.x, point.y, end.x, end.y))
            }
        }
    }

    fun start() {
        runLoop()
    }

    fun stop() {
        scope.coroutineContext.cancelChildren()
    }

    private fun runLoop() {
        scope.launch(Dispatchers.Default) {

            fun mergeRays(lightSourcePosition: Offset, rays: List<RayCaster.CastResult>) {
                val lightSource = PolygonPoint(lightSourcePosition.x, lightSourcePosition.y)
                lightPolygon.reset()
                lightPolygon.addPoint(lightSource)

                shadowPolygon.reset()
                shadowPolygon.addPoint(lightSource)
                shadowPolygon.addPoint(rays.first().shadowPointA)
                shadowPolygon.addPoint(rays.last().shadowPointB)

                rays.fastForEach { lightPolygon.addPoints(it.rays) }

                lightPolygon.finish()
                shadowPolygon.finish()
            }

            val jobs = mutableListOf<Deferred<RayCaster.CastResult>>()

            for (position in channel) {
                if (raysCount <= RAYS_DIVISION_THRESHOLD) {
                    val rays = rayCaster.castRays(
                        originPointX = position.x,
                        originPointY = position.y,
                        raysCount = raysCount,
                        rayMaxLength = lightRadius.toDouble(),
                        angle = lightConeAngle,
                    )
                    mergeRays(lightSourcePosition = position, rays = listOf(rays))
                } else {
                    jobs.clear()
                    val count = raysCount / RAYS_DIVISION_THRESHOLD + if (raysCount % RAYS_DIVISION_THRESHOLD != 0) 1 else 0
                    val step = raysCount / count
                    repeat(count) {
                        jobs += async {
                            rayCaster.castRays(
                                originPointX = position.x,
                                originPointY = position.y,
                                raysCount = raysCount,
                                rayMaxLength = lightRadius.toDouble(),
                                angle = lightConeAngle,
                                raysLimitStart = it * step,
                                raysLimitEnd = if (it == count - 1) raysCount else it * step + step,
                            )
                        }
                    }
                    val rays = jobs.awaitAll()
                    mergeRays(lightSourcePosition = position, rays = rays)
                }

                withContext(Dispatchers.Main) {
                    painter.onLightSourcePositionChanged(position.x, position.y, shadowPolygon, lightPolygon)
                }
            }
        }
    }


    fun onLightSourcePositionChanged(x: Float, y: Float) {
        if (currentLightSourceX != x || currentLightSourceY != y) {
            currentLightSourceX = x
            currentLightSourceY = y

            scope.launch {
                channel.send(Offset(x, y))
            }
        }
    }

    fun draw(lightSourcePosition: Offset, canvas: Canvas) {
        if (showRays) {
            val (rays) = rayCaster.castRays(
                originPointX = lightSourcePosition.x,
                originPointY = lightSourcePosition.y,
                raysCount = 50,
                rayMaxLength = lightRadius.toDouble(),
                angle = lightConeAngle,
            )
            debugRaysPainter.draw(rays, lightSourcePosition, canvas)
        } else {
            painter.draw(lightSourcePosition, shadowPolygon, lightPolygon, canvas)
        }

        starPath.fastForEach {
            canvas.drawPath(it.asComposePath(), starPaint)
        }
    }

    private companion object {
        private const val RAYS_DIVISION_THRESHOLD = 500
    }
}