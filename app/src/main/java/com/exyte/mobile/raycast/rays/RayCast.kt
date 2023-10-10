package com.exyte.mobile.raycast.rays

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin


/*
 * Created by Exyte on 28.09.2023.
 */
internal class RayCaster(
    private val width: Int,
    private val height: Int,
    private val activeSegments: List<LineSegment>,
) {
    private val centerX = width / 2f
    private val centerY = height / 2f

    fun castRays(
        originPointX: Float,
        originPointY: Float,
        @IntRange(from = 0L) raysCount: Int,
        @FloatRange(from = 0.0) rayMaxLength: Double,
        @FloatRange(from = 0.0, to = 360.0) angle: Double,
        raysLimitStart: Int = 0,
        raysLimitEnd: Int = raysCount,
    ): CastResult {
        val a = centerX - originPointX
        val d = distance(centerX, centerY, originPointX, originPointY)
        val additionalRotation = Math.toDegrees(acos(a / d))
        val currentAngle0 = 90.0 - additionalRotation
        val currentAngle1 = (if (originPointY < centerY) -90.0 else 90.0) - additionalRotation
        val result = ArrayList<PolygonPoint>()
        val angleStep = Math.toRadians(angle / raysCount)

        val m1 = Math.toRadians(-90.0 - angle / 2.0 + currentAngle0)
        val m2 = Math.toRadians(-90.0 - angle / 2.0 + currentAngle1)

        for (i in raysLimitStart..raysLimitEnd + 1) {
            val targetX = (originPointX + cos(angleStep * i + m1) * rayMaxLength).toFloat()
            val targetY = (originPointY + sin(angleStep * i + m2) * rayMaxLength).toFloat()

            val intersection = getClosestIntersection(
                rayStartX = originPointX,
                rayStartY = originPointY,
                rayEndX = targetX,
                rayEndY = targetY,
                segments = activeSegments,
            )
            if (intersection != null) {
                result.add(intersection)
            } else {
                result.add(PolygonPoint(targetX, targetY))
            }
        }

        val shadowLength = rayMaxLength * 1.2f
        val p1 = PolygonPoint(
            (originPointX + cos(angleStep * raysLimitStart + m1) * shadowLength).toFloat(),
            (originPointY + sin(angleStep * raysLimitStart + m2) * shadowLength).toFloat(),
        )
        val p2 = PolygonPoint(
            (originPointX + cos(angleStep * raysLimitEnd + m1) * shadowLength).toFloat(),
            (originPointY + sin(angleStep * raysLimitEnd + m2) * shadowLength).toFloat(),
        )

        return CastResult(result, p1, p2)
    }

    data class CastResult(val rays: List<PolygonPoint>, val shadowPointA: PolygonPoint, val shadowPointB: PolygonPoint)

    private companion object {
        private const val EPSILON = 0.000001

        private fun distance(startX: Float, startY: Float, endX: Float, endY: Float): Double =
            hypot((endX - startX).toDouble(), (endY - startY).toDouble())

        private fun crossProduct(ax: Float, ay: Float, bx: Float, by: Float): Double = (ax * by - bx * ay).toDouble()

        // Get the intersection of RAY & SEGMENT, returns null if no intersection found
        private fun getIntersection(
            rayStartX: Float,
            rayStartY: Float,
            rayEndX: Float,
            rayEndY: Float,
            segmentStartX: Float,
            segmentStartY: Float,
            segmentEndX: Float,
            segmentEndY: Float,
        ): PolygonPoint? {
            val rx = rayEndX - rayStartX
            val ry = rayEndY - rayStartY
            val sx = segmentEndX - segmentStartX
            val sy = segmentEndY - segmentStartY

            val rxs = crossProduct(rx, ry, sx, sy)
            val qpx = segmentStartX - rayStartX
            val qpy = segmentStartY - rayStartY

            val qpxr = crossProduct(qpx, qpy, rx, ry)

            // r * s = 0 and (q - p) * r = 0 ==> the two lines are collinear
            val isCollinear = rxs < EPSILON && qpxr < EPSILON
            if (isCollinear) return null

            // r * s = 0 and (q - p) * r != 0, ==> two lines are parallel
            val isParallel = rxs < EPSILON && qpxr >= EPSILON
            if (isParallel) return null

            // t = (q - p) * s / (r * s)
            val t = crossProduct(qpx, qpy, sx, sy) / rxs

            // u = (q - p) * r / (r * s)
            val u = crossProduct(qpx, qpy, rx, ry) / rxs

            // r x s != 0 and 0 <= t <= 1 and 0 <= u <= 1 ==> the two line segments meet at the point p + t * r
            val hasIntersection = rxs >= EPSILON && 0 <= t && t <= 1 && 0 <= u && u <= 1
            return if (hasIntersection) {
                // Intersection FOUND, return intersection point
                PolygonPoint(
                    x = floor(rayStartX + t * rx).toFloat(),
                    y = floor(rayStartY + t * ry).toFloat(),
                )
            } else null // Intersection NOT found, return null
        }
    }

    private fun getClosestIntersection(
        rayStartX: Float,
        rayStartY: Float,
        rayEndX: Float,
        rayEndY: Float,
        segments: List<LineSegment>,
        useDoubleCheck: Boolean = false,
    ): PolygonPoint? {
        var closestIntersection: PolygonPoint? = null
        var closestDistance = Double.MAX_VALUE

        for (line in segments) {
            if (useDoubleCheck) {
                val intersectRL = getIntersection(
                    rayStartX = rayStartX,
                    rayStartY = rayStartY,
                    rayEndX = rayEndX,
                    rayEndY = rayEndY,
                    segmentStartX = line.startX,
                    segmentStartY = line.startY,
                    segmentEndX = line.endX,
                    segmentEndY = line.endY,
                )
                if (intersectRL != null) {
                    val distance = distance(
                        startX = rayStartX,
                        startY = rayStartY,
                        endX = intersectRL.x,
                        endY = intersectRL.y,
                    )
                    if (closestIntersection == null || distance < closestDistance) {
                        closestIntersection = intersectRL
                        closestDistance = distance
                    }
                }
            }

            val intersectLR = getIntersection(
                rayStartX = line.startX,
                rayStartY = line.startY,
                rayEndX = line.endX,
                rayEndY = line.endY,
                segmentStartX = rayStartX,
                segmentStartY = rayStartY,
                segmentEndX = rayEndX,
                segmentEndY = rayEndY,
            )
            if (intersectLR != null) {
                val distance = distance(
                    startX = rayStartX,
                    startY = rayStartY,
                    endX = intersectLR.x,
                    endY = intersectLR.y,
                )
                if (closestIntersection == null || distance < closestDistance) {
                    closestIntersection = intersectLR
                    closestDistance = distance
                }
            }
        }

        return closestIntersection
    }
}

