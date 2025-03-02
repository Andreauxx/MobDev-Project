@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.GoldenYellow
import com.quadrants.memorix.ui.theme.WorkSans

@Composable
fun InterestsSelectionScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    val subjectsList = listOf("Math", "Science", "History", "English", "Computer Science")
    val selectedSubjects = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkViolet)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select Your Interests",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = WorkSans
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                subjectsList.forEach { subject ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(0.85f).padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedSubjects.contains(subject),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selectedSubjects.add(subject)
                                } else {
                                    selectedSubjects.remove(subject)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = GoldenYellow,
                                uncheckedColor = Color.White
                            )
                        )
                        Text(
                            text = subject,
                            fontSize = 16.sp,
                            color = Color.White,
                            fontFamily = WorkSans
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedSubjects.isNotEmpty()) {
                        isLoading = true
                        firestore.collection("users").document(userId)
                            .update("preferences.subjects", selectedSubjects)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Preferences Saved!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home") {
                                    popUpTo("interest_selection") { inclusive = true }
                                }

                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            .addOnCompleteListener {
                                isLoading = false
                            }
                    } else {
                        Toast.makeText(context, "Please select at least one interest!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.85f).height(55.dp)
            ) {
                Text(text = "Save & Continue", color = DarkViolet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    color = GoldenYellow,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
