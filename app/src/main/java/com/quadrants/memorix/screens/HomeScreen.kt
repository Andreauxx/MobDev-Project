package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import com.quadrants.memorix.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var showBottomSheet by remember { mutableStateOf(false) } // ✅ Track bottom sheet visibility

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A001F)) // Dark background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp), // ✅ Ensures content doesn't overlap bottom nav
            verticalArrangement = Arrangement.Top
        ) {
            SearchBar()
            QuizQuestionSection()

            // ✅ Spacer forces BottomNavBar to stay at the bottom
            Spacer(modifier = Modifier.weight(1f))
        }

        // ✅ Bottom Navigation Bar - Now correctly positioned at the bottom
        BottomNavBar(
            navController,
            onPlusClick = { showBottomSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter) // ✅ Forces position at bottom
        )

        // ✅ Bottom Sheet (Appears over BottomNavBar)
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }, // Close when tapped outside
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), // ✅ Correct parameter
                containerColor = Color(0xFF6A4C93) // Dark Purple background
            ) {
                BottomSheetContent { showBottomSheet = false }
            }
        }
    }
}

@Composable
fun SearchBar() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFF693D7D)) // Dark Purple
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Folder",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = "Flashcard sets, quizzes, questions",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Start,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        Icon(
            painter = painterResource(id = R.drawable.owl_icon),
            contentDescription = "Logo",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun QuizQuestionSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Which of the following is used to store key-value pairs in Python?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Answer Buttons in 2x2 Grid
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnswerButton(text = "List", color = Color.Red, modifier = Modifier.weight(1f)) {}
                AnswerButton(text = "Dictionary", color = Color.Green, modifier = Modifier.weight(1f)) {} // Correct answer
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnswerButton(text = "Tuple", color = Color(0xFF673AB7), modifier = Modifier.weight(1f)) {} // Purple
                AnswerButton(text = "Set", color = Color.Yellow, modifier = Modifier.weight(1f)) {}
            }
        }
    }
}

@Composable
fun AnswerButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
            .height(125.dp), // Fixed height for uniformity
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun BottomNavBar(navController: NavController, onPlusClick: () -> Unit, modifier: Modifier = Modifier) {
    BottomAppBar(
        modifier = modifier.fillMaxWidth(), // ✅ Modifier now correctly applied
        containerColor = Color(0xFF1A001F).copy(alpha = 0.95f), // Dark purple fade
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(iconId = R.drawable.ic_home, label = "Home") { navController.navigate("home") }
            BottomNavItem(iconId = R.drawable.ic_folder, label = "Library") { navController.navigate("library") }

            // ✅ Plus Icon - Clickable to open Bottom Sheet
            Column(
                modifier = Modifier
                    .size(56.dp) // Larger size
                    .clickable { onPlusClick() }, // Open Bottom Sheet
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp) // Bigger icon
                )
            }

            BottomNavItem(iconId = R.drawable.ic_barchart, label = "Stats") { navController.navigate("stats") }
            BottomNavItem(iconId = R.drawable.ic_profile, label = "Profile") { navController.navigate("profile") }
        }
    }
}


@Composable
fun BottomNavItem(iconId: Int, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}


@Composable
fun BottomSheetContent(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(50.dp)
                .height(6.dp)
                .background(Color.Gray, shape = RoundedCornerShape(50))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Action Buttons in Bottom Sheet
        BottomSheetItem(iconId = R.drawable.ic_flashcard, text = "Flashcard Set", onClick = onDismiss)
        BottomSheetItem(iconId = R.drawable.ic_quiz, text = "Create Quiz", onClick = onDismiss)
        BottomSheetItem(iconId = R.drawable.ic_class, text = "Create a Class", onClick = onDismiss)
        BottomSheetItem(iconId = R.drawable.ic_upload, text = "Upload Image/PDF", onClick = onDismiss)
    }
}

@Composable
fun BottomSheetItem(iconId: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF4C2C70)) // Darker Purple
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}