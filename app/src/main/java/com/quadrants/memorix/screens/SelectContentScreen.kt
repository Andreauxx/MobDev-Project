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

    var selectedStudyGuides by remember { mutableStateOf<Set<String>>(setOf()) }
    var selectedFlashcardSets by remember { mutableStateOf<Set<String>>(setOf()) }

    var studyGuideCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var flashcardSetCategories by remember { mutableStateOf<List<String>>(emptyList()) }

    var isStudyGuideExpanded by remember { mutableStateOf(false) }
    var isFlashcardSetExpanded by remember { mutableStateOf(false) }

    // Fetch categories for study guides and flashcard sets
    LaunchedEffect(Unit) {
        firestore.collection("flashcard_sets").get().addOnSuccessListener { result ->
            val studyGuideSet = mutableSetOf<String>()
            val flashcardSetSet = mutableSetOf<String>()

            result.documents.forEach { document ->
                val category = document.getString("category") ?: "Unknown Category"
                val cards = document.get("cards") as? List<Map<String, Any>> ?: emptyList()

                if (cards.any { it["type"] == "term-definition" }) {
                    studyGuideSet.add(category)
                }
                if (cards.any { it["type"] == "multiple-choice" }) {
                    flashcardSetSet.add(category)
                }
            }

            studyGuideCategories = studyGuideSet.toList().sorted()
            flashcardSetCategories = flashcardSetSet.toList().sorted()
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
                fontSize = 24.sp,
                color = White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Study Guides Card
        ContentSelectionCard(
            title = "Study Guides",
            isExpanded = isStudyGuideExpanded,
            onExpandToggle = { isStudyGuideExpanded = !isStudyGuideExpanded },
            categories = studyGuideCategories,
            selectedCategories = selectedStudyGuides,
            onCategoryToggle = { category, selected ->
                selectedStudyGuides = if (selected) selectedStudyGuides + category else selectedStudyGuides - category
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Flashcard Sets Card
        ContentSelectionCard(
            title = "Flashcard Sets",
            isExpanded = isFlashcardSetExpanded,
            onExpandToggle = { isFlashcardSetExpanded = !isFlashcardSetExpanded },
            categories = flashcardSetCategories,
            selectedCategories = selectedFlashcardSets,
            onCategoryToggle = { category, selected ->
                selectedFlashcardSets = if (selected) selectedFlashcardSets + category else selectedFlashcardSets - category
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                if (userId != null) {
                    val preferences = mapOf(
                        "user_preferences" to mapOf(
                            "selectedStudyGuides" to selectedStudyGuides.toList(),
                            "selectedFlashcardSets" to selectedFlashcardSets.toList()
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
            },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow)
        ) {
            Text("Save", color = DarkViolet)
        }
    }
}

@Composable
fun ContentSelectionCard(
    title: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    categories: List<String>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String, Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4B2A72)),
        modifier = Modifier.fillMaxWidth().clickable { onExpandToggle() }.padding(vertical = 6.dp).clip(RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "$title Icon",
                    tint = GoldenYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))
            }
            if (isExpanded) {
                Column {
                    categories.forEach { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Text(category, fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))
                            Checkbox(
                                checked = selectedCategories.contains(category),
                                onCheckedChange = { checked -> onCategoryToggle(category, checked) },
                                colors = CheckboxDefaults.colors(checkedColor = GoldenYellow)
                            )
                        }
                    }
                }
            }
        }
    }
}
