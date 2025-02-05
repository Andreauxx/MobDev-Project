@file:OptIn(ExperimentalMaterial3Api::class)

package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.ui.theme.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign


@Composable
fun CreateSetScreen(navController: NavController) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var terms = remember { mutableStateListOf<Pair<String, String>>() }
    var newTerm by remember { mutableStateOf(TextFieldValue("")) }
    var newDefinition by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().height(70.dp),
            title = { Text("Create Set", color = White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() },
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                }
            },
            actions = {
                IconButton(
                    onClick = { /* Help/Info */ },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkMediumViolet),

        )


        Spacer(modifier = Modifier.height(30.dp))


        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp)),
            value = title,
            onValueChange = { title = it },
            label = { Text("Title", color = White, fontFamily = WorkSans) },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = "Title", tint = White)
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = DarkMediumViolet,
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Terms & Definitions", color = White, fontSize = 20.sp,fontFamily = WorkSans)

        Spacer(modifier = Modifier.height(24.dp))

        terms.forEachIndexed { index, (term, definition) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkMediumViolet)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Term: $term", color = White, fontSize = 18.sp)
                    Text("Definition: $definition", color = White, fontSize = 16.sp)
                }
            }
        }

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp)),
            value = newTerm,
            onValueChange = { newTerm = it },
            label = { Text("New Term", color = White, fontFamily = WorkSans) },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Book, contentDescription = "New Term", tint = White)
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = DarkMediumViolet,
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp)),
            value = newDefinition,
            onValueChange = { newDefinition = it },
            label = { Text("New Definition", color = White, fontFamily = WorkSans) },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Description, contentDescription = "New Definition", tint = White)
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = DarkMediumViolet,
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(86.dp))

        Button(
            onClick = {
                if (newTerm.text.isNotEmpty() && newDefinition.text.isNotEmpty()) {
                    terms.add(newTerm.text to newDefinition.text)
                    newTerm = TextFieldValue("")
                    newDefinition = TextFieldValue("")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MediumViolet),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Term", color = White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Save the flashcard set logic */ },
            colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.SaveAlt, contentDescription = "Save", tint = DarkViolet)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Set", color = DarkViolet)
        }
    }
}
