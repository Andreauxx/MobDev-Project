@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.GoldenYellow
import com.quadrants.memorix.ui.theme.WorkSans

@Composable
fun UserDetailsScreen(navController: NavController, userId: String, name: String, email: String) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf(name) }
    var age by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkViolet)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User Icon",
                tint = GoldenYellow,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Complete Your Profile",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = WorkSans
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", icon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(value = age, onValueChange = { age = it }, label = "Age", icon = Icons.Default.Cake)
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(value = school, onValueChange = { school = it }, label = "School", icon = Icons.Default.School)
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(value = birthday, onValueChange = { birthday = it }, label = "Birthday (YYYY-MM-DD)", icon = Icons.Default.Cake)
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(value = course, onValueChange = { course = it }, label = "Course", icon = Icons.Default.School)
            Spacer(modifier = Modifier.height(12.dp))

            PasswordTextField(value = password, onValueChange = { password = it }, label = "Password")
            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Save and Continue Button
            Button(
                onClick = {
                    val userAge = age.toIntOrNull() ?: 0
                    if (fullName.isNotEmpty() && userAge > 0 && school.isNotEmpty() && birthday.isNotEmpty() && course.isNotEmpty()) {
                        isLoading = true

                        val firebaseAuth = FirebaseAuth.getInstance()
                        val currentUser = firebaseAuth.currentUser

                        if (currentUser != null) {
                            currentUser.updatePassword(password)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Password set successfully!", Toast.LENGTH_SHORT).show()

                                    // ✅ Store user info first (without interests)
                                    val userData = hashMapOf(
                                        "name" to fullName,
                                        "email" to email,
                                        "age" to userAge,
                                        "birthday" to birthday,
                                        "school" to school,
                                        "course" to course,
                                        "profilePictureUrl" to "", // ✅ Empty for now
                                        "createdFlashcards" to emptyList<String>(),
                                        "createdQuizzes" to emptyList<String>(),
                                        "sharedWithMe" to emptyList<String>()
                                    )

                                    firestore.collection("users").document(userId).set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("interests_selection/$userId")
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnCompleteListener {
                                            isLoading = false
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to set password: ${e.message}", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                }
                        } else {
                            Toast.makeText(context, "User is not signed in. Please log in first!", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    } else {
                        Toast.makeText(context, "Please complete all fields!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.85f).height(55.dp)
            ) {
                Text(text = "Save & Continue", color = DarkViolet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = GoldenYellow,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = label, tint = GoldenYellow)
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(0.85f),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedContainerColor = Color(0xFF4A326F),
            unfocusedContainerColor = Color(0xFF4A326F)
        )
    )
}

// ✅ Password Field with Eye Icon Toggle
@Composable
fun PasswordTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = "Password", tint = GoldenYellow)
        },
        trailingIcon = {
            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = "Toggle Password", tint = GoldenYellow)
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(0.85f),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedContainerColor = Color(0xFF4A326F),
            unfocusedContainerColor = Color(0xFF4A326F)
        )
    )
}
