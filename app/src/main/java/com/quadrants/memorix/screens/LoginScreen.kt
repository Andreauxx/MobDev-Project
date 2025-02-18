package com.quadrants.memorix.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) } // ✅ Toggle password visibility

    Box(
        modifier = Modifier.fillMaxSize().background(DarkViolet)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Welcome Back!",
                fontSize = 24.sp,
                color = White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = WorkSans
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = White) },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = GoldenYellow) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = White,
                    focusedContainerColor = DarkMediumViolet,
                    unfocusedContainerColor = DarkMediumViolet
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Password Input with Toggle Visibility
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = White) },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password", tint = GoldenYellow) },
                trailingIcon = {
                    val visibilityIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = visibilityIcon, contentDescription = "Toggle Password", tint = GoldenYellow)
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.85f),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = White,
                    focusedContainerColor = DarkMediumViolet,
                    unfocusedContainerColor = DarkMediumViolet
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Login Button
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please enter both email and password!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = firebaseAuth.currentUser?.uid
                                if (userId != null) {
                                    // ✅ Fetch User Details from Firestore
                                    firestore.collection("users").document(userId).get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                val userName = document.getString("name") ?: "User"
                                                Toast.makeText(context, "Welcome, $userName!", Toast.LENGTH_SHORT).show()

                                                // ✅ Navigate to Home and Clear Back Stack
                                                navController.navigate("home") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(context, "User profile not found!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to fetch profile!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                Toast.makeText(context, "Login Failed! Check email or password.", Toast.LENGTH_SHORT).show()
                            }
                            isLoading = false
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkieViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.85f).height(55.dp)
            ) {
                Text(text = "Log In", color = White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Forgot Password
            TextButton(
                onClick = { navController.navigate("forgot_password") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Forgot Password?",
                    color = White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Sign Up Redirection
            TextButton(
                onClick = { navController.navigate("signup") },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = "Don't have an account? Sign up",
                    color = White,
                    fontSize = 14.sp
                )
            }
        }

        // ✅ Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                color = GoldenYellow,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
