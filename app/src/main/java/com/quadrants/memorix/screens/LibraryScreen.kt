@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import android.util.Base64
import android.net.Uri
import com.google.gson.Gson
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.MainActivity
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


@Composable
fun LibraryScreen(navController: NavController, userId: String, activity: MainActivity,onPlusClick: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    var folders by remember { mutableStateOf<Map<String, List<Map<String, Any>>>>(emptyMap()) }
    var showAccessDenied by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val categories = listOf("All", "Flashcard Sets", "Quizzes")
    var selectedCategory by remember { mutableStateOf("All") }
    var generatedContent by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Function to handle deletion
    fun deleteItem(type: String, title: String) {
        val collection = if (type == "flashcard") "flashcard_sets" else "quiz_questions"
        firestore.collection(collection)
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
            }
            .addOnFailureListener { e ->
                println("❌ Error deleting item: ${e.message}")
            }
    }


    // ✅ Fetch generated AI quizzes and flashcards
    LaunchedEffect(Unit) {
        fetchGeneratedContent { contentList ->
            generatedContent = contentList
        }
    }

    // ✅ Firestore Query with Grouping by Title
    LaunchedEffect(Unit) {
        try {
            withContext(Dispatchers.IO) {
                val flashcards = firestore.collection("flashcard_sets").get().await().documents.mapNotNull { it.data }
                val quizzes = firestore.collection("quiz_questions").get().await().documents.mapNotNull { it.data }

                val flashcardGroups = flashcards.groupBy { it["title"]?.toString() ?: "Untitled" }
                val quizGroups = quizzes.groupBy { it["title"]?.toString() ?: "Untitled" }

                folders = mapOf(
                    "Flashcard Sets" to flashcardGroups.map { (title, items) ->
                        mergeGroup(title, items, "flashcard", items.firstOrNull()?.get("category")?.toString() ?: "General")
                    },
                    "Quizzes" to quizGroups.map { (title, items) ->
                        mergeGroup(title, items, "quiz", items.firstOrNull()?.get("category")?.toString() ?: "General")
                    }
                )
            }
        } catch (e: Exception) {
            println("❌ Firestore Error: ${e.message}")
        }
    }



    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, currentScreen = "folders", onPlusClick = onPlusClick)
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
        ) {
            // ✅ Wrap Text and Spacer inside Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Library",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                )
                Spacer(modifier = Modifier.height(1.dp)) // ✅ This will now be visible

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✅ Category Selection Tabs
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
                        categories.forEach { category ->
                            Text(
                                text = category,
                                fontSize = 16.sp,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                                color = White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedCategory == category) MediumViolet else Color.Transparent)
                                    .padding(12.dp)
                                    .clickable { selectedCategory = category }
                            )
                        }
                    }
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        folders.filterKeys {
                            selectedCategory == "All" || it.equals(selectedCategory, ignoreCase = true)
                        }.forEach { (folderName, items) ->
                            item { Text(folderName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White) }
                            items(items) { item ->
                                FolderItem(
                                    folderName = item["title"]?.toString() ?: "Unknown",
                                    isLocked = item["isLocked"] as? Boolean ?: false,
                                    type = item["type"]?.toString() ?: "",
                                    itemCount = item["itemCount"] as? Int ?: 0,
                                    category = item["category"]?.toString() ?: "General",
                                    items = item["questions"] as? List<Map<String, Any>> ?: emptyList(),
                                    navController = navController,
                                    onAccessDenied = { showAccessDenied = true },
                                    onDelete = { deleteItem(item["type"]?.toString() ?: "", item["title"]?.toString() ?: "") }
                                )
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

fun mergeGroup(title: String, items: List<Map<String, Any>>, type: String, category: String): Map<String, Any> {
    val totalItems = if (type == "flashcard") {
        items.sumOf { (it["cards"] as? List<*>)?.size ?: 0 } // ✅ Count flashcards inside all documents
    } else {
        items.size // ✅ For quizzes, count the number of documents (since they are stored differently)
    }

    return mapOf(
        "title" to title,
        "type" to type,
        "category" to category,
        "isLocked" to (items.firstOrNull()?.get("isLocked") ?: false),
        "itemCount" to totalItems,  // ✅ Properly count flashcards or quizzes
        "questions" to if (type == "flashcard") {
            items.flatMap { it["cards"] as? List<Map<String, Any>> ?: emptyList() } // ✅ Flatten all flashcards
        } else {
            items // ✅ For quizzes, keep them as they are
        }
    )
}

private fun fetchGeneratedContent(onContentFetched: (List<Map<String, Any>>) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("cards")
        .get()
        .addOnSuccessListener { cardDocs ->
            val flashcards = cardDocs.documents.mapNotNull { it.data }

            firestore.collection("quiz_questions")
                .get()
                .addOnSuccessListener { quizDocs ->
                    val quizzes = quizDocs.documents.mapNotNull { it.data }

                    onContentFetched(flashcards + quizzes) // ✅ Merge flashcards & quizzes
                }
                .addOnFailureListener { e ->
                    println("❌ Error fetching quizzes: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            println("❌ Error fetching flashcards: ${e.message}")
        }
}




@Composable
fun FolderItem(
    folderName: String,
    isLocked: Boolean,
    type: String,
    itemCount: Int,
    category: String,
    items: List<Map<String, Any>>,
    navController: NavController,
    onAccessDenied: () -> Unit,
    onDelete: () -> Unit
) {
    val gson = Gson()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isLocked) {
                    onAccessDenied()
                } else {
                    val safeItems = items ?: emptyList()
                    val itemsJson = gson.toJson(safeItems)
                    val encodedFolderName = Base64.encodeToString(folderName.toByteArray(), Base64.DEFAULT)
                    val encodedItemsJson = Uri.encode(Base64.encodeToString(itemsJson.toByteArray(), Base64.DEFAULT))

                    println("📌 Navigating with: $encodedFolderName / $encodedItemsJson")

                    when (type) {
                        "flashcard" -> navController.navigate("folderDetail/flashcard/$encodedFolderName/$encodedItemsJson")
                        "quiz" -> navController.navigate("home?quizJson=$encodedItemsJson")
                    }
                }
            }
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color.Gray else DarkMediumViolet
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = "Folder Icon",
                tint = Color.Yellow,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = folderName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "$itemCount items",
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.7f)
                )
                Text(
                    text = category,
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.5f)
                )
            }

            // Edit Button (for Quizzes)
            if (type == "quiz") {
                IconButton(
                    onClick = {
                        navController.navigate("editQuiz/${Uri.encode(folderName)}")
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit Icon",
                        tint = Color.White
                    )
                }
            }

            // Delete Button (for Flashcards & Quizzes)
            IconButton(
                onClick = { showConfirmDialog = true }, // Show confirmation dialog before deleting
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete Icon",
                    tint = Color.Red
                )
            }
        }
    }

    // ✅ Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete \"$folderName\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirmDialog = false
                        showSuccessDialog = true
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ✅ Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text("\"$folderName\" has been successfully deleted.") },
            confirmButton = {
                TextButton(
                    onClick = { showSuccessDialog = false }
                ) {
                    Text("OK", color = MediumViolet)
                }
            }
        )
    }
}

