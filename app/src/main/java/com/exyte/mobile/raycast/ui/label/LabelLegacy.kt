package com.exyte.mobile.raycast.ui.label

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.exyte.mobile.raycast.ui.theme.labelColor
import com.exyte.mobile.raycast.ui.theme.labelColors

/*
 * Created by Exyte on 04.10.2023.
 */
@Composable
internal fun CenterLabelLegacy(
    text: String,
    areaWidth: Int,
    areaHeight: Int,
    textPathWidth: Int,
    textPathHeight: Int,
    lightConeRadius: () -> Float,
    lightSource: () -> Offset,
) {
    Text(
        text = text,
        fontSize = 48.sp,
        color = labelColor,
        letterSpacing = 4.25.sp,
        style = LocalTextStyle.current.copy(
            brush = createRadialGradient(
                areaWidth = areaWidth,
                areaHeight = areaHeight,
                textPathWidth = textPathWidth,
                textPathHeight = textPathHeight,
                lightSource = lightSource,
                lightConeRadius = lightConeRadius(),
            ),
            lineHeight = with(LocalDensity.current) { textPathHeight.toSp() },
            lineHeightStyle = LineHeightStyle(trim = LineHeightStyle.Trim.Both, alignment = LineHeightStyle.Alignment.Proportional)
        )
    )
}

private fun createRadialGradient(
    areaWidth: Int,
    areaHeight: Int,
    textPathWidth: Int,
    textPathHeight: Int,
    lightConeRadius: Float,
    lightSource: () -> Offset,
): Brush {
    val cw = areaWidth / 2f
    val ch = areaHeight / 2f
    val tw = textPathWidth * 1.05f / 2f
    val th = textPathHeight * 1.2f
    val (x, y) = lightSource()

    return Brush.radialGradient(
        colors = labelColors,
        center = Offset(
            x = tw + (x - cw),
            y = th + (y - ch),
        ),
        radius = lightConeRadius,
    )
}