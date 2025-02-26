package com.quadrants.memorix.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.Flashcard
import com.quadrants.memorix.FlashcardSet
import com.quadrants.memorix.QuizQuestion
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.DarkieViolet
import com.quadrants.memorix.ui.theme.MediumViolet
import com.quadrants.memorix.ui.theme.RoyalBlue
import com.quadrants.memorix.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuizScreen(navController: NavController, quizName: String) {
    val firestore = FirebaseFirestore.getInstance()
    var question by remember { mutableStateOf(TextFieldValue("")) }
    var answerOptions by remember { mutableStateOf(mutableListOf("", "", "", "")) }
    var correctAnswerIndex by remember { mutableStateOf(0) }

    LaunchedEffect(quizName) {
        firestore.collection("quiz_questions").whereEqualTo("question", quizName).get()
            .addOnSuccessListener { result ->
                val document = result.documents.firstOrNull()
                if (document != null) {
                    question = TextFieldValue(document.getString("question") ?: "")
                    val answers = document.get("answers") as List<String>?
                    answers?.let {
                        answerOptions = it.toMutableList()
                    }
                    correctAnswerIndex = document.getLong("correctAnswerIndex")?.toInt() ?: 0
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkViolet).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Edit Quiz", color = White) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MediumViolet)
        )

        TextField(value = question, onValueChange = { question = it }, label = { Text("Question", color = White) })

        answerOptions.forEachIndexed { index, option ->
            TextField(value = TextFieldValue(option), onValueChange = {
                answerOptions[index] = it.text
            }, label = { Text("Option ${index + 1}", color = White) })
            RadioButton(selected = index == correctAnswerIndex, onClick = { correctAnswerIndex = index })
        }

        Button(onClick = {
            firestore.collection("quiz_questions").whereEqualTo("question", quizName).get()
                .addOnSuccessListener { result ->
                    val document = result.documents.firstOrNull()
                    if (document != null) {
                        val updatedData = hashMapOf(
                            "question" to question.text,
                            "answers" to answerOptions.toList(),
                            "correctAnswerIndex" to correctAnswerIndex
                        )
                        firestore.collection("quiz_questions").document(document.id).set(updatedData)
                            .addOnSuccessListener { navController.popBackStack() }
                    }
                }
        }) {
            Text("Save Changes")
        }
    }
}
