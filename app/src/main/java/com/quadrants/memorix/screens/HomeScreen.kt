package com.quadrants.memorix.screens

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.MainActivity
import com.quadrants.memorix.QuizQuestion
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, activity: MainActivity) {
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid
    var showBottomSheet by remember { mutableStateOf(false) }

    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var streak by remember { mutableStateOf(0) }

    var searchText by remember { mutableStateOf("") }

    var timerSeconds by remember { mutableStateOf(30) }
    var timerRunning by remember { mutableStateOf(false) }
    var timeUp by remember { mutableStateOf(false) }


    // ✅ Utility Function to Get Current Date in Format yyyy-MM-dd
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }


    LaunchedEffect(Unit) {
        if (userId != null) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                val preferences = document.get("user_preferences") as? Map<*, *> ?: mapOf(
                    "selectedFolders" to listOf("All"),
                    "selectedCategories" to listOf("All")
                )

                val selectedFolders = preferences["selectedFolders"] as? List<String> ?: listOf("All")
                val selectedCategories = preferences["selectedCategories"] as? List<String> ?: listOf("All")

                firestore.collection("quiz_questions")
                    .get()
                    .addOnSuccessListener { result ->
                        questions = result.documents.mapNotNull { doc ->
                            val folderTitle = doc.getString("title") ?: ""
                            val category = doc.getString("category") ?: ""
                            val question = doc.getString("question") ?: ""

                            if (("All" in selectedFolders || folderTitle in selectedFolders) ||
                                ("All" in selectedCategories || category in selectedCategories)
                            ) {
                                QuizQuestion(
                                    question = question,
                                    answers = doc.get("answers") as? List<String> ?: emptyList(),
                                    correctAnswerIndex = doc.getLong("correctAnswerIndex")?.toInt() ?: 0,
                                    explanation = doc.getString("explanation") ?: "",
                                    timeLimit = doc.getLong("timeLimit")?.toInt() ?: 30
                                )
                            } else null
                        }
                    }
            }
        }
    }

    val pagerState = rememberPagerState { questions.size }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(timerRunning) {
        while (timerRunning && timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        }
        if (timerSeconds == 0) {
            timeUp = true
            timerRunning = false
            coroutineScope.launch { moveToNextQuestion(pagerState, pagerState.currentPage, questions.size) }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentScreen = "home",
                onPlusClick = { showBottomSheet = true } // ✅ Fix: This will show the bottom sheet
            )
        }

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 0.dp),
                verticalArrangement = Arrangement.Top
            ) {
                SearchBar()
                Spacer(modifier = Modifier.height(6.dp)) //
                Text(
                    text = "🔥 Streak: $streak",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Yellow,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(4.dp)
                )

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 6.dp)
                ) { page ->
                    QuizQuestionSection(
                        quizQuestion = questions[page],
                        userId = userId ?: "",  // ✅ Pass userId here
                        firestore = firestore,  // ✅ Pass Firestore instance
                        coroutineScope = coroutineScope,  // ✅ Pass coroutine scope
                        onStartTimer = { time ->
                            timerSeconds = time
                            timerRunning = true
                            timeUp = false
                        },
                        onCorrectAnswer = {
                            streak++
                            timerRunning = false
                            coroutineScope.launch { moveToNextQuestion(pagerState, page, questions.size) }
                        },
                        onWrongAnswer = {
                            streak = 0
                            timerRunning = false
                            coroutineScope.launch { moveToNextQuestion(pagerState, page, questions.size) }
                        },
                        timerSeconds = timerSeconds,
                        timeUp = timeUp
                    )
                }
            }
        }
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


@Composable
fun QuizQuestionSection(
    userId: String,  // ✅ Added userId
    firestore: FirebaseFirestore,  // ✅ Added Firestore instance
    coroutineScope: CoroutineScope,  // ✅ Added Coroutine Scope
    quizQuestion: QuizQuestion,
    onStartTimer: (Int) -> Unit,
    onCorrectAnswer: () -> Unit,
    onWrongAnswer: () -> Unit,
    timerSeconds: Int,
    timeUp: Boolean
) {
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var correctAnswerRevealed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (quizQuestion.timeLimit > 0) {
            onStartTimer(quizQuestion.timeLimit)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
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

            Text(
                text = if (timeUp) "⏱️ Time's Up!" else "⏱️ $timerSeconds s",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (timeUp) Color.Red else Color.Yellow,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            quizQuestion.answers.chunked(2).forEach { rowAnswers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowAnswers.forEach { answer ->
                        val index = quizQuestion.answers.indexOf(answer)
                        val isCorrect = index == quizQuestion.correctAnswerIndex
                        val backgroundColor = when {
                            selectedAnswerIndex == index && isCorrect -> Correct
                            selectedAnswerIndex == index && !isCorrect -> Color.Red
                            correctAnswerRevealed && isCorrect -> Correct
                            else -> Color(0xFF4A326F)
                        }

                        AnswerButton(text = answer, color = backgroundColor) {
                            if (selectedAnswerIndex == null) {
                                selectedAnswerIndex = index
                                correctAnswerRevealed = true

                                if (isCorrect) {
                                    updateItemsReviewed(firestore, userId, coroutineScope)  // ✅ Removed ?: ""
                                    playCorrectSound(context)
                                    vibratePhone(context)
                                    onCorrectAnswer()
                                } else {
                                    playWrongSound(context)
                                    vibratePhone(context)
                                    onWrongAnswer()
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (correctAnswerRevealed && quizQuestion.explanation.isNotEmpty()) {
                Dialog(onDismissRequest = {}) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B5C))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (selectedAnswerIndex == quizQuestion.correctAnswerIndex)
                                    "✅ You're Correct!"
                                else
                                    "❌ Wrong! The correct answer is: ${quizQuestion.answers[quizQuestion.correctAnswerIndex]}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = WorkSans,
                                color = if (selectedAnswerIndex == quizQuestion.correctAnswerIndex) Correct else Color.Red
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = quizQuestion.explanation,
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


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

suspend fun moveToNextQuestion(pagerState: PagerState, currentPage: Int, totalQuestions: Int) {
    delay(2500)
    if (currentPage < totalQuestions - 1) {
        pagerState.animateScrollToPage(currentPage + 1)
    }
}

fun playCorrectSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.correct)
    mediaPlayer.start()
    mediaPlayer.setOnCompletionListener { it.release() }
}

fun playWrongSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.wrong)
    mediaPlayer.start()
    mediaPlayer.setOnCompletionListener { it.release() }
}

fun vibratePhone(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}

// ✅ Function to Update Items Reviewed in Firestore
fun updateItemsReviewed(firestore: FirebaseFirestore, userId: String, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        try {
            val today = getCurrentDate()
            val userRef = firestore.collection("users").document(userId)
                .collection("dailyStats").document(today)

            // ✅ Use Firestore Increment
            userRef.update("itemsReviewed", com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener {
                    println("✅ Items reviewed updated successfully.")
                }
                .addOnFailureListener { e ->
                    println("❌ Error updating items reviewed: ${e.message}")
                }
        } catch (e: Exception) {
            println("❌ Error updating items reviewed: ${e.message}")
        }
    }
}
