package com.quadrants.memorix

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quadrants.memorix.screens.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.util.Base64
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.quadrants.memorix.screens.SelectContentScreen
import androidx.compose.ui.unit.dp


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(activity: MainActivity) {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val sharedPreferences =
        remember { context.getSharedPreferences("MemorixPrefs", Context.MODE_PRIVATE) }
    val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.padding(0.dp), // ✅ Removes all padding from Scaffold
        bottomBar = {
            val currentScreen = navController.currentDestination?.route ?: ""
            if (currentScreen.contains("home") || currentScreen.contains("folders") ||
                currentScreen.contains("stats") || currentScreen.contains("profile")
            ) {
                BottomNavBar(
                    navController = navController,
                    currentScreen = currentScreen,
                    onPlusClick = { showBottomSheet = true }
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp) // ✅ Ensures no window insets
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.fillMaxSize() // ✅ No additional padding
            ) {
                // Splash Screen
                composable("splash") { SplashScreen(navController) }

                // Authentication Screens
                composable("signup") { SignUpScreen(navController, sharedPreferences) }
                composable("login") { LoginScreen(navController, sharedPreferences) }

                // Onboarding
                composable("onboarding") {
                    OnboardScreen(onFinish = {
                        sharedPreferences.edit().putBoolean("hasSeenOnboarding", true).apply()

                        val userId = sharedPreferences.getString("userId", null)
                        if (userId == null) {
                            // ✅ Go to Signup if no user is logged in
                            navController.navigate("signup") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        } else {
                            // ✅ Go to Home if user is logged in
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    })
                }

                // Main Screens
                composable("home") { HomeScreen(navController, activity, onPlusClick = { showBottomSheet = true }) }
                composable("folders") {
                    val userId = sharedPreferences.getString("userId", "") ?: ""
                    LibraryScreen(navController, userId, activity, onPlusClick = { showBottomSheet = true })
                }
                composable("stats") {
                    StatsScreen(navController, activity, onPlusClick = { showBottomSheet = true })
                }
                composable("profile") {
                    ProfileScreen(navController, activity, onPlusClick = { showBottomSheet = true })
                }
                composable("select_content") { SelectContentScreen(navController) }


                composable("edit_profile") {
                    EditProfileScreen(navController)
                }

                composable("home?quizJson={quizJson}") { backStackEntry ->
                    val quizJson = backStackEntry.arguments?.getString("quizJson")
                    val decodedQuizJson = quizJson?.let {
                        val decodedString = Uri.decode(it) // Decode safely
                        String(Base64.decode(decodedString, Base64.DEFAULT))
                    }

                    val listType = object : TypeToken<List<QuizQuestion>>() {}.type
                    val quizQuestions: List<QuizQuestion> = Gson().fromJson(decodedQuizJson, listType) ?: emptyList()

                    println("✅ Decoded quiz JSON: $decodedQuizJson")
                    println("✅ Parsed quizQuestions: ${quizQuestions.size}")

                    HomeScreen(navController, activity, quizQuestions, onPlusClick = { navController.navigate("createQuiz") })
                }





                // Flashcard and Quiz Creation
                composable("flashcard") { CreateSetScreen(navController) }
                composable("quiz") { CreateQuizScreen(navController) }

                // Folder Detail Screen
                composable("folderDetail/flashcard/{folderName}/{itemsJson}") { backStackEntry ->
                    val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
                    val itemsJson = backStackEntry.arguments?.getString("itemsJson") ?: ""

                    if (folderName.isNotBlank() && itemsJson.isNotBlank()) {
                        val decodedFolderName = String(Base64.decode(folderName, Base64.DEFAULT))
                        val decodedItemsJson = String(Base64.decode(itemsJson, Base64.DEFAULT))

                        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val items: List<Map<String, Any>> = try {
                            Gson().fromJson(decodedItemsJson, listType)
                        } catch (e: Exception) {
                            println("❌ JSON Parsing Error: ${e.message}")
                            emptyList()
                        }

                        if (items.isNotEmpty()) {
                            FolderDetailScreen(
                                decodedFolderName,
                                items,
                                navController,
                                isCreator = true,
                                activity = activity
                            )
                        }
                    }
                }




                composable("folderDetail/quiz/{folderName}/{itemsJson}") { backStackEntry ->
                    val folderName = String(
                        Base64.decode(
                            backStackEntry.arguments?.getString("folderName") ?: "", Base64.DEFAULT
                        )
                    )
                    val itemsJson = String(
                        Base64.decode(
                            backStackEntry.arguments?.getString("itemsJson") ?: "",
                            Base64.DEFAULT
                        )
                    )

                    val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val items: List<Map<String, Any>> = try {
                        Gson().fromJson(itemsJson, listType)
                    } catch (e: Exception) {
                        emptyList()
                    }

                    if (items.isNotEmpty()) {
                        FolderDetailScreen(folderName, items, navController, isCreator = true,  activity = activity)
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
                BottomSheetContent(navController, activity, onDismiss = { showBottomSheet = false })
            }
        }
    }
}
