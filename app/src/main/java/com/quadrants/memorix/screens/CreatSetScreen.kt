@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.*
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateSetScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var title by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf(TextFieldValue("")) }
    var definition by remember { mutableStateOf(TextFieldValue("")) }
    var newTerm by remember { mutableStateOf(TextFieldValue("")) }
    var terms = remember { mutableStateListOf<Map<String, Any>>() }
    var answers = remember { mutableStateListOf(TextFieldValue(""), TextFieldValue(""), TextFieldValue(""), TextFieldValue("")) }
    var correctAnswerIndex by remember { mutableStateOf(0) }
    var isMultipleChoice by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet)
            .padding(16.dp)
            .clickable {
                focusManager.clearFocus()
                keyboardController?.hide()
            },
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
            }
            Text(
                text = "Create Flashcard Set",
                fontSize = 28.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title", color = White) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = GoldenYellow,
                unfocusedBorderColor = White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category", color = White) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = GoldenYellow,
                unfocusedBorderColor = White
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        ) {
            Text("Multiple Choice", color = White, fontSize = 16.sp, fontFamily = WorkSans)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isMultipleChoice,
                onCheckedChange = { isMultipleChoice = it },
                colors = SwitchDefaults.colors(checkedThumbColor = GoldenYellow)
            )
        }

        if (!isMultipleChoice) {
            OutlinedTextField(
                value = newTerm,
                onValueChange = { newTerm = it },
                label = { Text("Term", color = White) },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = GoldenYellow,
                    unfocusedBorderColor = White
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = definition,
                onValueChange = { definition = it },
                label = { Text("Definition", color = White) },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = GoldenYellow,
                    unfocusedBorderColor = White
                )
            )
        } else {
            answers.forEachIndexed { index, answer ->
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answers[index] = it },
                    label = { Text("Option ${index + 1}", color = White) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = GoldenYellow,
                        unfocusedBorderColor = White
                    )
                )
            }

            Text("Select Correct Option", color = White, fontSize = 16.sp, fontFamily = WorkSans, modifier = Modifier.padding(top = 8.dp))

            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                answers.forEachIndexed { index, _ ->
                    RadioButton(
                        selected = correctAnswerIndex == index,
                        onClick = { correctAnswerIndex = index },
                        colors = RadioButtonDefaults.colors(selectedColor = GoldenYellow)
                    )
                }
            }
        }

        Text(text = errorMessage, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))

        Button(
            onClick = {
                if ((isMultipleChoice && answers.all { it.text.isNotEmpty() }) || (!isMultipleChoice && newTerm.text.isNotEmpty() && definition.text.isNotEmpty())) {
                    val card = if (isMultipleChoice) {
                        mapOf(
                            "term" to newTerm.text,
                            "definition" to definition.text,
                            "answers" to answers.map { it.text },
                            "correctAnswerIndex" to correctAnswerIndex
                        )
                    } else {
                        mapOf(
                            "term" to newTerm.text,
                            "definition" to definition.text
                        )
                    }
                    terms.add(card)
                    newTerm = TextFieldValue("")
                    definition = TextFieldValue("")
                    answers.replaceAll { TextFieldValue("") }
                    errorMessage = ""
                } else {
                    errorMessage = "Please fill all fields"
                }
            },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MediumViolet)
        ) {
            Text("Add Item", color = White, fontFamily = WorkSans)
        }

        Button(
            onClick = {
                if (title.text.isNotEmpty() && category.text.isNotEmpty() && terms.isNotEmpty()) {
                    val setData = hashMapOf(
                        "title" to title.text,
                        "category" to category.text,
                        "type" to if (isMultipleChoice) "multiple-choice" else "term-definition",
                        "cards" to terms
                    )
                    firestore.collection("flashcard_sets")
                        .add(setData)
                        .addOnSuccessListener {
                            println("✅ Flashcard set saved successfully!")
                            navController.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            println("❌ Error saving flashcard set: ${e.message}")
                            errorMessage = "Error saving flashcard set: ${e.message}"
                        }
                } else {
                    errorMessage = "Please complete all fields before saving"
                }
            },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow)
        ) {
            Text("Save Set", color = DarkViolet, fontFamily = WorkSans)
        }
    }
}
