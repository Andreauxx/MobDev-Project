package com.quadrants.memorix

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quadrants.memorix.screens.*

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("MemorixPrefs", Context.MODE_PRIVATE) }
    val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

    NavHost(navController = navController, startDestination = "splash") {

        // Splash Screen (Always the entry point)
        composable("splash") { SplashScreen(navController) }

        // Authentication Screens
        composable("signup") { SignUpScreen(navController) }
        composable("login") { LoginScreen(navController) }

        // Onboarding (Appears only after login/signup)
        composable("onboarding") {
            OnboardScreen(onFinish = {
                sharedPreferences.edit().putBoolean("hasSeenOnboarding", true).apply()
                navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
            })
        }

        // Main Screens
        composable("home") { HomeScreen(navController) }
        composable("folders") { LibraryScreen(navController) }
        composable("stats") { StatsScreen(navController) }
        //composable("profile") { ProfileScreen(navController) }
        // **NEW: Category Content Screen**
        composable("category_content/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "Unknown"

        }
    }
}
