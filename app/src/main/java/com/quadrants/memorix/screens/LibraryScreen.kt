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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.quadrants.memorix.ui.theme.RoyalBlue
import com.quadrants.memorix.ui.theme.White

data class Folder(
    val name: String,
    val itemsCount: Int,
    val lastModified: String,
    val category: String,
    val isLocked: Boolean = false,
    val type: String // "Flashcard" or "Quiz"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController, userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    var flashcardSets by remember { mutableStateOf<List<FlashcardSet>>(emptyList()) }
    var quizzes by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Computer Science", "Mathematics", "Physics")

    // Fetch flashcards
    LaunchedEffect(Unit) {
        firestore.collection("flashcard_sets")
            .get()
            .addOnSuccessListener { result ->
                flashcardSets = result.documents.mapNotNull { doc ->
                    FlashcardSet(
                        title = doc.getString("title") ?: "Untitled Set",
                        category = doc.getString("category") ?: "Uncategorized",
                        isPublic = doc.getBoolean("isPublic") ?: true,
                        createdBy = doc.getString("createdBy") ?: "",
                        cards = (doc.get("cards") as? List<Map<String, Any>>)?.map {
                            Flashcard(
                                type = it["type"] as? String ?: "term-definition",
                                term = it["term"] as? String ?: "",
                                definition = it["definition"] as? String ?: "",
                                explanation = it["explanation"] as? String ?: ""
                            )
                        } ?: emptyList()
                    )
                }
            }
    }

    // Fetch quizzes
    LaunchedEffect(Unit) {
        firestore.collection("quiz_questions")
            .get()
            .addOnSuccessListener { result ->
                quizzes = result.documents.mapNotNull { doc ->
                    QuizQuestion(
                        question = doc.getString("question") ?: "",
                        answers = doc.get("answers") as? List<String> ?: emptyList(),
                        correctAnswerIndex = doc.getLong("correctAnswerIndex")?.toInt() ?: 0,
                        explanation = doc.getString("explanation") ?: ""
                    )
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkViolet)) {
        // Category selection
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                Text(
                    text = category,
                    fontSize = 16.sp,
                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                    color = White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCategory == category) DarkieViolet else Color.Transparent)
                        .padding(12.dp)
                        .clickable { selectedCategory = category }
                )
            }
        }

        // Flashcards section
        Text(
            text = "📚 Flashcard Sets",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.padding(horizontal = 16.dp)) {
            items(flashcardSets.filter { it.category == selectedCategory || selectedCategory == "All" }) { set ->
                val isLocked = !set.isPublic && set.createdBy != userId
                val folder = Folder(
                    name = set.title,
                    itemsCount = set.cards.size,
                    lastModified = "Updated recently",
                    category = set.category,
                    isLocked = isLocked,
                    type = "Flashcard"
                )
                FolderItem(folder, navController)
            }
        }

        // Quizzes section
        Text(
            text = "📝 Quizzes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.padding(horizontal = 16.dp)) {
            items(quizzes) { quiz ->
                val folder = Folder(
                    name = quiz.question.take(20) + "...",
                    itemsCount = quiz.answers.size,
                    lastModified = "Updated recently",
                    category = "Quiz",
                    type = "Quiz"
                )
                FolderItem(folder, navController)
            }
        }
    }
}

@Composable
fun FolderItem(folder: Folder, navController: NavController) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkieViolet),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable {
                if (!folder.isLocked) {
                    navController.navigate("folderDetail/${folder.name}/${folder.category}")
                }
            }
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (folder.isLocked) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color.Red)
            } else {
                Icon(painter = painterResource(id = R.drawable.ic_folder), contentDescription = "Folder", tint = RoyalBlue)
            }

            Text(text = folder.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
            Text(text = "${folder.itemsCount} items", fontSize = 14.sp, color = White.copy(alpha = 0.7f))
            Text(text = folder.lastModified, fontSize = 12.sp, color = White.copy(alpha = 0.5f))
        }
    }
}
