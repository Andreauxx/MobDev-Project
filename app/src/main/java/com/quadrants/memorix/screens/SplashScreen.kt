package com.quadrants.memorix.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.quadrants.memorix.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6C3393), // Purple
                        Color(0xFF210F2D) // Darker purple
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.owl_icon),
            contentDescription = "Owl Icon",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
        )
    }

    // Navigate to Sign-Up Screen after a delay
    LaunchedEffect(Unit) {
        delay(3000L) // 3 seconds
        navController.navigate("signup") {
            popUpTo("splash") { inclusive = true } // Remove SplashScreen from backstack
        }
    }
}
