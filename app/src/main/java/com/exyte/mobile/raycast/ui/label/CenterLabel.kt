package com.exyte.mobile.raycast.ui.label

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.geometry.Offset

/*
 * Created by Exyte on 04.10.2023.
 */
@Composable
@NonRestartableComposable
fun CenterLabel(
    text: String,
    areaWidth: Int,
    areaHeight: Int,
    textPathWidth: Int,
    textPathHeight: Int,
    lightConeRadius: () -> Float,
    lightSource: () -> Offset,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        CenterLabelApi33(
            text = text,
            areaWidth = areaWidth,
            areaHeight = areaHeight,
            textPathWidth = textPathWidth,
            textPathHeight = textPathHeight,
            lightConeRadius = lightConeRadius,
            lightSource = lightSource,
        )
    } else {
        CenterLabelLegacy(
            text = text,
            areaWidth = areaWidth,
            areaHeight = areaHeight,
            textPathWidth = textPathWidth,
            textPathHeight = textPathHeight,
            lightConeRadius = lightConeRadius,
            lightSource = lightSource,
        )
    }
}