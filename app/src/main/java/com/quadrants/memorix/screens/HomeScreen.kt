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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quadrants.memorix.R
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A001F)), // Dark background
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Search Bar
        SearchBar()

        // Quiz Question Section
        QuizQuestionSection()

        // Bottom Navigation Bar
        BottomNavBar()
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
            painter = painterResource(id = R.drawable.ic_folder), // Use folder as search icon placeholder
            contentDescription = "Search",
            tint = Color.White
        )

        Text(
            text = "Flashcard sets, quizzes, questions",
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        Icon(
            painter = painterResource(id = R.drawable.owl_icon), // Owl logo
            contentDescription = "Logo",
            tint = Color.White
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
            text = "Which of the following is used to store\nkey-value pairs in Python?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Answer Buttons
        AnswerButton(text = "List", color = Color.Red) {}
        AnswerButton(text = "Dictionary", color = Color.Green) {} // Correct answer
        AnswerButton(text = "Tuple", color = Color(0xFF673AB7)) {} // Purple
        AnswerButton(text = "Set", color = Color.Yellow) {}
    }
}

@Composable
fun AnswerButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}

@Composable
fun BottomNavBar() {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Black.copy(alpha = 0.8f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(iconId = R.drawable.ic_home, label = "Home")
            BottomNavItem(iconId = R.drawable.ic_add, label = "Add")
            BottomNavItem(iconId = R.drawable.ic_barchart, label = "Stats")
            BottomNavItem(iconId = R.drawable.ic_profile, label = "Profile")
        }
    }
}

@Composable
fun BottomNavItem(iconId: Int, label: String) {
    Column(
        modifier = Modifier
            .clickable { /* TODO: Handle navigation */ }
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


