package com.dselivetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dselivetracker.ui.navigation.DseNavHost
import com.dselivetracker.ui.theme.DSETrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DSETrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DseNavHost()
                }
            }
        }
    }
}
