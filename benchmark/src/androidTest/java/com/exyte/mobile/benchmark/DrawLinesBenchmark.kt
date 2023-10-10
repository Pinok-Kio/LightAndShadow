package com.exyte.mobile.benchmark

import android.graphics.Canvas
import android.graphics.Paint
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class DrawLinesBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun drawLinesNative() {
        val canvas = Canvas()
        val rays = buildList {
            repeat(1000) {
                this += PolygonPoint(it * 2f, it * 2f)
            }
        }
        val origin = PolygonPoint(0f, 0f)
        val rayPaint = Paint()
        val lines = FloatArray(rays.size * 4)
        benchmarkRule.measureRepeated {
            rays.fastForEachIndexed { i, polygonPoint ->
                lines[i * 4] = origin.x
                lines[i * 4 + 1] = origin.y
                lines[i * 4 + 2] = polygonPoint.x
                lines[i * 4 + 3] = polygonPoint.y
            }
            canvas.drawLines(
                lines,
                rayPaint
            )
        }
    }

    @Test
    fun drawLinesForeach() {
        val canvas = Canvas()
        val rays = buildList {
            repeat(1000) {
                this += PolygonPoint(it * 2f, it * 2f)
            }
        }
        val origin = PolygonPoint(0f, 0f)
        val rayPaint = Paint()
        benchmarkRule.measureRepeated {
            rays.fastForEach {
                canvas.drawLine(
                    origin.x,
                    origin.y,
                    it.x,
                    it.y,
                    rayPaint,
                )
            }
        }
    }
}