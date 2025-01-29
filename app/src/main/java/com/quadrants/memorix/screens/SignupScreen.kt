package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.R

@Composable
fun SignUpScreen(navController: NavController) { // Accept NavController
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient( // Corrected gradient type
                    colors = listOf(
                        Color(0xFF6C3393), // Purple
                        Color(0xFF210F2D) // Darker purple
                    )
                )
            ) // Purple background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Icon(
                painter = painterResource(id = R.drawable.owl_icon), // Ensure this exists
                contentDescription = "Owl Icon",
                tint = Color.White,
                modifier = Modifier.size(210.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "The best way to study.\nSign up for free.",
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "By signing up, you accept Memorix's Terms of Service and Privacy Policy",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* TODO: Handle Google Sign-In */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = "Continue with Google", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Handle Email Sign-Up */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = "Continue with Email", color = Color.White)
            }

            Spacer(modifier = Modifier.height(180.dp))

            TextButton(
                onClick = { navController.navigate("login") } // Navigate to login
            ) {
                Text(text = "Log in", color = Color.White)
            }
        }
    }
}
