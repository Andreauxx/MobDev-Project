package com.quadrants.memorix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.quadrants.memorix.ui.theme.MemorixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemorixTheme {
                AppNavigation() // Start the app with navigation
            }
        }
    }
}
