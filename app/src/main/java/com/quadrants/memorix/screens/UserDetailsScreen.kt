@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*

@Composable
fun UserDetailsScreen(navController: NavController, userId: String, name: String, email: String) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf(name) }
    var age by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkieViolet),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Complete Your Profile",
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 🔹 Input Fields
                CustomTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", icon = Icons.Default.Person)
                CustomTextField(value = age, onValueChange = { age = it }, label = "Age", icon = Icons.Default.Cake, keyboardType = KeyboardType.Number)
                CustomTextField(value = school, onValueChange = { school = it }, label = "School", icon = Icons.Default.School)
                CustomTextField(value = birthday, onValueChange = { birthday = it }, label = "Birthday (YYYY-MM-DD)", icon = Icons.Default.Cake)
                CustomTextField(value = course, onValueChange = { course = it }, label = "Course", icon = Icons.Default.School)

                // 🔒 Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Password", tint = GoldenYellow)
                    },
                    trailingIcon = {
                        val visibilityIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = visibilityIcon, contentDescription = "Toggle Password", tint = GoldenYellow)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = GoldenYellow,
                        unfocusedBorderColor = Color.Gray,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 🔹 Save Button
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
                                        val userData = hashMapOf(
                                            "name" to fullName,
                                            "email" to email,
                                            "age" to userAge,
                                            "birthday" to birthday,
                                            "school" to school,
                                            "course" to course,
                                            "profilePictureUrl" to "",
                                            "createdFlashcards" to emptyList<String>(),
                                            "createdQuizzes" to emptyList<String>(),
                                            "sharedWithMe" to emptyList<String>()
                                        )

                                        firestore.collection("users").document(userId).set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
                                                navController.navigate("interest_selection/$userId") {
                                                    popUpTo("user_details") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnCompleteListener { isLoading = false }
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = DarkViolet, modifier = Modifier.size(20.dp))
                    } else {
                        Text(text = "Save & Continue", color = DarkViolet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 🔹 Improved Input Field with Keyboard Options
@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = label, tint = GoldenYellow)
        },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = GoldenYellow,
            unfocusedBorderColor = Color.Gray,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
