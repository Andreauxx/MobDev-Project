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
import androidx.compose.ui.res.painterResource
import com.quadrants.memorix.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(navController: NavController, folderName: String, category: String) {
    val files = when (category) {
        "Flashcard Sets" -> listOf("Flashcard 1", "Flashcard 2", "Flashcard 3")
        "Study Guides" -> listOf("Guide 1", "Guide 2", "Guide 3")
        "Classes" -> listOf("Lesson 1", "Lesson 2", "Lesson 3")
        "Folders" -> listOf("File 1", "File 2", "File 3")
        else -> listOf("Item 1", "Item 2", "Item 3") // Default fallback
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = folderName, color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painterResource(id = R.drawable.ic_back), contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkViolet)
            )
        },
        bottomBar = { BottomNavBar(navController, currentScreen = "folderDetail") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
        ) {
            Text(
                text = "Contents of $folderName",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(files) { file ->
                    FileItem(file)
                }
            }
        }
    }
}

@Composable
fun FileItem(fileName: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkieViolet),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* TODO: Open file */ }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = fileName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
    }
}
