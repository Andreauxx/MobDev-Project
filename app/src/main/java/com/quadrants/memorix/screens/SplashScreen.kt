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
import com.google.firebase.auth.FirebaseAuth
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.MediumViolet
import kotlinx.coroutines.delay
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("MemorixPrefs", Context.MODE_PRIVATE)
    }

    var isVisible by remember { mutableStateOf(false) }
    val systemUiController = rememberSystemUiController()

    // ✅ Set Status Bar & Navigation Bar Colors
    SideEffect {
        systemUiController.setStatusBarColor(MediumViolet, darkIcons = false)
        systemUiController.setNavigationBarColor(DarkViolet, darkIcons = false)
    }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(1500L) // Allow NavController to initialize

        val user = FirebaseAuth.getInstance().currentUser
        val savedUserId = sharedPreferences.getString("userId", null)
        val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

        println("🟢 hasSeenOnboarding: $hasSeenOnboarding")
        println("🟢 savedUserId: $savedUserId")
        println("🟢 Firebase User: ${user?.uid}")

        when {
            // ✅ Only navigate to Home if Firebase User is authenticated and savedUserId exists
            user != null && savedUserId != null -> {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            // ✅ If onboarding hasn't been seen, go to onboarding
            !hasSeenOnboarding -> {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            // ✅ If no user exists, force signup
            else -> {
                navController.navigate("signup") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // ✅ UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MediumViolet, DarkViolet)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(1500))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.owl_icon),
                    contentDescription = "Owl Icon",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}
