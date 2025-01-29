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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    // Theme colors
    val backgroundColor = Color(0xFF1A001F) // Dark purple background
    val buttonColor = Color(0xFF736B70) // Light purple/gray for the button
    val textColor = Color.White

    // State management for input fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Content (Back Button and Logo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() } // Navigate back
                )

                // Logo Icon
                Icon(
                    painter = painterResource(id = R.drawable.owl_icon),
                    contentDescription = "Logo",
                    tint = textColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // "Log in" Title
            Text(
                text = "Log in",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Email or Username Input Field
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = "Email or username") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = buttonColor,
                    unfocusedContainerColor = buttonColor,
                    disabledContainerColor = buttonColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            )

            // Password Input Field
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = "Password") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = buttonColor,
                    unfocusedContainerColor = buttonColor,
                    disabledContainerColor = buttonColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            )

            // Log In Button
            Button(
                onClick = { /* TODO: Handle login logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Log in", color = textColor)
            }

            // Forgot Password Text
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier
                    .clickable { /* TODO: Handle forgot password */ }
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Sign-up Redirection Text
            Text(
                text = "Don't have an account?",
                fontSize = 16.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            TextButton(
                onClick = { navController.navigate("signup") } // Navigate to SignUp
            ) {
                Text(text = "Sign up", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer Text
            Text(
                text = "Memorix Inc.",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
