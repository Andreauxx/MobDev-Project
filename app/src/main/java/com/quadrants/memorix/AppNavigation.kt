package com.quadrants.memorix

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quadrants.memorix.screens.LoginScreen
import com.quadrants.memorix.screens.SignUpScreen
import com.quadrants.memorix.screens.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("signup") { SignUpScreen(navController) }  // Pass navController
        composable("login") { LoginScreen(navController) }    // Add login screen
    }
}
