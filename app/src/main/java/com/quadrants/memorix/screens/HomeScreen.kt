package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.MediumViolet
import com.quadrants.memorix.ui.theme.WorkSans
import com.quadrants.memorix.QuizQuestion
import com.quadrants.memorix.ui.theme.DarkieViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.VerticalPager
import com.quadrants.memorix.MainActivity
import com.quadrants.memorix.ui.theme.DarkMediumViolet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, activity: MainActivity) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val firestore = FirebaseFirestore.getInstance()

    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var streak by remember { mutableStateOf(0) }

    // Fetch Questions from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("quiz_questions")
            .get()
            .addOnSuccessListener { result ->
                questions = result.documents.mapNotNull { doc ->
                    doc.toObject(QuizQuestion::class.java)
                }
            }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkViolet)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 56.dp),
            verticalArrangement = Arrangement.Top
        ) {
            SearchBar()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "🔥 Streak: $streak",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Yellow,
                fontFamily = WorkSans,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            )

            val pagerState = rememberPagerState { questions.size }
            val coroutineScope = rememberCoroutineScope()

            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp) // ✅ Adds padding between search bar & quiz content
            ) { page ->
                QuizQuestionSection(
                    quizQuestion = questions[page],
                    onCorrectAnswer = {
                        streak++
                        coroutineScope.launch { moveToNextQuestion(pagerState, page, questions.size) } // ✅ Fix: Pass questions.size
                    },
                    onWrongAnswer = {
                        streak = 0
                        coroutineScope.launch { moveToNextQuestion(pagerState, page, questions.size) } // ✅ Fix: Pass questions.size
                    }
                )
            }

        }

        // Bottom Navigation Bar with Modal

        BottomNavBar(
            navController = navController,
            currentScreen = "home",
            onPlusClick = { showBottomSheet = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }
            ) {
                BottomSheetContent(navController, activity) { showBottomSheet = false }
            }
        }
    }
}

// ✅ Improved Search Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf("") }

    TextField(

        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("Search flashcards, quizzes, questions...", color = Color.White.copy(alpha = 0.7f)) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.owl_icon),
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = DarkMediumViolet,
            focusedIndicatorColor = Color.Transparent, // ✅ Removes white line
            unfocusedIndicatorColor = Color.Transparent // ✅ Removes white line
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 36.dp) // ✅ Extra top spacing
            .clip(RoundedCornerShape(25.dp))
    )
}

// ✅ Improved Quiz Question UI
@Composable
fun QuizQuestionSection(quizQuestion: QuizQuestion, onCorrectAnswer: () -> Unit, onWrongAnswer: () -> Unit) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var correctAnswerRevealed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = quizQuestion.question,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            quizQuestion.answers.chunked(2).forEach { rowAnswers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowAnswers.forEach { answer ->
                        val isCorrect = answer == quizQuestion.correctAnswer
                        val backgroundColor = when {
                            correctAnswerRevealed && isCorrect -> Color.Green
                            selectedAnswer == answer && !isCorrect -> Color.Red
                            else -> Color.Gray
                        }

                        AnswerButton(text = answer, color = backgroundColor) {
                            if (selectedAnswer == null) {
                                selectedAnswer = answer
                                correctAnswerRevealed = true

                                if (isCorrect) onCorrectAnswer() else onWrongAnswer()
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ Function to Move to Next Question
suspend fun moveToNextQuestion(pagerState: PagerState, currentPage: Int, totalQuestions: Int) {
    delay(1000)
    if (currentPage < totalQuestions - 1) { // ✅ Fix: Use totalQuestions
        pagerState.animateScrollToPage(currentPage + 1)
    }
}


// ✅ Styled Answer Button
@Composable
fun AnswerButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(8.dp).size(150.dp, 80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}


// Bottom Navigation Bar
@Composable
fun BottomNavBar(navController: NavController, currentScreen: String, onPlusClick: () -> Unit, modifier: Modifier = Modifier) {
    BottomAppBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = DarkieViolet
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            BottomNavItem(R.drawable.ic_home, "Home", currentScreen == "home") { navController.navigate("home") }
            BottomNavItem(R.drawable.ic_folder, "Library", currentScreen == "library") { navController.navigate("folders") }

            // Plus Button for Modal
            IconButton(onClick = onPlusClick) {
                Icon(painterResource(id = R.drawable.ic_add), contentDescription = "Add", tint = Color.White)
            }

            BottomNavItem(R.drawable.ic_barchart, "Stats", currentScreen == "stats") { navController.navigate("stats") }
            BottomNavItem(R.drawable.ic_profile, "Profile", currentScreen == "profile") { navController.navigate("profile") }
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
fun BottomSheetContent(navController: NavController, activity: MainActivity, onDismiss: () -> Unit) {
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
        BottomSheetItem(
            iconId = R.drawable.ic_flashcard,
            text = "Flashcard Set",
            onClick = {
                navController.navigate("flashcard")
                onDismiss()
            }
        )
        BottomSheetItem(
            iconId = R.drawable.ic_quiz,
            text = "Create Quiz",
            onClick = {
                navController.navigate("quiz")
                onDismiss()
            }
        )
        BottomSheetItem(
            iconId = R.drawable.ic_class,
            text = "Create a Class",
            onClick = {
                navController.navigate("classScreen")
                onDismiss()
            }
        )
        BottomSheetItem(
            iconId = R.drawable.ic_upload,
            text = "Upload Image/PDF",
            onClick = {
                activity.openFileOrCamera() // Call function from MainActivity
                onDismiss()
            }
        )
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