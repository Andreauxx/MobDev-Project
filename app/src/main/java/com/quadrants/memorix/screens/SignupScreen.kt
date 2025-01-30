package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.*

@Composable
fun SignUpScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet) // Primary Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Owl Icon
            Icon(
                painter = painterResource(id = R.drawable.owl_icon),
                contentDescription = "Owl Icon",
                tint = White, // Accent color for branding
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Title
            Text(
                text = "The best way to study.\nSign up for free.",
                fontSize = 22.sp,
                color = White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontFamily = WorkSans
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Terms & Conditions Text
            Text(
                text = "By signing up, you accept Memorix's Terms of Service and Privacy Policy",
                fontSize = 14.sp,
                color = White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontFamily = WorkSans,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp) // Add padding

            )

            Spacer(modifier = Modifier.height(32.dp))

            // Continue with Google Button
            Button(
                onClick = { navController.navigate("onboarding") },
                colors = ButtonDefaults.buttonColors(containerColor = DarkieViolet),
                shape = RoundedCornerShape(12.dp), // Rounded edges
                modifier = Modifier
                    .fillMaxWidth(0.85f) // Match the button size from image
                    .height(55.dp) // Fixed height
                    .border(width = 1.dp, color = GoldenYellow, shape = RoundedCornerShape(12.dp)) // Border added
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google), // Google icon
                        contentDescription = "Google",
                        tint = Color.Unspecified, // Keeps original Google colors
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue with Google",
                        color = White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue with Email Button
            Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(containerColor = DarkieViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f) // Match the button size from image
                    .height(55.dp) // Fixed height
                    .border(width = 1.dp, color = GoldenYellow, shape = RoundedCornerShape(12.dp)) // Border added
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email, // Email icon
                        contentDescription = "Email",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue with Email",
                        color = White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(160.dp))

            // Log in Redirection
            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth(0.85f) // Match the button size from the image
                    .height(55.dp) // Fixed height
                    .border(width = 1.dp, color = GoldenYellow, shape = RoundedCornerShape(8.dp)) // Border added
            ) {
                Text(
                    text = "Log in",
                    color = White,
                    fontSize = 12.sp,
                    fontFamily = WorkSans,
                    fontStyle = FontStyle.Normal
                )
            }
        }
    }
}
