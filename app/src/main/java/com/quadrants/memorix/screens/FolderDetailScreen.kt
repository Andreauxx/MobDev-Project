@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.MainActivity
import com.quadrants.memorix.ui.theme.*
import kotlinx.coroutines.tasks.await

@Composable
fun FolderDetailScreen(folderName: String, navController: NavController, isCreator: Boolean, activity: MainActivity) {
    val firestore = FirebaseFirestore.getInstance()
    var items by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // ✅ Reload flashcards every time the screen recomposes
    LaunchedEffect(folderName) {
        try {
            val documents = firestore.collection("flashcard_sets")
                .whereEqualTo("title", folderName)
                .get()
                .await()

            val doc = documents.documents.firstOrNull()
            doc?.let {
                items = it["cards"] as? List<Map<String, Any>> ?: emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error loading flashcards: ${e.message}")
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, currentScreen = "folderDetail", onPlusClick = { showBottomSheet = true })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(folderName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { card ->
                        when {
                            card.containsKey("term") && card.containsKey("definition") -> {
                                FlashcardItem(item = card, navController = navController, isCreator = isCreator, folderName = folderName)
                            }
                            card.containsKey("question") && card.containsKey("answers") -> {
                                QuestionItem(item = card, navController = navController, isCreator = isCreator, folderName = folderName)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            BottomSheetContent(navController, activity) { showBottomSheet = false }
        }
    }
}


@Composable
fun QuestionItem(item: Map<String, Any>, navController: NavController, isCreator: Boolean, folderName: String) {
    val question = item["question"]?.toString() ?: "No question available"
    val answers = item["answers"] as? List<String> ?: emptyList()

    val correctAnswerIndex = (item["correctAnswerIndex"] as? Number)?.toInt() ?: -1

    val correctAnswer = if (correctAnswerIndex in answers.indices) {
        answers[correctAnswerIndex].toString()
    } else {
        "N/A"
    }

    val explanation = item["explanation"]?.toString() ?: ""

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkieViolet),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = question, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)

            if (answers.isNotEmpty()) {
                answers.forEachIndexed { index, answer ->
                    Text(
                        text = "${index + 1}. $answer",
                        fontSize = 16.sp,
                        color = if (index == correctAnswerIndex) Color.Green else White.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = if (explanation.isNotEmpty()) "Explanation: $explanation"
                else "✅ Correct Answer: $correctAnswer",
                fontSize = 14.sp,
                color = White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            if (isCreator) {
                Button(
                    onClick = {
                        val encodedFolder = Uri.encode(folderName)
                        val encodedTerm = Uri.encode(question)
                        navController.navigate("editFlashcard/$encodedFolder/$encodedTerm")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MediumViolet),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Edit", color = DarkViolet)
                }
            }
        }
    }
}

@Composable
fun FlashcardItem(item: Map<String, Any>, navController: NavController, isCreator: Boolean, folderName: String) {
    val term = item["term"]?.toString() ?: "No term available"
    val definition = item["definition"]?.toString() ?: "No definition available"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkieViolet),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            //.clickable { navController.navigate("viewQuestion/${Uri.encode(term)}") }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = term, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
            Text(text = "Definition: $definition", fontSize = 16.sp, color = White.copy(alpha = 0.7f))

            if (isCreator) {
                Button(
                    onClick = {
                        navController.navigate("editFlashcard/${Uri.encode(folderName)}/${Uri.encode(term)}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MediumViolet),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Edit", color = DarkViolet)
                }

            }
        }
    }
}
