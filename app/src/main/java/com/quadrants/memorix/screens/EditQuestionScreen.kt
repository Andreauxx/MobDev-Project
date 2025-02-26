@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.White
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditQuestionScreen(navController: NavController, question: String, type: String) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var questionText by remember { mutableStateOf(question) }
    var explanation by remember { mutableStateOf("") }
    var answers by remember { mutableStateOf(List(4) { "" }) }
    var correctAnswerIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // ✅ Load Question Data from Firestore
    LaunchedEffect(Unit) {
        val collection = if (type == "Quiz") "quiz_questions" else "flashcard_sets"
        try {
            val documents = firestore.collection(collection)
                .whereEqualTo("question", question)
                .get()
                .await()

            val doc = documents.documents.firstOrNull()
            doc?.let {
                title = it.getString("title") ?: ""
                questionText = it.getString("question") ?: ""
                explanation = it.getString("explanation") ?: ""
                answers = (it["answers"] as? List<*>)?.map { answer -> answer.toString() } ?: List(4) { "" }
                correctAnswerIndex = when (val index = it["correctAnswerIndex"]) {
                    is Double -> index.toInt()
                    is Int -> index
                    else -> 0
                }
            }
        } catch (e: Exception) {
            println("❌ Error loading question: ${e.message}")
        }
    }

    // ✅ Layout for Editing Questions
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (type == "Quiz") "Edit Quiz" else "Edit Flashcard", color = White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkViolet)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ Title Input
            Text(text = "Title", color = White)
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(color = White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f))
            )

            // ✅ Question Input
            Text(text = "Question", color = White)
            BasicTextField(
                value = questionText,
                onValueChange = { questionText = it },
                textStyle = TextStyle(color = White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f))
            )

            // ✅ Explanation Input
            Text(text = "Explanation", color = White)
            BasicTextField(
                value = explanation,
                onValueChange = { explanation = it },
                textStyle = TextStyle(color = White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f))
            )

            // ✅ Answers Input (Only for Quiz)
            if (type == "Quiz") {
                Text(text = "Answers", color = White)
                answers.forEachIndexed { index, answer ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = correctAnswerIndex == index,
                            onClick = { correctAnswerIndex = index },
                            colors = RadioButtonDefaults.colors(selectedColor = Color.Green)
                        )
                        BasicTextField(
                            value = answer,
                            onValueChange = { newValue -> answers = answers.toMutableList().apply { this[index] = newValue } },
                            textStyle = TextStyle(color = White, fontSize = 16.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Gray.copy(alpha = 0.2f))
                                .padding(8.dp)
                        )
                    }
                }
            }

            // ✅ Save Button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        saveQuestion(
                            firestore,
                            type,
                            title,
                            questionText,
                            explanation,
                            answers,
                            correctAnswerIndex
                        ) {
                            navController.popBackStack() // Navigate back after saving
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Save", color = DarkViolet, fontWeight = FontWeight.Bold)
            }

            // ✅ Cancel Button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Cancel", color = White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


private suspend fun saveQuestion(
    firestore: FirebaseFirestore,
    type: String,
    title: String,
    question: String,
    explanation: String,
    answers: List<String>,
    correctAnswerIndex: Int,
    onComplete: () -> Unit
) {
    val collection = if (type == "Quiz") "quiz_questions" else "flashcard_sets"
    val data = mutableMapOf<String, Any>(
        "title" to title,
        "question" to question,
        "explanation" to explanation
    )

    if (type == "Quiz") {
        data["answers"] = answers
        data["correctAnswerIndex"] = correctAnswerIndex
    }

    try {
        val documents = firestore.collection(collection)
            .whereEqualTo("question", question)
            .get()
            .await()

        val doc = documents.documents.firstOrNull()
        doc?.reference?.update(data)?.await()
        onComplete()
    } catch (e: Exception) {
        println("❌ Error saving question: ${e.message}")
    }
}
