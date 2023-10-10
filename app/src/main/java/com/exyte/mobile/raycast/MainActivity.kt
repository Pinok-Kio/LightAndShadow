package com.exyte.mobile.raycast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.exyte.mobile.raycast.ui.AboutCard01
import com.exyte.mobile.raycast.ui.AboutCard02
import com.exyte.mobile.raycast.ui.AboutCard03
import com.exyte.mobile.raycast.ui.AboutCard04
import com.exyte.mobile.raycast.ui.DebugScreen
import com.exyte.mobile.raycast.ui.theme.RayCastTheme
import com.exyte.mobile.raycast.ui.theme.fillMaxSize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            RayCastTheme {
                Surface(modifier = fillMaxSize, color = MaterialTheme.colorScheme.background) {
                    val currentScreen = remember { mutableIntStateOf(0) }
                    Box(
                        modifier = fillMaxSize,
                    ) {
                        when (currentScreen.intValue) {
                            0 -> ScreenSelection { currentScreen.intValue = it }
                            1 -> DebugScreen()
                            2 -> AboutCard01()
                            3 -> AboutCard02()
                            4 -> AboutCard03()
                            5 -> AboutCard04()
                        }
                    }

                    BackHandler(currentScreen.intValue != 0) {
                        currentScreen.intValue = 0
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenSelection(
    onScreenSelected: (Int) -> Unit,
) {
    Column(
        modifier = fillMaxSize,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Button(onClick = { onScreenSelected(1) }) {
            Text(text = "Full screen")
        }

        Button(onClick = { onScreenSelected(2) }) {
            Text(text = "Moon")
        }

        Button(onClick = { onScreenSelected(3) }) {
            Text(text = "Metal")
        }

        Button(onClick = { onScreenSelected(4) }) {
            Text(text = "Rusty 1")
        }

        Button(onClick = { onScreenSelected(5) }) {
            Text(text = "Rusty 2")
        }
    }
}

