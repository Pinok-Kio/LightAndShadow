package com.exyte.mobile.raycast.ui

import android.graphics.Path
import android.graphics.PathMeasure
import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.exyte.mobile.raycast.R
import com.exyte.mobile.raycast.ui.theme.fillMaxSize
import kotlinx.coroutines.isActive
import android.graphics.Path as PlatformPath

/*
 * Created by Exyte on 03.10.2023.
 */

@Composable
fun DebugScreen() {
    Box(
        modifier = fillMaxSize
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        BackgroundImage()

        RayCast(
            modifier = fillMaxSize,
        )
    }
}

@Composable
private fun BackgroundImage() {
    Image(
        modifier = fillMaxSize,
        painter = painterResource(id = R.drawable.img_metal_plate_01),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun RayCast(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 360.0) lcAngle: Float = 45f,
    @FloatRange(from = 0.0) lcRadius: Float = 1800f,
    showControls: Boolean = true,
    lcAnimateLight: Boolean = true,
) {
    if (LocalInspectionMode.current) {
        BoxWithConstraints(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            SimpleRayCast(
                modifier = fillMaxSize,
                lightSourcePosition = { Offset.Zero },
                lightConeAngle = { lcAngle },
                lightConeRadius = { lcRadius },
                showDebugRays = { false },
            )
        }
    } else {
        val lightSourcePosition = remember { mutableStateOf(Offset.Zero) }
        val lightConeAngle = remember { mutableFloatStateOf(lcAngle) }
        val lightConeRadius = remember { mutableFloatStateOf(lcRadius) }
        val animateLightSource = remember { mutableStateOf(lcAnimateLight) }
        val showDebugRays = remember { mutableStateOf(false) }

        BoxWithConstraints(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            val lightPath = remember {
                buildLightPath(
                    areaWidth = constraints.maxWidth,
                    areaHeight = constraints.maxHeight,
                )
            }
            LightMovementAnimation(
                isEnabled = animateLightSource.value,
                lightPath = lightPath,
            ) { currentPosition ->
                lightSourcePosition.value = currentPosition
            }

            SimpleRayCast(
                modifier = fillMaxSize
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                if (!animateLightSource.value) {
                                    lightSourcePosition.value = it
                                }
                            },
                        ) { change, _ ->
                            lightSourcePosition.value = change.position
                        }
                    },
                lightSourcePosition = { lightSourcePosition.value },
                lightConeAngle = { lightConeAngle.floatValue },
                lightConeRadius = { lightConeRadius.floatValue },
                showDebugRays = { showDebugRays.value },
            )

            if (showControls) {
                Controls(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 45.dp, vertical = 40.dp)
                        .align(Alignment.BottomCenter),
                    lightConeAngle = { lightConeAngle.floatValue },
                    onLightConeAngleChange = { lightConeAngle.floatValue = it },
                    lightRadius = { lightConeRadius.floatValue },
                    onLightRadiusChange = { lightConeRadius.floatValue = it },
                    animateLightSource = { animateLightSource.value },
                    onAnimateLightSourceChange = { animateLightSource.value = !animateLightSource.value },
                    showDebugRays = { showDebugRays.value },
                    onShowDebugRaysChange = { showDebugRays.value = !showDebugRays.value },
                )
            }
        }
    }
}


private fun buildLightPath(areaWidth: Int, areaHeight: Int): PlatformPath {
    val centerY = areaHeight / 2f

    return Path().apply {
        addRoundRect(
            -50f,
            centerY - 1000f,
            areaWidth + 50f,
            centerY + 1000f,
            800f,
            800f,
            Path.Direction.CW,
        )
    }
}

@Composable
private fun Controls(
    lightConeAngle: () -> Float,
    onLightConeAngleChange: (Float) -> Unit,
    lightRadius: () -> Float,
    onLightRadiusChange: (Float) -> Unit,
    animateLightSource: () -> Boolean,
    onAnimateLightSourceChange: (Boolean) -> Unit,
    showDebugRays: () -> Boolean,
    onShowDebugRaysChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier,
    ) {
        Text(
            text = "Light cone angle",
            color = Color.White,
        )
        ControlSlider(
            range = 15f..360f,
            value = lightConeAngle,
            onChange = onLightConeAngleChange,
        )

        Text(
            text = "Light radius",
            color = Color.White,
        )
        ControlSlider(
            range = 100f..1800f,
            value = lightRadius,
            onChange = onLightRadiusChange,
        )

        ControlCheckBox(
            isChecked = animateLightSource,
            onCheckedChange = onAnimateLightSourceChange,
            label = { if (animateLightSource()) "Stop Light" else "Animate Light" },
        )

        ControlCheckBox(
            isChecked = showDebugRays,
            onCheckedChange = onShowDebugRaysChange,
            label = { if (showDebugRays()) "Hide Debug Rays" else "Show Debug Rays" },
        )
    }
}

@Composable
private fun ControlSlider(
    range: ClosedFloatingPointRange<Float>,
    value: () -> Float,
    onChange: (Float) -> Unit,
) {
    Slider(
        modifier = Modifier.fillMaxWidth(),
        value = value(),
        valueRange = range,
        onValueChange = onChange,
    )
}

@Composable
private fun ControlCheckBox(
    isChecked: () -> Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: () -> String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isChecked(),
            onCheckedChange = onCheckedChange,
        )

        Text(
            text = label(),
            color = Color.White,
        )
    }
}

@Composable
private fun LightMovementAnimation(
    isEnabled: Boolean,
    lightPath: PlatformPath,
    onPositionChange: (Offset) -> Unit,
) {
    val lightAnimatable = remember { Animatable(0f) }

    LaunchedEffect(isEnabled) {
        if (isEnabled) {
            val pm = PathMeasure()
            pm.setPath(lightPath, true)

            val pos = floatArrayOf(0f, 0f)
            while (isActive) {
                val pathLength = pm.length
                lightAnimatable.snapTo(0f)
                lightAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 8000, easing = LinearEasing),
                ) {
                    pm.getPosTan(this.value * pathLength, pos, null)
                    val x = pos[0]
                    val y = pos[1]
                    onPositionChange(Offset(x, y))
                }

                if (!pm.nextContour()) {
                    pm.setPath(lightPath, true)
                }
            }
        }
    }
}