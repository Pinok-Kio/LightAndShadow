package com.exyte.mobile.raycast.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.sp
import com.exyte.mobile.raycast.R

private val chakraFontFamily = FontFamily(
    fonts = listOf(
        Font(
            resId = R.font.chakra_petch_regular,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
        ),
    )
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = chakraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        textMotion = TextMotion.Animated,
    )
)