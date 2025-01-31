package com.quadrants.memorix.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.MediumViolet
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // Animation state for fade-in effect
    var isVisible by remember { mutableStateOf(false) }
    val systemUiController = rememberSystemUiController()

    // **Set Status Bar & Navigation Bar Colors**
    SideEffect {
        systemUiController.setStatusBarColor(MediumViolet, darkIcons = false)
        systemUiController.setNavigationBarColor(DarkViolet, darkIcons = false) // ✅ Match the background
    }
    // Trigger fade-in animation
    LaunchedEffect(Unit) {
        isVisible = true
        delay(2500L) // Delay before navigating
        navController.navigate("signup") {
            popUpTo("splash") { inclusive = true } // Remove SplashScreen from backstack
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MediumViolet, // Gradient start color
                        DarkViolet // Gradient end color
                    )
                )
            ),
        contentAlignment = Alignment.Center // Center everything inside the Box
    ) {
        // Centered Animated Owl Icon
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1500)) // Smooth fade-in
        ) {
            Box(contentAlignment = Alignment.Center) { // Ensures the logo is centered
                Image(
                    painter = painterResource(id = R.drawable.owl_icon),
                    contentDescription = "Owl Icon",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}
