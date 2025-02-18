package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser // Get logged-in user

    var quizTitle by remember { mutableStateOf(TextFieldValue("")) }
    var questionTimeLimit by remember { mutableStateOf(TextFieldValue("30")) }
    var newQuestion by remember { mutableStateOf(TextFieldValue("")) }
    var explanation by remember { mutableStateOf(TextFieldValue("")) } // ✅ Optional Explanation Field
    var answerOptions by remember { mutableStateOf(mutableListOf("", "", "", "")) }
    var correctAnswerIndex by remember { mutableStateOf(0) }
    var category by remember { mutableStateOf(TextFieldValue("")) }
    var difficulty by remember { mutableStateOf("Medium") } // ✅ Default difficulty
    var isPublic by remember { mutableStateOf(true) } // ✅ Control if the question is public
    var sharedWith by remember { mutableStateOf(mutableListOf<String>()) } // ✅ Store shared users

    var showDialog by remember { mutableStateOf(false) } // ✅ Success dialog

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().height(70.dp),
            title = { Text("Create Quiz", color = White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MediumViolet)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ Quiz Title
        TextField(
            modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)),
            value = quizTitle,
            onValueChange = { quizTitle = it },
            label = { Text("Quiz Title", color = White) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Question Field
        TextField(
            modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)),
            value = newQuestion,
            onValueChange = { newQuestion = it },
            label = { Text("New Question", color = White) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Difficulty Selection
        DropdownMenu(
            expanded = false,
            onDismissRequest = {},
        ) {
            DropdownMenuItem(onClick = { difficulty = "Easy" }, text = { Text("Easy") })
            DropdownMenuItem(onClick = { difficulty = "Medium" }, text = { Text("Medium") })
            DropdownMenuItem(onClick = { difficulty = "Hard" }, text = { Text("Hard") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Public/Private Toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Public:", color = White)
            Switch(checked = isPublic, onCheckedChange = { isPublic = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Button to Add Question to Firestore
        Button(
            onClick = {
                if (newQuestion.text.isNotEmpty() && answerOptions.all { it.isNotEmpty() }) {
                    val timeLimit = questionTimeLimit.text.toIntOrNull() ?: 30

                    val quizData = hashMapOf(
                        "question" to newQuestion.text,
                        "answers" to answerOptions.toList(),
                        "correctAnswerIndex" to correctAnswerIndex,
                        "timeLimit" to timeLimit,
                        "category" to category.text,
                        "difficulty" to difficulty,
                        "questionType" to "multiple_choice"
                    ).toMutableMap()

                    // ✅ Set creator ID only if it's private
                    if (!isPublic) {
                        quizData["createdBy"] = currentUser?.uid ?: ""
                        quizData["sharedWith"] = sharedWith // ✅ List of users who can access
                    }

                    firestore.collection("quiz_questions")
                        .add(quizData)
                        .addOnSuccessListener {
                            showDialog = true // ✅ Show success dialog
                        }
                        .addOnFailureListener { e ->
                            println("❌ Error adding question: ${e.message}")
                        }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MediumViolet),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Question", color = White)
        }

        // ✅ Show Dialog on Successful Addition
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = { Button(onClick = { showDialog = false }) { Text("OK") } },
                title = { Text("Success") },
                text = { Text("✅ Problem Added!") }
            )
        }
    }
}
