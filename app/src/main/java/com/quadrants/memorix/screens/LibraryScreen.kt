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
import com.quadrants.memorix.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.quadrants.memorix.ui.theme.*

data class Folder(
    val name: String,
    val itemsCount: Int,
    val lastModified: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {
    val systemUiController = rememberSystemUiController()

    // **Set Status Bar & Navigation Bar Colors**
    SideEffect {
        systemUiController.setStatusBarColor(DarkViolet, darkIcons = false)
        systemUiController.setNavigationBarColor(DarkieViolet, darkIcons = false)
    }

    val folders = listOf(
        Folder("Calculus", 5, "Updated 2 days ago", "Flashcard Sets"),
        Folder("Mobile Dev", 5, "Updated 3 hours ago", "Flashcard Sets"),
        Folder("Data Structures", 5, "Updated 1 week ago", "Study Guides"),
        Folder("Physics", 3, "Updated yesterday", "Study Guides"),
        Folder("Chemistry", 7, "Updated today", "Classes"),
        Folder("AI & Machine Learning", 10, "Updated 5 hours ago", "Folders"),
        Folder("Algebra", 6, "Updated 4 days ago", "Classes"),
        Folder("Software Engineering", 8, "Updated 2 weeks ago", "Folders")
    )

    var selectedCategory by remember { mutableStateOf("All") } // ✅ Default to "All"
    val categories = listOf("All", "Flashcard Sets", "Study Guides", "Classes", "Folders") // ✅ Include "All"

    // **Filter Folders Based on Selected Category**
    val filteredFolders = if (selectedCategory == "All") folders else folders.filter { it.category == selectedCategory }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add folder logic */ },
                containerColor = MediumViolet,
                contentColor = White
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Folder"
                )
            }
        },
        bottomBar = {
            BottomNavBar(navController, currentScreen = "library", onPlusClick = { /* TODO: Handle Modal */ })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
        ) {
            // **Header Section**
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Library",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Icon(
                    painter = painterResource(id = R.drawable.owl_icon),
                    contentDescription = "Logo",
                    tint = White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // **Category Selection Tabs**
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    Text(
                        text = category,
                        fontSize = 16.sp,
                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedCategory == category) White else White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { selectedCategory = category }
                            .padding(8.dp)
                    )
                }
            }

            Divider(color = White.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

            // **Folder Grid View**
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredFolders) { folder ->
                    FolderItem(folder)
                }
            }
        }
    }
}


// ✅ **Bottom Navigation Bar**
@Composable
fun BottomNavBar(navController: NavController, currentScreen: String, onPlusClick: (() -> Unit)? = null) {
    BottomAppBar(
        containerColor = DarkieViolet
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(R.drawable.ic_home, "Home", currentScreen == "home") { navController.navigate("home") }
            BottomNavItem(R.drawable.ic_folder, "Library", currentScreen == "library") { navController.navigate("library") }

            // Center Plus Button
            if (onPlusClick != null) {
                IconButton(onClick = onPlusClick) {
                    Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add", tint = Color.White)
                }
            }

            BottomNavItem(R.drawable.ic_barchart, "Stats", currentScreen == "stats") { navController.navigate("stats") }
            BottomNavItem(R.drawable.ic_profile, "Profile", currentScreen == "profile") { navController.navigate("profile") }
        }
    }
}


// ✅ **Folder Item**
@Composable
fun FolderItem(folder: Folder) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkieViolet),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { /* TODO: Open folder */ }
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = "Folder",
                tint = RoyalBlue,
                modifier = Modifier.size(32.dp)
            )

            Text(text = folder.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
            Text(text = "${folder.itemsCount} items", fontSize = 14.sp, color = White.copy(alpha = 0.7f))
            Text(text = folder.lastModified, fontSize = 12.sp, color = White.copy(alpha = 0.5f))
        }
    }
}
