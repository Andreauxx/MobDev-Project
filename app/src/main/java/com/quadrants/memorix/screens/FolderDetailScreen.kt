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
    val items = remember { mutableStateListOf<Map<String, Any>>() }
    var showBottomSheet by remember { mutableStateOf(false) }

    // ✅ Fetch All Flashcards and Quizzes
    LaunchedEffect(folderName) {
        try {
            val documents = firestore.collection("flashcard_sets")
                .whereEqualTo("title", folderName)
                .get()
                .await()

            val allCards = mutableListOf<Map<String, Any>>()

            for (doc in documents.documents) {
                val cardList = doc["cards"] as? List<Map<String, Any>> ?: emptyList()
                allCards.addAll(cardList)  // ✅ Merge all cards from different documents
            }

            // 🔹 Debugging: Print retrieved cards
            println("📦 Total Cards Retrieved: ${allCards.size}")
            allCards.forEach { println("🃏 Card: $it") }

            // ✅ Ensure unique term-definition & multiple-choice cards
            items.clear()
            items.addAll(allCards.filter { it["type"] == "term-definition" || it["type"] == "multiple-choice" })

            println("✅ Final Unique Cards Count: ${items.size}")

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
                    items(items, key = { it["term"]?.toString() ?: it["question"]?.toString() ?: "" }) { card ->
                        println("🎴 Rendering card in UI: ${card["term"] ?: card["question"]}")

                        when (card["type"]) {
                            "term-definition" -> FlashcardItem(item = card, navController = navController, isCreator = isCreator, folderName = folderName)
                            "multiple-choice" -> QuestionItem(item = card, navController = navController, isCreator = isCreator, folderName = folderName)
                            else -> println("⚠️ Unknown card type: ${card["type"]}")
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
    val explanation = item["explanation"]?.toString() ?: ""

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) } // Ensure UI updates
    val firestore = FirebaseFirestore.getInstance()

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
                else "✅ Correct Answer: ${answers.getOrNull(correctAnswerIndex) ?: "N/A"}",
                fontSize = 14.sp,
                color = White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            if (isCreator) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Delete", color = White)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Question") },
            text = { Text("Are you sure you want to delete this question? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        firestore.collection("flashcard_sets")
                            .whereEqualTo("title", folderName)
                            .get()
                            .addOnSuccessListener { documents ->
                                val doc = documents.documents.firstOrNull()
                                doc?.let {
                                    val updatedCards = (it["cards"] as? List<Map<String, Any>>)?.toMutableList()
                                    updatedCards?.remove(item)

                                    firestore.collection("flashcard_sets")
                                        .document(it.id)
                                        .update("cards", updatedCards)
                                        .addOnSuccessListener {
                                            showSuccessMessage = true
                                            items = updatedCards?.toList() ?: emptyList()

                                        }
                                }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = White)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessMessage) {
        LaunchedEffect(Unit) {
            showSuccessMessage = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showSuccessMessage = false }) {
                    Text("OK", color = White)
                }
            }
        ) {
            Text("Question deleted successfully!", color = White)
        }
    }
}


@Composable
fun FlashcardItem(item: Map<String, Any>, navController: NavController, isCreator: Boolean, folderName: String) {
    val term = item["term"]?.toString() ?: "No term available"
    val definition = item["definition"]?.toString() ?: "No definition available"

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()
    var items by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) } // Ensure UI updates

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkieViolet),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = term, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
            Text(text = "Definition: $definition", fontSize = 16.sp, color = White.copy(alpha = 0.7f))

            if (isCreator) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            navController.navigate("editFlashcard/${Uri.encode(folderName)}/${Uri.encode(term)}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MediumViolet),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Edit", color = DarkViolet)
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Delete", color = White)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Flashcard") },
            text = { Text("Are you sure you want to delete this flashcard? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        firestore.collection("flashcard_sets")
                            .whereEqualTo("title", folderName)
                            .get()
                            .addOnSuccessListener { documents ->
                                val doc = documents.documents.firstOrNull()
                                doc?.let {
                                    val updatedCards = (it["cards"] as? List<Map<String, Any>>)?.toMutableList()
                                    updatedCards?.remove(item)

                                    firestore.collection("flashcard_sets")
                                        .document(it.id)
                                        .update("cards", updatedCards)
                                        .addOnSuccessListener {
                                            // ✅ Update UI immediately
                                            items = updatedCards?.toList() ?: emptyList()
                                            showSuccessMessage = true
                                        }
                                }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = White)
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessMessage) {
        LaunchedEffect(Unit) {
            showSuccessMessage = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showSuccessMessage = false }) {
                    Text("OK", color = White)
                }
            }
        ) {
            Text("Flashcard deleted successfully!", color = White)
        }
    }
}
