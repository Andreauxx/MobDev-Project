package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFlashcardScreen(navController: NavController, folderName: String, term: String) {
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var setID by remember { mutableStateOf("") } // ✅ Store setID in state
    var termText by remember { mutableStateOf(term) }
    var definition by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var isMultipleChoice by remember { mutableStateOf(false) }
    var question by remember { mutableStateOf("") }
    var answers = remember { mutableStateListOf("", "", "", "") }
    var correctAnswerIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val documents = firestore.collection("flashcard_sets")
                .whereEqualTo("title", folderName)
                .get()
                .await()

            val doc = documents.documents.firstOrNull()
            doc?.let {
                setID = it.id
                val cards = it["cards"] as? List<Map<String, Any>>
                val card = cards?.find { c -> c["term"] == term || c["question"] == term }

                card?.let { c ->
                    isMultipleChoice = c.containsKey("question")
                    if (isMultipleChoice) {
                        question = c["question"].toString()
                        answers.clear() // ✅ Ensure the list is emptied before assigning
                        answers.addAll((c["answers"] as? List<String>) ?: listOf("", "", "", ""))
                        correctAnswerIndex = (c["correctAnswerIndex"] as? Long)?.toInt() ?: 0
                        explanation = c["explanation"]?.toString() ?: ""
                    } else {
                        termText = c["term"].toString()
                        definition = c["definition"].toString()
                        explanation = c["explanation"]?.toString() ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error loading flashcard: ${e.message}")
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Edit Flashcard", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkViolet)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isMultipleChoice) {
                IconTextField(
                    label = "Question",
                    value = question,
                    onValueChange = { question = it },
                    icon = Icons.Default.QuestionAnswer
                )

                Text("Answer Options", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                answers.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = index == correctAnswerIndex,
                            onClick = { correctAnswerIndex = index },
                            colors = RadioButtonDefaults.colors(selectedColor = GoldenYellow)
                        )

                        Spacer(modifier = Modifier.width(12.dp)) // Space before text field

                        IconTextField(
                            value = answers[index],
                            onValueChange = { newText -> answers[index] = newText },
                            icon = Icons.Default.FormatListBulleted
                        )
                    }

                }

                IconTextField(
                    label = "Explanation",
                    value = explanation,
                    onValueChange = { explanation = it },
                    icon = Icons.Default.Info
                )
            } else {
                IconTextField(
                    label = "Term",
                    value = termText,
                    onValueChange = { termText = it },
                    icon = Icons.Default.Book
                )

                IconTextField(
                    label = "Definition",
                    value = definition,
                    onValueChange = { definition = it },
                    icon = Icons.Default.Description
                )

                IconTextField(
                    label = "Explanation (Optional)",
                    value = explanation,
                    onValueChange = { explanation = it },
                    icon = Icons.Default.Info
                )
            }


            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        if (setID.isNotEmpty()) {
                            saveFlashcard(
                                firestore, folderName, setID, isMultipleChoice, termText, definition, explanation, question, answers, correctAnswerIndex
                            ) {
                                // ✅ Ensure the folder details screen reloads
                                navController.popBackStack()
                                navController.navigate("folderDetail/$folderName")
                            }
                        } else {
                            println("❌ setID is empty, cannot save flashcard")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", color = DarkViolet, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

        }
    }
}

@Composable
fun IconTextField(label: String = "", value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column {
        Text(label, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
                .padding(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = White, modifier = Modifier.size(24.dp).padding(end = 8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = White, fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private suspend fun saveFlashcard(
    firestore: FirebaseFirestore,
    folderName: String,
    setID: String,
    isMultipleChoice: Boolean,
    term: String,
    definition: String,
    explanation: String,
    question: String,
    answers: List<String>,
    correctAnswerIndex: Int,
    onComplete: () -> Unit
) {
    try {
        val docRef = firestore.collection("flashcard_sets").document(setID)
        val doc = docRef.get().await()

        if (doc.exists()) {
            val cards = (doc["cards"] as? MutableList<MutableMap<String, Any>>)?.toMutableList() ?: mutableListOf()

            val normalizedTerm = term.trim().lowercase()

            val index = cards.indexOfFirst { c ->
                if (isMultipleChoice) {
                    c["question"]?.toString()?.trim()?.lowercase() == question.trim().lowercase()
                } else {
                    c["term"]?.toString()?.trim()?.lowercase() == normalizedTerm
                }
            }

            if (index != -1) {
                val updatedCard: MutableMap<String, Any> = if (isMultipleChoice) {
                    mutableMapOf(
                        "question" to question,
                        "answers" to answers,
                        "correctAnswerIndex" to correctAnswerIndex,
                        "explanation" to explanation,
                        "type" to "multiple-choice"
                    )
                } else {
                    mutableMapOf(
                        "term" to term.trim(),
                        "definition" to definition.trim(),
                        "explanation" to explanation.trim(),
                        "type" to "term-definition"
                    )
                }

                cards[index] = updatedCard

                docRef.update("cards", cards).await()
                println("✅ Successfully updated flashcard")
            } else {
                val newCard: MutableMap<String, Any> = if (isMultipleChoice) {
                    mutableMapOf(
                        "question" to question,
                        "answers" to answers,
                        "correctAnswerIndex" to correctAnswerIndex,
                        "explanation" to explanation,
                        "type" to "multiple-choice"
                    )
                } else {
                    mutableMapOf(
                        "term" to term.trim(),
                        "definition" to definition.trim(),
                        "explanation" to explanation.trim(),
                        "type" to "term-definition"
                    )
                }

                cards.add(newCard)
                docRef.update("cards", cards).await()
                println("✅ Added new flashcard")
            }

            // ✅ Trigger recomposition by navigating back with a flag
            onComplete()
        }
    } catch (e: Exception) {
        println("❌ Error updating flashcard: ${e.message}")
    }
}
