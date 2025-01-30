package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkViolet // Background Color
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = White,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { navController.popBackStack() }
                )

                Icon(
                    painter = painterResource(id = R.drawable.owl_icon),
                    contentDescription = "Logo",
                    tint = White,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Log in Title
            Text(
                text = "Log in",
                style = TextStyle(
                    fontFamily = WorkSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email Field
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email or username", color = White, style = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Normal)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkieViolet, // Background color
                    unfocusedContainerColor = DarkieViolet, // Background color when not focused
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedIndicatorColor = Color.Transparent, // Remove underline
                    unfocusedIndicatorColor = Color.Transparent // Remove underline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkieViolet, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp)
            )


            Spacer(modifier = Modifier.height(6.dp))

            // Password Field
            TextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        text = "Password",
                        color = White,
                        style = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Normal)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkieViolet, // Background when focused
                    unfocusedContainerColor = DarkieViolet, // Background when not focused
                    focusedTextColor = White, // Text color when focused
                    unfocusedTextColor = White, // Text color when not focused
                    focusedIndicatorColor = Color.Transparent, // Remove underline
                    unfocusedIndicatorColor = Color.Transparent // Remove underline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkieViolet, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp)
            )


            Spacer(modifier = Modifier.height(16.dp))

            // Log in Button
            Button(
                onClick = { navController.navigate("onboarding") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Log in",
                    color = DarkViolet,
                    style = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign up Prompt
            Text(
                text = "Don't have an account?",
                fontSize = 16.sp,
                color = White,
                textAlign = TextAlign.Center,
            )

            TextButton(
                onClick = { navController.navigate("signup") }
            ) {
                Text(text = "Sign up", color = White, fontSize = 15.sp)
            }
        }
    }
}
