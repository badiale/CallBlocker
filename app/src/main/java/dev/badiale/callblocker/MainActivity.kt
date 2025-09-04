package dev.badiale.callblocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.badiale.callblocker.presentation.screens.MainScreen
import dev.badiale.callblocker.ui.theme.CallBlockerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CallBlockerTheme {
                MainScreen()
            }
        }
    }
}