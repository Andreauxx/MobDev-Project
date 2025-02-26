package com.quadrants.memorix.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.QuestionAnswer
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.GoldenYellow
import com.quadrants.memorix.ui.theme.White

@Composable
fun SelectContentScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid

    var selectedFolders by remember { mutableStateOf<Set<String>>(setOf()) }
    var selectedCategories by remember { mutableStateOf<Set<String>>(setOf()) }
    var selectedQuestions by remember { mutableStateOf<Set<String>>(setOf()) }

    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var foldersWithQuestions by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }

    // UI state for expanded cards
    var isCategoriesExpanded by remember { mutableStateOf(false) }
    var isFoldersExpanded by remember { mutableStateOf(false) }
    var isQuestionsExpanded by remember { mutableStateOf(false) }

    // Fetch categories and folders with questions from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("quiz_questions").get().addOnSuccessListener { result ->
            val categorySet = mutableSetOf<String>()
            val folderMap = mutableMapOf<String, MutableList<String>>()

            result.documents.forEach { document ->
                val category = document.getString("category") ?: "Unknown Category"
                val folderTitle = document.getString("title") ?: "Unknown Folder"
                val question = document.getString("question") ?: ""

                categorySet.add(category)
                folderMap.getOrPut(folderTitle) { mutableListOf() }.add(question)
            }

            categories = categorySet.toList().sorted()
            foldersWithQuestions = folderMap.toMap()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
            }
            Text(
                "Select Content for Home Screen",
                fontSize = 28.sp,
                color = White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Categories Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4B2A72)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isCategoriesExpanded = !isCategoriesExpanded }
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Category Icon",
                        tint = GoldenYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Categories", fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))
                }
                if (isCategoriesExpanded) {
                    Column {
                        categories.forEach { category ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(category, fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = selectedCategories.contains(category),
                                    onCheckedChange = { checked ->
                                        selectedCategories = if (checked) selectedCategories + category else selectedCategories - category
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = GoldenYellow)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Folders Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4B2A72)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isFoldersExpanded = !isFoldersExpanded }
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder Icon",
                        tint = GoldenYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Folders", fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))
                }
                if (isFoldersExpanded) {
                    Column {
                        foldersWithQuestions.keys.forEach { folder ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(folder, fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))
                                Checkbox(
                                    checked = selectedFolders.contains(folder),
                                    onCheckedChange = { checked ->
                                        selectedFolders = if (checked) selectedFolders + folder else selectedFolders - folder
                                        val folderQuestions = foldersWithQuestions[folder] ?: emptyList()
                                        selectedQuestions = if (checked) selectedQuestions + folderQuestions else selectedQuestions - folderQuestions.toSet()
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = GoldenYellow)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Questions Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4B2A72)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isQuestionsExpanded = !isQuestionsExpanded }
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = "Question Icon",
                        tint = GoldenYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Questions", fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))
                }
                if (isQuestionsExpanded) {
                    Column {
                        foldersWithQuestions.forEach { (folderTitle, questions) ->
                            Text(folderTitle, fontSize = 18.sp, color = White, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
                            questions.forEach { question ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                                ) {
                                    Text(question, fontSize = 16.sp, color = White, modifier = Modifier.weight(1f))
                                    Checkbox(
                                        checked = selectedQuestions.contains(question),
                                        onCheckedChange = { checked ->
                                            selectedQuestions = if (checked) selectedQuestions + question else selectedQuestions - question
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = GoldenYellow)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button (Fixed at Bottom)
        Button(
            onClick = {
                if (userId != null) {
                    if (selectedFolders.isEmpty() && selectedCategories.isEmpty() && selectedQuestions.isEmpty()) {
                        Toast.makeText(navController.context, "Please select at least one item.", Toast.LENGTH_SHORT).show()
                    } else {
                        val preferences = mapOf(
                            "user_preferences" to mapOf(
                                "selectedFolders" to selectedFolders.toList(),
                                "selectedCategories" to selectedCategories.toList(),
                                "selectedQuestions" to selectedQuestions.toList()
                            )
                        )

                        firestore.collection("users").document(userId)
                            .set(preferences, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(navController.context, "Preferences saved!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(navController.context, "Failed to save preferences", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(navController.context, "User not authenticated", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow)
        ) {
            Text("Save", color = DarkViolet)
        }
    }
}
