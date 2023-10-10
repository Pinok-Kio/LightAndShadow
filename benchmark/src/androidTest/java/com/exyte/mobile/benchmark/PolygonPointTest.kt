package com.exyte.mobile.benchmark

import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.cos
import kotlin.math.sin

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class PolygonPointTest {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun packFloatTest() {
        benchmarkRule.measureRepeated {
            repeat(100) {
                val p = PolygonPoint(100f, 200f)
            }
        }
    }

    @Test
    fun dataClassTest() {
        benchmarkRule.measureRepeated {
            repeat(100) {
                val p = PPoint(100f, 200f)
            }
        }
    }

    @Test
    fun packFloatLineSegmentTest() {
        benchmarkRule.measureRepeated {
            repeat(100) {
                val p = PackedLineSegment(PolygonPoint(100f, 200f), PolygonPoint(300f, 400f))
            }
        }
    }

    @Test
    fun packFloatLineSegment1Test() {
        benchmarkRule.measureRepeated {
            repeat(100) {
                val p = PackedLineSegment1(100f, 200f, 300f, 400f)
            }
        }
    }

    @Test
    fun dataClassLineSegmentTest() {
        benchmarkRule.measureRepeated {
            repeat(100) {
                val p = LineSegment(100f, 200f, 300f, 400f)
            }
        }
    }

    @Test
    fun createPointTest() {
        benchmarkRule.measureRepeated {
            repeat(100) {
                PolygonPoint(
                    (100f + cos(50.0 * 10 + 90) * 1800f).toFloat(),
                    (200f + sin(50.0 * 10 + 90) * 1800f).toFloat(),
                )
            }
        }
    }
}

fun PolygonPoint(x: Float, y: Float) = PolygonPoint(packFloats(x, y))

@JvmInline
value class PolygonPoint(private val packedValue: Long) {
    val x: Float get() = unpackFloat1(packedValue)
    val y: Float get() = unpackFloat2(packedValue)
}

data class PPoint(val x: Float, val y: Float)

data class LineSegment(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
)

data class PackedLineSegment(
    val start: PolygonPoint,
    val end: PolygonPoint,
)


data class PackedLineSegment1(val packedValue1: Long, val packedValue2: Long)

fun PackedLineSegment1(x1: Float, y1: Float, x2: Float, y2: Float) = PackedLineSegment1(packFloats(x1, y1), packFloats(x2, y2))