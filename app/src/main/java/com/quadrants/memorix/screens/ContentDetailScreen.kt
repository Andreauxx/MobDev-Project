package com.quadrants.memorix.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDetailScreen(navController: NavController, itemName: String, itemType: String) {
    val contents = when (itemType) {
        "Flashcard Sets" -> listOf("Flashcard 1", "Flashcard 2", "Flashcard 3")
        "Study Guides" -> listOf("Guide 1", "Guide 2", "Guide 3")
        "Classes" -> listOf("Lesson 1", "Lesson 2", "Lesson 3")
        "Folders" -> listOf("File 1", "File 2", "File 3")
        else -> listOf()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = itemName,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkViolet)
            )
        },
        bottomBar = {
            BottomNavBar(navController, currentScreen = "content", onPlusClick = {})
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
        ) {
            Text(
                text = "Contents of $itemName",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(contents) { content ->
                    ContentItem(content)
                }
            }
        }
    }
}

    @Composable
fun ContentItem(content: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkieViolet),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* TODO: Open content */ }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = content,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
    }
}
