@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*

@Composable
fun CreateQuizScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    var title by remember { mutableStateOf(TextFieldValue("") )}
    var category by remember { mutableStateOf(TextFieldValue("") )}
    var question by remember { mutableStateOf(TextFieldValue("") )}
    var timeLimit by remember { mutableStateOf(TextFieldValue("") )}
    var answers = remember { mutableStateListOf(TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), TextFieldValue("")) }
    var correctAnswerIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
            }
            Text(
                text = "Create Quiz",
                fontSize = 28.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title", color = White) }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = GoldenYellow, unfocusedBorderColor = White))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category", color = White) }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = GoldenYellow, unfocusedBorderColor = White))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Question", color = White) }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = GoldenYellow, unfocusedBorderColor = White))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = timeLimit,
            onValueChange = {
                if (it.text.all { char -> char.isDigit() }) timeLimit = it
            },
            label = { Text("Time Limit (seconds)", color = White) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = GoldenYellow, unfocusedBorderColor = White)
        )

        answers.forEachIndexed { index, answer ->
            OutlinedTextField(value = answer, onValueChange = { answers[index] = it }, label = { Text("Option ${index + 1}", color = White) }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = GoldenYellow, unfocusedBorderColor = White))
        }

        Text("Select Correct Option", color = White, fontSize = 16.sp, fontFamily = WorkSans, modifier = Modifier.padding(top = 8.dp))
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
            answers.forEachIndexed { index, _ ->
                RadioButton(selected = correctAnswerIndex == index, onClick = { correctAnswerIndex = index }, colors = RadioButtonDefaults.colors(selectedColor = GoldenYellow))
            }
        }

        Button(onClick = {
            if (title.text.isNotEmpty() && category.text.isNotEmpty() && question.text.isNotEmpty() && timeLimit.text.isNotEmpty() && answers.all { it.text.isNotEmpty() }) {
                val quizData = hashMapOf(
                    "title" to title.text,
                    "category" to category.text,
                    "question" to question.text,
                    "timeLimit" to timeLimit.text.toInt(),
                    "answers" to answers.map { it.text },
                    "correctAnswerIndex" to correctAnswerIndex
                )
                firestore.collection("quiz_questions").add(quizData)
                    .addOnSuccessListener { navController.popBackStack() }
            }
        }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow)) {
            Text("Save Quiz", color = DarkViolet, fontFamily = WorkSans)
        }
    }
}