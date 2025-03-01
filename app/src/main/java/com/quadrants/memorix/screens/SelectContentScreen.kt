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

    var selectedTermDefinitionSets by remember { mutableStateOf<Set<String>>(setOf()) }
    var selectedMultipleChoiceSets by remember { mutableStateOf<Set<String>>(setOf()) }

    var termDefinitionSets by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // (Title, Category)
    var multipleChoiceSets by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // (Title, Category)

    var isTermDefinitionExpanded by remember { mutableStateOf(false) }
    var isMultipleChoiceExpanded by remember { mutableStateOf(false) }

    // ✅ Fetch Flashcard Sets by Type
    LaunchedEffect(Unit) {
        firestore.collection("flashcard_sets").get().addOnSuccessListener { result ->
            val termDefinitionList = mutableListOf<Pair<String, String>>()
            val multipleChoiceList = mutableListOf<Pair<String, String>>()

            result.documents.forEach { document ->
                val title = document.getString("title") ?: "Untitled"
                val category = document.getString("category") ?: "Uncategorized"
                val cards = document.get("cards") as? List<Map<String, Any>> ?: emptyList()

                if (cards.any { it["type"] == "term-definition" }) {
                    termDefinitionList.add(title to category)
                }
                if (cards.any { it["type"] == "multiple-choice" }) {
                    multipleChoiceList.add(title to category)
                }
            }

            termDefinitionSets = termDefinitionList.sortedBy { it.first }
            multipleChoiceSets = multipleChoiceList.sortedBy { it.first }
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
                "Select Flashcards for Home Screen",
                fontSize = 24.sp,
                color = White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Term-Definition Flashcard Selection
        FlashcardSelectionCard(
            title = "Term-Definition Flashcards",
            icon = Icons.Default.Folder,
            isExpanded = isTermDefinitionExpanded,
            onExpandToggle = { isTermDefinitionExpanded = !isTermDefinitionExpanded },
            sets = termDefinitionSets,
            selectedSets = selectedTermDefinitionSets,
            onSetToggle = { setTitle, selected ->
                selectedTermDefinitionSets = if (selected) selectedTermDefinitionSets + setTitle else selectedTermDefinitionSets - setTitle
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Multiple-Choice Flashcard Selection
        FlashcardSelectionCard(
            title = "Multiple-Choice Flashcards",
            icon = Icons.Default.QuestionAnswer,
            isExpanded = isMultipleChoiceExpanded,
            onExpandToggle = { isMultipleChoiceExpanded = !isMultipleChoiceExpanded },
            sets = multipleChoiceSets,
            selectedSets = selectedMultipleChoiceSets,
            onSetToggle = { setTitle, selected ->
                selectedMultipleChoiceSets = if (selected) selectedMultipleChoiceSets + setTitle else selectedMultipleChoiceSets - setTitle
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ Save Button
        Button(
            onClick = {
                if (userId != null) {
                    val preferences = mapOf(
                        "user_preferences" to mapOf(
                            "selectedTermDefinitionSets" to selectedTermDefinitionSets.toList(),
                            "selectedMultipleChoiceSets" to selectedMultipleChoiceSets.toList()
                        )
                    )

                    firestore.collection("users").document(userId)
                        .set(preferences, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(navController.context, "Preferences saved!", Toast.LENGTH_SHORT).show()
                            navController.previousBackStackEntry?.savedStateHandle?.set("refreshHome", true) // ✅ Notify HomeScreen
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    categories: List<String>,
    isSelected: Boolean,
    onSelectionToggle: () -> Unit
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
                    imageVector = icon,
                    contentDescription = "$title Icon",
                    tint = GoldenYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))

                // ✅ Selection Checkbox
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = GoldenYellow)
                )
            }
            if (isExpanded) {
                Column {
                    categories.forEach { category ->
                        Text(
                            text = "• $category",
                            fontSize = 16.sp,
                            color = White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun FlashcardSelectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    sets: List<Pair<String, String>>, // (Title, Category)
    selectedSets: Set<String>,
    onSetToggle: (String, Boolean) -> Unit
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
                    imageVector = icon,
                    contentDescription = "$title Icon",
                    tint = GoldenYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontSize = 18.sp, color = White, modifier = Modifier.weight(1f))
            }
            if (isExpanded) {
                Column {
                    sets.forEach { (setTitle, category) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(setTitle, fontSize = 16.sp, color = White, fontWeight = FontWeight.Bold)
                                Text("Category: $category", fontSize = 14.sp, color = White.copy(alpha = 0.7f))
                            }
                            Checkbox(
                                checked = selectedSets.contains(setTitle),
                                onCheckedChange = { checked -> onSetToggle(setTitle, checked) },
                                colors = CheckboxDefaults.colors(checkedColor = GoldenYellow)
                            )
                        }
                    }
                }
            }
        }
    }
}
