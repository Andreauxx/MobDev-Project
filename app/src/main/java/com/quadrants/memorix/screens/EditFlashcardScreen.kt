package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

    var termText by remember { mutableStateOf(term) }
    var definition by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // ✅ Load Flashcard Data from Firestore
    LaunchedEffect(Unit) {
        try {
            val documents = firestore.collection("flashcard_sets")
                .whereEqualTo("title", folderName)  // Find by Folder Name
                .whereArrayContains("cards", mapOf("term" to term))  // Find Card by Term
                .get()
                .await()

            val doc = documents.documents.firstOrNull()
            doc?.let {
                val card = (it["cards"] as? List<Map<String, Any>>)?.find { card ->
                    card["term"] == term
                }
                card?.let { c ->
                    termText = c["term"].toString()
                    definition = c["definition"].toString()
                    explanation = c["explanation"]?.toString() ?: ""
                }
            }
        } catch (e: Exception) {
            println("❌ Error loading flashcard: ${e.message}")
        }
    }

    // ✅ Layout for Editing Flashcard
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Edit Flashcard", color = White) },
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
            Text("Term", color = White)
            BasicTextField(
                value = termText,
                onValueChange = { termText = it },
                textStyle = TextStyle(color = White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f))
            )

            Text("Definition", color = White)
            BasicTextField(
                value = definition,
                onValueChange = { definition = it },
                textStyle = TextStyle(color = White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f))
            )

            Text("Explanation", color = White)
            BasicTextField(
                value = explanation,
                onValueChange = { explanation = it },
                textStyle = TextStyle(color = White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f))
            )

            // ✅ Save Button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        saveFlashcard(firestore, folderName, termText, definition, explanation) {
                            navController.popBackStack()
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

private suspend fun saveFlashcard(
    firestore: FirebaseFirestore,
    folderName: String,
    term: String,
    definition: String,
    explanation: String,
    onComplete: () -> Unit
) {
    try {
        val documents = firestore.collection("flashcard_sets")
            .whereEqualTo("title", folderName)
            .get()
            .await()

        val doc = documents.documents.firstOrNull()
        doc?.let {
            val cards = it["cards"] as? MutableList<Map<String, Any>> ?: mutableListOf()
            val cardIndex = cards.indexOfFirst { c -> c["term"] == term }

            if (cardIndex != -1) {
                cards[cardIndex] = mapOf(
                    "term" to term,
                    "definition" to definition,
                    "explanation" to explanation
                )
                doc.reference.update("cards", cards).await()
            }
            onComplete()
        }
    } catch (e: Exception) {
        println("❌ Error saving flashcard: ${e.message}")
    }
}
