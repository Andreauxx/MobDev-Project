package com.quadrants.memorix.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.Flashcard
import com.quadrants.memorix.FlashcardSet
import com.quadrants.memorix.QuizQuestion
import com.quadrants.memorix.ui.theme.*

@Composable
fun ViewQuestionScreen(navController: NavController, question: String, items: List<Map<String, Any>>, isCreator: Boolean) {
    val item = items.find { it["question"]?.toString() == question }

    Column(modifier = Modifier.fillMaxSize().background(DarkViolet).padding(16.dp)) {
        item?.let {
            val answers = it["answers"] as? List<*> ?: emptyList<String>()
            val correctAnswerIndex = (it["correctAnswerIndex"] as? Double)?.toInt() ?: -1
            val explanation = it["explanation"]?.toString() ?: ""

            Text(text = question, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)

            answers.forEachIndexed { index, answer ->
                Text(
                    text = "${index + 1}. ${answer.toString()}",
                    fontSize = 18.sp,
                    color = if (index == correctAnswerIndex) Color.Green else White
                )
            }

            Text(
                text = "Explanation: $explanation",
                fontSize = 16.sp,
                color = White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            if (isCreator) {
                Button(
                    onClick = { navController.navigate("editQuestion/${Uri.encode(question)}") },
                    colors = ButtonDefaults.buttonColors(containerColor = MediumViolet),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Edit", color = DarkViolet)
                }
            }
        } ?: Text("Question not found", color = White)
    }
}
