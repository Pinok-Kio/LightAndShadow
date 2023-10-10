package com.exyte.mobile.raycast.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import com.exyte.mobile.raycast.painter.rememberSightVisualizer
import com.exyte.mobile.raycast.ui.label.CenterLabel
import com.exyte.mobile.raycast.ui.label.CenterLabelLegacy
import com.exyte.mobile.raycast.ui.theme.fillMaxSize

/*
 * Created by Exyte on 09.10.2023.
 */
@Composable
fun SimpleRayCast(
    lightSourcePosition: () -> Offset,
    lightConeAngle: () -> Float,
    lightConeRadius: () -> Float,
    showDebugRays: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (LocalInspectionMode.current) {
            CenterLabelLegacy(
                text = "EXYTE",
                areaWidth = constraints.maxWidth,
                areaHeight = constraints.maxHeight,
                textPathWidth = 600,
                textPathHeight = 300,
                lightConeRadius = lightConeRadius,
                lightSource = lightSourcePosition,
            )
        } else {
            val visualizer = rememberSightVisualizer(
                width = constraints.maxWidth,
                height = constraints.maxHeight,
                labelText = "EXYTE",
                coneAngle = lightConeAngle(),
                lightRadius = lightConeRadius(),
                showRays = showDebugRays(),
            ).apply {
                val (x, y) = lightSourcePosition()
                onLightSourcePositionChanged(x, y)
            }

            DisposableEffect(Unit) {
                visualizer.start()
                onDispose(visualizer::stop)
            }

            Canvas(
                modifier = fillMaxSize
                    .graphicsLayer {
                        this.compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                val p = lightSourcePosition()
                drawIntoCanvas {
                    visualizer.draw(lightSourcePosition = p, canvas = it)
                }
            }

            CenterLabel(
                text = "EXYTE",
                areaWidth = constraints.maxWidth,
                areaHeight = constraints.maxHeight,
                textPathWidth = visualizer.textPathSize.width.toInt(),
                textPathHeight = visualizer.textPathSize.height.toInt(),
                lightConeRadius = lightConeRadius,
                lightSource = lightSourcePosition,
            )
        }
    }
}