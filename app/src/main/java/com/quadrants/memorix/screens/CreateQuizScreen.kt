@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var timeLimit by remember { mutableStateOf("") }
    var answers = remember { mutableStateListOf("", "", "", "") }
    var correctAnswerIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Quiz", color = White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkViolet)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Create a New Quiz", fontSize = 24.sp, color = White)
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Quiz Title", color = White) },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = "Title", tint = White) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = GoldenYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category", color = White) },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = "Category", tint = White) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = GoldenYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        label = { Text("Question", color = White) },
                        leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "Question", tint = White) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = GoldenYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { explanation = it },
                        label = { Text("Explanation (Optional)", color = White) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = "Explanation", tint = White) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = GoldenYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = timeLimit,
                        onValueChange = { if (it.all { char -> char.isDigit() }) timeLimit = it },
                        label = { Text("Time Limit (Seconds)", color = White) },
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = "Time Limit", tint = White) },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = GoldenYellow,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Answer Choices", fontSize = 18.sp, color = White)
                }

                items(answers.size) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = correctAnswerIndex == index,
                            onClick = { correctAnswerIndex = index },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldenYellow)
                        )

                        OutlinedTextField(
                            value = answers[index],
                            onValueChange = { answers[index] = it },
                            label = { Text("Option ${index + 1}", color = White) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = GoldenYellow,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && category.isNotEmpty() && question.isNotEmpty() && timeLimit.isNotEmpty()) {
                                val quizID = firestore.collection("quiz_questions").document().id

                                val quizData = hashMapOf(
                                    "title" to title,
                                    "category" to category,
                                    "question" to question,
                                    "explanation" to explanation,
                                    "timeLimit" to timeLimit.toInt(),
                                    "answers" to answers,
                                    "correctAnswerIndex" to correctAnswerIndex,
                                    "quizID" to quizID
                                )

                                firestore.collection("quiz_questions").document(quizID)
                                    .set(quizData)
                                    .addOnSuccessListener {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        navController.popBackStack()
                                    }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = DarkViolet)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Quiz", fontSize = 16.sp, color = DarkViolet)
                    }
                }
            }
        }
    }
}
