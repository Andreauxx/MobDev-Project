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
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance() // ✅ Initialize Firestore

    var quizTitle by remember { mutableStateOf(TextFieldValue("")) }
    var questionTimeLimit by remember { mutableStateOf(TextFieldValue("30")) }
    var newQuestion by remember { mutableStateOf(TextFieldValue("")) }
    var answerOptions by remember { mutableStateOf(mutableListOf("", "", "", "")) }
    var correctAnswerIndex by remember { mutableStateOf(0) }

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

        TextField(
            modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)),
            value = quizTitle,
            onValueChange = { quizTitle = it },
            label = { Text("Quiz Title", color = White) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Title", tint = White) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = DarkMediumViolet,
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Add Questions", color = White, fontSize = 20.sp)

        TextField(
            modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp)),
            value = newQuestion,
            onValueChange = { newQuestion = it },
            label = { Text("New Question", color = White) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "Question", tint = White) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = DarkMediumViolet,
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        answerOptions.forEachIndexed { index, option ->
            var optionText by remember { mutableStateOf(TextFieldValue(option)) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // ✅ Input field for each answer choice
                TextField(
                    modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(12.dp)),
                    value = optionText,
                    onValueChange = {
                        optionText = it
                        answerOptions[index] = it.text // ✅ Ensures updates persist
                    },
                    label = { Text("Option ${index + 1}", color = White) },
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = DarkMediumViolet,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        cursorColor = White
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // ✅ RadioButton correctly updates correctAnswerIndex
                RadioButton(
                    selected = index == correctAnswerIndex,
                    onClick = { correctAnswerIndex = index } // ✅ Fix: Properly updates selection
                )
            }

            Text(
                text = if (index == correctAnswerIndex) "✔ Correct Answer" else "",
                color = if (index == correctAnswerIndex) Color.Green else White,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
        }


        Button(
            onClick = {
                if (newQuestion.text.isNotEmpty() && answerOptions.all { it.isNotEmpty() }) {
                    val timeLimit = questionTimeLimit.text.toIntOrNull() ?: 30

                    val quizData = hashMapOf(
                        "question" to newQuestion.text,
                        "answers" to answerOptions.toList(),
                        "correctAnswerIndex" to correctAnswerIndex, // ✅ Ensure correct index is saved
                        "timeLimit" to timeLimit
                    )

                    firestore.collection("quiz_questions")
                        .add(quizData)
                        .addOnSuccessListener {
                            println("✅ Question added successfully!")
                        }
                        .addOnFailureListener { e ->
                            println("❌ Error adding question: ${e.message}")
                        }

                    // ✅ Reset form fields
                    newQuestion = TextFieldValue("")
                    answerOptions = mutableListOf("", "", "", "")
                    correctAnswerIndex = 0 // ✅ Reset selection after saving
                    questionTimeLimit = TextFieldValue("30")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MediumViolet),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Question", color = White)
        }

    }
}
