package com.exyte.mobile.raycast.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exyte.mobile.raycast.R
import com.exyte.mobile.raycast.ui.theme.RayCastTheme
import com.exyte.mobile.raycast.ui.theme.exyteGreen
import com.exyte.mobile.raycast.ui.theme.exyteLight
import com.exyte.mobile.raycast.ui.theme.fillMaxSize

/*
 * Created by Exyte on 03.10.2023.
 */
@Composable
fun AboutCard01() {
    RaycastCard(
        bgColor = Color(0xFF0C0C0C),
        imageResId = R.drawable.img_moon,
    )
}

@Composable
fun AboutCard02() {
    RaycastCard(
        bgColor = Color(0xFF0C0C0C),
        imageResId = R.drawable.img_metal_plate_01,
    )
}

@Composable
fun AboutCard03() {
    RaycastCard(
        bgColor = Color(0xFF0C0C0C),
        imageResId = R.drawable.img_metal_plate_02,
    )
}

@Composable
fun AboutCard04() {
    RaycastCard(
        bgColor = Color(0xFF0C0C0C),
        imageResId = R.drawable.img_metal_plate_03,
    )
}

@Composable
private fun RaycastCard(
    bgColor: Color,
    @DrawableRes imageResId: Int,
) {
    Column(
        modifier = fillMaxSize
            .verticalScroll(state = rememberScrollState())
            .background(bgColor)
            .padding(top = 74.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
    ) {
        Card(imageResId)

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "// Contact us",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = exyteLight,
        )

        Text(
            text = "WE ARE A DISTRIBUTED TEAM, BUT IF YOU WANT TO SEND US A POSTCARD, YOU CAN USE ONE OF THESE ADDRESSES:",
            fontSize = 20.sp,
            color = exyteLight,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Address(
            line1 = "UNITED ARAB EMIRATES",
            line2 = "Building A1",
            line3 = "Dubai Silicon Oasis DDP",
            coords = "X:=25.1188268; Y:=55.3691325",
        )

        Spacer(modifier = Modifier.height(16.dp))

        Address(
            line1 = "UNITED STATES",
            line2 = "408 Broadway New York",
            line3 = "NY 10013",
            coords = "X:=40.7188811; Y:=-74.0018562",
        )

        Spacer(modifier = Modifier.height(16.dp))

        Email(email = "hello@exyte.com")
    }
}

@Composable
private fun Card(@DrawableRes imageResId: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(modifier = fillMaxSize) {
            Image(
                modifier = fillMaxSize,
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            RayCast(
                modifier = fillMaxSize,
                showControls = false,
                lcAngle = 360f,
            )
        }
    }
}

@Composable
private fun ColumnScope.Address(
    line1: String,
    line2: String,
    line3: String,
    coords: String,
) {
    Text(
        text = line1,
        color = exyteGreen,
    )

    Text(
        text = line2,
        fontSize = 12.sp,
        color = exyteLight,
    )

    Text(
        text = line3,
        fontSize = 12.sp,
        color = exyteLight,
    )

    Text(
        modifier = Modifier.padding(top = 8.dp),
        text = "{$coords}",
        fontSize = 12.sp,
        color = exyteLight.copy(alpha = 0.5f),
    )
}

@Composable
private fun ColumnScope.Email(
    email: String,
) {
    Text(
        text = "EMAIL",
        color = exyteGreen,
    )

    Text(
        text = email,
        fontSize = 12.sp,
        color = exyteLight,
    )
}

@Preview
@Composable
private fun PreviewAboutScreen() {
    RayCastTheme {
        RaycastCard(
            bgColor = Color(0xFF2E2E25),
            imageResId = R.drawable.img_moon,
        )
    }
}