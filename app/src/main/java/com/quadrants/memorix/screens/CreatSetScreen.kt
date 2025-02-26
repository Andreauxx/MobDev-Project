@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.List
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
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateSetScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var newTerm by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var answers = remember { mutableStateListOf("", "", "", "") }
    var correctAnswerIndex by remember { mutableStateOf(0) }
    var isMultipleChoice by remember { mutableStateOf(false) }
    var flashcards = remember { mutableStateListOf<Map<String, Any>>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Flashcard Set", color = White) },
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
                    Text("Create a New Flashcard Set", fontSize = 24.sp, color = White)
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title", color = White) },
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Multiple Choice?", color = White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isMultipleChoice,
                            onCheckedChange = { isMultipleChoice = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldenYellow)
                        )
                    }
                }

                if (!isMultipleChoice) {
                    item {
                        OutlinedTextField(
                            value = newTerm,
                            onValueChange = { newTerm = it },
                            label = { Text("Term", color = White) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Term", tint = White) },
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
                            value = definition,
                            onValueChange = { definition = it },
                            label = { Text("Definition", color = White) },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = "Definition", tint = White) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = GoldenYellow,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it },
                            label = { Text("Question", color = White) },
                            leadingIcon = { Icon(Icons.Default.Help, contentDescription = "Question", tint = White) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = GoldenYellow,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    items(answers.size) { index ->
                        OutlinedTextField(
                            value = answers[index],
                            onValueChange = { answers[index] = it },
                            label = { Text("Option ${index + 1}", color = White) },
                            leadingIcon = { Icon(Icons.Default.List, contentDescription = "Option", tint = White) },
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
                        Text("Select Correct Answer", color = White, fontSize = 16.sp)
                        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                            answers.forEachIndexed { index, _ ->
                                RadioButton(
                                    selected = correctAnswerIndex == index,
                                    onClick = { correctAnswerIndex = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldenYellow)
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val card = if (isMultipleChoice) {
                                mapOf(
                                    "question" to question,
                                    "answers" to answers,
                                    "correctAnswerIndex" to correctAnswerIndex,
                                    "type" to "multiple-choice"
                                )
                            } else {
                                mapOf(
                                    "term" to newTerm,
                                    "definition" to definition,
                                    "type" to "term-definition"
                                )
                            }
                            flashcards.add(card)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Card", fontSize = 16.sp, color = DarkViolet)
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && category.isNotEmpty() && flashcards.isNotEmpty()) {
                                val setID = firestore.collection("flashcard_sets").document().id
                                val setData = hashMapOf(
                                    "title" to title,
                                    "category" to category,
                                    "setID" to setID,
                                    "createdBy" to "",  // Replace with actual user ID
                                    "isPublic" to true,
                                    "cards" to flashcards
                                )

                                firestore.collection("flashcard_sets").document(setID)
                                    .set(setData)
                                    .addOnSuccessListener {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        navController.popBackStack()
                                    }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Set", fontSize = 16.sp, color = DarkViolet)
                    }
                }
            }
        }
    }
}