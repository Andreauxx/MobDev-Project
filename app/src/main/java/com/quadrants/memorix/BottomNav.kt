// BottomNav.kt
package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.MainActivity
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkieViolet

// ✅ Bottom Navigation Bar
// ✅ BottomNavBar
@Composable
fun BottomNavBar(
    navController: NavController,
    currentScreen: String,
    onPlusClick: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = DarkieViolet
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomNavItem(R.drawable.ic_home, "Home", currentScreen == "home") {
                if (currentScreen != "home") {
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                }
            }
            BottomNavItem(R.drawable.ic_folder, "Library", currentScreen == "folders") {  // ✅ Changed from "library" to "folders"
                if (currentScreen != "folders") {
                    navController.navigate("folders") { popUpTo("folders") { inclusive = true } }
                }
            }

            IconButton(onClick = onPlusClick) {
                Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add", tint = Color.White)
            }

            BottomNavItem(R.drawable.ic_barchart, "Stats", currentScreen == "stats") {
                if (currentScreen != "stats") {
                    navController.navigate("stats") { popUpTo("stats") { inclusive = true } }
                }
            }
            BottomNavItem(R.drawable.ic_profile, "Profile", currentScreen == "profile") {
                if (currentScreen != "profile") {
                    navController.navigate("profile") { popUpTo("profile") { inclusive = true } }
                }
            }
        }
    }
}

// ✅ Bottom Navigation Item
@Composable
fun BottomNavItem(iconId: Int, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = label,
            tint = if (isSelected) Color(0xFFFFD700) else Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}

// ✅ Bottom Sheet Content
@Composable
fun BottomSheetContent(navController: NavController, activity: MainActivity, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(50.dp)
                .height(6.dp)
                .background(Color.Gray, shape = RoundedCornerShape(50))
        )

        Spacer(modifier = Modifier.height(16.dp))

        BottomSheetItem(R.drawable.ic_flashcard, "Flashcard Set") {
            navController.navigate("flashcard")
            onDismiss()
        }
        BottomSheetItem(R.drawable.ic_quiz, "Create Quiz") {
            navController.navigate("quiz")
            onDismiss()
        }
        BottomSheetItem(R.drawable.ic_class, "Create a Class") {
            navController.navigate("classScreen")
            onDismiss()
        }
        BottomSheetItem(R.drawable.ic_upload, "Upload Image/PDF") {
            activity.openFileOrCamera()
            onDismiss()
        }
    }
}

@Composable
fun BottomSheetItem(iconId: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF4C2C70))
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = Color.White, fontSize = 16.sp)
    }
}
