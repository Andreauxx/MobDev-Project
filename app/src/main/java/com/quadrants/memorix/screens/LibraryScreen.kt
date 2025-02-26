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
                                    navController = navController
                                ) { showAccessDenied = true }
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
    return mapOf(
        "title" to title,
        "type" to type,
        "category" to category,  // ✅ Added category
        "isLocked" to (items.firstOrNull()?.get("isLocked") ?: false),
        "itemCount" to items.size,
        "questions" to items
    )
}



@Composable
fun FolderItem(
    folderName: String,
    isLocked: Boolean,
    type: String,
    itemCount: Int,
    category: String, // ✅ Added category parameter
    items: List<Map<String, Any>>,
    navController: NavController,
    onAccessDenied: () -> Unit
) {
    val gson = Gson()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isLocked) {
                    onAccessDenied()
                } else {
                    val itemsJson = gson.toJson(items)
                    val encodedFolderName = Base64.encodeToString(folderName.toByteArray(), Base64.DEFAULT)
                    val encodedItemsJson = Base64.encodeToString(itemsJson.toByteArray(), Base64.DEFAULT)

                    when (type) {
                        "flashcard" -> navController.navigate("folderDetail/flashcard/$encodedFolderName/$encodedItemsJson")
                        "quiz" -> navController.navigate("folderDetail/quiz/$encodedFolderName/$encodedItemsJson")
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = "Folder Icon",
                tint = Color.Yellow,
                modifier = Modifier.size(32.dp).padding(end = 16.dp)
            )

            Column {
                Text(text = folderName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
                Text(text = "$itemCount items", fontSize = 14.sp, color = White.copy(alpha = 0.7f))
                Text(text = category, fontSize = 14.sp, color = White.copy(alpha = 0.5f)) // ✅ Display category
            }
        }
    }
}

