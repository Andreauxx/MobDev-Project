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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.DarkieViolet
import com.quadrants.memorix.ui.theme.MediumViolet
import com.quadrants.memorix.ui.theme.WorkSans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val systemUiController = rememberSystemUiController()

    // **Set Status Bar & Navigation Bar Colors**
    SideEffect {
        systemUiController.setStatusBarColor(DarkViolet, darkIcons = false)
        systemUiController.setNavigationBarColor(DarkViolet, darkIcons = false) // ✅ Match the background
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkViolet)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, bottom = 56.dp), // ✅ Added top padding
            verticalArrangement = Arrangement.Top
        ) {
            SearchBar()
            QuizQuestionSection()
            Spacer(modifier = Modifier.weight(1f))
        }

        BottomNavBar(
            navController,
            currentScreen = "home",
            onPlusClick = { showBottomSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MediumViolet
            ) {
                BottomSheetContent { showBottomSheet = false }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    TextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = {
            Text(
                "Search flashcards, quizzes, questions...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = WorkSans
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(14.dp)

            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.owl_icon),
                contentDescription = "Logo",
                tint = Color.White
            )
        },
        textStyle = TextStyle(color = Color.White), // ✅ Corrected Here!
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MediumViolet,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(25.dp))
    )

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

fun BottomNavBar(navController: NavController, currentScreen: String, onPlusClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    BottomAppBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = DarkieViolet,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                iconId = R.drawable.ic_home,
                label = "Home",
                isSelected = currentScreen == "home"
            ) { navController.navigate("home") }

            BottomNavItem(
                iconId = R.drawable.ic_folder,
                label = "Library",
                isSelected = currentScreen == "library"
            ) { navController.navigate("folders") }

            if (onPlusClick != null) {
                Column(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { onPlusClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            BottomNavItem(
                iconId = R.drawable.ic_barchart,
                label = "Stats",
                isSelected = currentScreen == "stats"
            ) { navController.navigate("stats") }

            BottomNavItem(
                iconId = R.drawable.ic_profile,
                label = "Profile",
                isSelected = currentScreen == "profile"
            ) { navController.navigate("profile") }
        }
    }
}


@Composable
fun BottomNavItem(iconId: Int, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = label,
            tint = if (isSelected) Color(0xFFFFD700) else Color.White,
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