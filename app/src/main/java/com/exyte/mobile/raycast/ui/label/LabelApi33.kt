package com.exyte.mobile.raycast.ui.label

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.unit.sp
import com.exyte.mobile.raycast.ui.theme.labelColor
import com.exyte.mobile.raycast.ui.theme.shadowedLabelColor
import org.intellij.lang.annotations.Language

/*
 * Created by Exyte on 04.10.2023.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
internal fun CenterLabelApi33(
    text: String,
    areaWidth: Int,
    areaHeight: Int,
    textPathWidth: Int,
    textPathHeight: Int,
    lightConeRadius: () -> Float,
    lightSource: () -> Offset,
) {
    val shader = remember {
        PathColorShader().apply {
            setColors(labelColor, shadowedLabelColor)
            setAreaSize(areaWidth, areaHeight)
            setLabelSize(textPathWidth, textPathHeight)
        }
    }.apply {
        setLightConeRadius(lightConeRadius())
    }

    Text(
        text = text,
        fontSize = 48.sp,
        letterSpacing = 4.25.sp,
        style = LocalTextStyle.current.copy(
            brush = shader.setLightSource(lightSource().x, lightSource().y).toBrush()
        ),
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class PathColorShader {
    private val rs = RuntimeShader(LABEL_SHADER)

    fun setLightConeRadius(radius: Float) {
        rs.setFloatUniform("lightConeRadius", radius)
    }

    fun setColors(lightColor: Color, darkColor: Color) {
        rs.setFloatUniform("lightColor", lightColor.red, lightColor.green, lightColor.blue, lightColor.alpha)
        rs.setFloatUniform("darkColor", darkColor.red, darkColor.green, darkColor.blue, darkColor.alpha)
    }

    fun setAreaSize(w: Int, h: Int) {
        rs.setFloatUniform("areaSize", w.toFloat(), h.toFloat())
    }

    fun setLabelSize(w: Int, h: Int) {
        rs.setFloatUniform("labelSize", w.toFloat(), h.toFloat())
    }

    fun setLightSource(x: Float, y: Float): PathColorShader = apply {
        rs.setFloatUniform("lightSource", x, y)
    }

    fun toBrush() = ShaderBrush(rs)

    companion object {
        @Language("AGSL")
        private const val LABEL_SHADER = """
            uniform half2 lightSource;
            uniform half4 lightColor;
            uniform half4 darkColor;
            uniform half2 areaSize;
            uniform half2 labelSize;
            uniform float lightConeRadius;
            
            const half maxMixValue = 0.85;
            const half4 colorWhite = half4(1.0);
    
            half4 main(vec2 coords) {
                half2 ls = half2(
                    lightSource.x - (areaSize.x - labelSize.x) / 2.0, 
                    lightSource.y - (areaSize.y - labelSize.y) / 2.0
                );
                
                half distanceToLightSource = distance(ls, coords);
                
            // Find additional white color tint multiplier
                half katet = abs(ls.x - coords.x);
                half diag = distanceToLightSource;
                half sinus = sin(katet / diag);  
                half tintMultiplier = 1.0 - sinus;
                
            // Find additional tint position    
                half realLabelHeight = labelSize.y * 2.53;
                half labelVerticalPadding = (realLabelHeight - labelSize.y) / 2.0;

                half s2 = step(areaSize.y / 2.0, lightSource.y);
                half s1 = 1.0 - s2;
                
                half mm1 = (1.0 - step(labelVerticalPadding + 30.0, coords.y)) * (1.0 - smoothstep(labelVerticalPadding - 30.0, labelVerticalPadding + 30.0, coords.y)) * s1; 
                half mm2 = step(realLabelHeight - labelVerticalPadding - 30.0, coords.y) * smoothstep(realLabelHeight - labelVerticalPadding - 30.0, realLabelHeight - labelVerticalPadding + 30.0, coords.y) * s2;
                half m2 = clamp(mm1 + mm2, 0.0, 1.0);

            // Combining all together
                half m = step(lightConeRadius, distanceToLightSource);
                half mixValue = clamp(distanceToLightSource / lightConeRadius, 0.0, maxMixValue) * (1.0 - m) + (maxMixValue * m);
                
                return mix(lightColor, darkColor, mixValue) + colorWhite * tintMultiplier * m2;    
            }
"""
    }
}