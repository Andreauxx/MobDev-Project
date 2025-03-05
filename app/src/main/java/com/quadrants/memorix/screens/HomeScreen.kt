package com.quadrants.memorix.screens

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.quadrants.memorix.utils.getCurrentDate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    activity: MainActivity,
    quizQuestions: List<QuizQuestion> = emptyList(),
    onPlusClick: () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(color = DarkViolet, darkIcons = false)
    }

    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid
    var showBottomSheet by remember { mutableStateOf(false) }

    var shouldRefresh by rememberSaveable { mutableStateOf(false) }
    var flashcards by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val gson = remember { Gson() }
    var quizData by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }

    // ✅ Retrieve Quiz Data from Navigation
    val quizJson = navController.currentBackStackEntry?.arguments?.getString("quizJson")

    LaunchedEffect(quizJson) {
        if (!quizJson.isNullOrEmpty()) {
            try {
                val decodedJson = String(Base64.decode(quizJson, Base64.DEFAULT)) // ✅ Decode Base64
                quizData = gson.fromJson(decodedJson, Array<QuizQuestion>::class.java).toList()
                println("✅ Loaded Quiz Data: ${quizData.size} questions")

                isLoading = false // ✅ Ensure loading state is updated

            } catch (e: Exception) {
                println("❌ Error Decoding Quiz JSON: ${e.message}")
                isLoading = false // ✅ Prevent infinite loading if an error occurs
            }
        }
    }


    // Add shimmer effect animation
    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Adjust as needed for effect
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // ✅ Fetch flashcards if no quiz is loaded
    LaunchedEffect(userId, shouldRefresh) {
        if (userId != null && quizData.isEmpty()) { // ✅ Fetch only if quizData is empty
            isLoading = true
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val preferences = document.data?.get("user_preferences") as? Map<String, Any> ?: emptyMap()
                    val selectedSetsTermDefinition = preferences["selectedTermDefinitionSets"] as? List<String> ?: emptyList()
                    val selectedSetsMultipleChoice = preferences["selectedMultipleChoiceSets"] as? List<String> ?: emptyList()

                    firestore.collection("flashcard_sets").get()
                        .addOnSuccessListener { result ->
                            flashcards = result.documents.flatMap { doc ->
                                val title = doc.getString("title") ?: ""
                                val cards = doc.get("cards") as? List<Map<String, Any>> ?: emptyList()

                                if (selectedSetsTermDefinition.contains(title) || selectedSetsMultipleChoice.contains(title)) {
                                    cards.mapNotNull {
                                        val type = it["type"] as? String
                                        when (type) {
                                            "multiple-choice" -> QuizQuestion(
                                                question = it["question"] as? String ?: "",
                                                answers = (it["answers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                                correctAnswerIndex = (it["correctAnswerIndex"] as? Long)?.toInt() ?: 0,
                                                explanation = it["explanation"] as? String ?: "",
                                                timeLimit = 0
                                            )
                                            "term-definition" -> {
                                                val term = it["term"] as? String ?: ""
                                                val definition = it["definition"] as? String ?: ""
                                                if (term.isNotEmpty() && definition.isNotEmpty()) {
                                                    QuizQuestion(
                                                        question = term,
                                                        answers = listOf(definition),
                                                        correctAnswerIndex = 0,
                                                        explanation = it["explanation"] as? String ?: "",
                                                        timeLimit = 0
                                                    )
                                                } else null
                                            }
                                            else -> null
                                        }
                                    }
                                } else emptyList()
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                }
                .addOnFailureListener { isLoading = false }
        }
    }


    val pagerState = rememberPagerState { if (quizData.isNotEmpty()) quizData.size else flashcards.size }
    val coroutineScope = rememberCoroutineScope()

    // ✅ Display Quiz Questions if Available
    val isQuizMode = quizData.isNotEmpty()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentScreen = "home",
                onPlusClick = { showBottomSheet = true }
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                SearchBar()
                Spacer(modifier = Modifier.height(12.dp))
                StreakCounter(5) // Streak placeholder
                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    repeat(2) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF4B2A72).copy(alpha = 0.5f),
                                                Color(0xFF5C3998).copy(alpha = 0.7f),
                                                Color(0xFF4B2A72).copy(alpha = 0.5f)
                                            ),
                                            start = Offset(0f, 0f),
                                            end = Offset(translateAnim, 0f)
                                        )
                                    )
                            )
                        }
                    }
                } else {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        if (quizData.isNotEmpty()) {  // ✅ Ensure we are displaying quiz questions
                            val quizQuestion = quizData[page]
                            MultipleChoiceCard(quizQuestion, pagerState, coroutineScope, {}, {})
                        } else {
                            val flashcard = flashcards.getOrNull(page)
                            if (flashcard != null) {
                                if (flashcard.answers.size == 1) {
                                    AnimatedTermDefinitionCard(flashcard, pagerState, coroutineScope)
                                } else {
                                    MultipleChoiceCard(flashcard, pagerState, coroutineScope, {}, {})
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))
                    PaginationDots(pagerState, coroutineScope)
                }
            }

            if (showBottomSheet) {
                ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
                    BottomSheetContent(navController, activity) {
                        showBottomSheet = false
                    }
                }
            }
        }
    }
}




@Composable
fun StreakCounter(streak: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkieViolet)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔥",
            fontSize = 28.sp,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Streak: $streak",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GoldenYellow
        )
    }
}

@Composable
fun PaginationDots(pagerState: PagerState, coroutineScope: CoroutineScope) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(minOf(pagerState.pageCount, 5)) { iteration ->
            val pageIndex = when {
                pagerState.pageCount <= 5 -> iteration
                pagerState.currentPage <= 1 -> iteration
                pagerState.currentPage >= pagerState.pageCount - 2 ->
                    pagerState.pageCount - 5 + iteration
                else -> pagerState.currentPage - 2 + iteration
            }

            val isSelected = pagerState.currentPage == pageIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) GoldenYellow else Color.White.copy(alpha = 0.3f)
                    )
                    .clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pageIndex)
                        }
                    }
            )
        }
    }
}

@Composable
fun AnimatedTermDefinitionCard(
    flashcard: QuizQuestion,
    pagerState: PagerState,
    coroutineScope: CoroutineScope
) {
    var flipped by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = flipped, label = "Card Flip")

    val rotationY by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "Rotation Y"
    ) { if (it) 180f else 0f }

    val scale by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "Scale"
    ) { if (it) 0.98f else 1f }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid // ✅ Retrieve userId here

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(vertical = 12.dp)
            .clickable {
                flipped = !flipped
                if (flipped) {
                    vibratePhone(context)
                    if (userId != null) {
                        updateFlashcardsReviewed(userId)
                    }
                    coroutineScope.launch {
                        delay(2000)
                        moveToNextQuestion(pagerState, pagerState.currentPage, pagerState.pageCount)
                    }
                }
            }
            .graphicsLayer(
                rotationY = rotationY,
                scaleX = scale,
                scaleY = scale,
                cameraDistance = 12 * context.resources.displayMetrics.density
            )
            .shadow(20.dp, RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (flipped)
                    GoldenYellow
                else
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5C3998),
                            Color(0xFF4B2A72)
                        )
                    ).let { Color(0xFF4B2A72) }
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!flipped) {
                            Modifier.background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF5C3998),
                                        Color(0xFF4B2A72)
                                    )
                                )
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .graphicsLayer(rotationY = if (flipped) 180f else 0f)
                ) {
                    Text(
                        text = if (!flipped) "Term" else "Definition",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!flipped) Color.White.copy(alpha = 0.6f) else DarkViolet.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = if (!flipped) flashcard.question else flashcard.answers[0],
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!flipped) Color.White else DarkViolet,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    if (!flipped) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Tap to flip",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    else if (flashcard.explanation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Explanation:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkViolet
                        )
                        Text(
                            text = flashcard.explanation,
                            fontSize = 16.sp,
                            color = DarkViolet,
                            textAlign = TextAlign.Center
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun MultipleChoiceCard(
    flashcard: QuizQuestion,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var showExplanation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkieViolet
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                flashcard.question,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            flashcard.answers.chunked(2).forEach { rowAnswers ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowAnswers.forEach { answer ->
                        val index = flashcard.answers.indexOf(answer)
                        val isCorrect = index == flashcard.correctAnswerIndex
                        Button(
                            onClick = {
                                selectedAnswerIndex = index
                                showExplanation = true
                                if (isCorrect) {
                                    playCorrectSound(context)
                                    vibratePhone(context)
                                    onCorrect()

                                } else {
                                    playWrongSound(context)
                                    vibratePhone(context)
                                    onWrong()
                                }

                                coroutineScope.launch {
                                    moveToNextQuestion(pagerState, pagerState.currentPage, pagerState.pageCount)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .height(90.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    selectedAnswerIndex == index && isCorrect -> Correct
                                    selectedAnswerIndex == index && !isCorrect -> Color.Red
                                    else -> Color(0xFF5C3998)
                                }
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Text(
                                answer,
                                fontSize = 18.sp,
                                color = White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExplanation && flashcard.explanation.isNotEmpty()) {
        Dialog(onDismissRequest = { showExplanation = false }) {
            LaunchedEffect(Unit) { // ✅ Automatically dismiss after 4 seconds
                delay(4000)
                showExplanation = false
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(16.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkViolet // Ensure it's fully visible
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp) // Add elevation to pop out
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .background(DarkViolet), // Explicit background fix
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(500))
                    ) {
                        Icon(
                            imageVector = if (selectedAnswerIndex == flashcard.correctAnswerIndex)
                                Icons.Filled.CheckCircle
                            else
                                Icons.Filled.Cancel,
                            contentDescription = "Result",
                            tint = if (selectedAnswerIndex == flashcard.correctAnswerIndex) Correct else Color.Red,
                            modifier = Modifier
                                .size(48.dp) // Increase size for visibility
                                .graphicsLayer(scaleX = 1.2f, scaleY = 1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (selectedAnswerIndex == flashcard.correctAnswerIndex)
                            "You're Correct!"
                        else
                            "The correct answer is:",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedAnswerIndex == flashcard.correctAnswerIndex) Correct else Color.Red
                    )

                    if (selectedAnswerIndex != flashcard.correctAnswerIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = flashcard.answers[flashcard.correctAnswerIndex],
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Explanation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = flashcard.explanation,
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showExplanation = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldenYellow
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Continue",
                            color = DarkViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

    }
}

// Improved moveToNextQuestion with smoother transition
suspend fun moveToNextQuestion(pagerState: PagerState, currentPage: Int, totalQuestions: Int) {
    delay(3000)
    if (currentPage < totalQuestions - 1) {
        pagerState.animateScrollToPage(
            page = currentPage + 1,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }
}

// Improved Search Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    var searchText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkMediumViolet
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = {
                    Text(
                        "Search flashcards...",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = GoldenYellow,

                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoldenYellow.copy(alpha = 0.2f))
                    .border(1.dp, GoldenYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.owl_icon),
                    contentDescription = "Logo",
                    tint = GoldenYellow,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Functions for sound and vibration
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

// Function to Update Items Reviewed in Firestore
fun updateItemsReviewed(firestore: FirebaseFirestore, userId: String, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        try {
            val today = getCurrentDate()
            val userRef = firestore.collection("users").document(userId)
                .collection("dailyStats").document(today)

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


fun updateFlashcardsReviewed(userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val currentDate = getCurrentDate()
    val userRef = firestore.collection("users").document(userId)

    val updates = mapOf(
        "dailyStats.$currentDate.flashcardsReviewed" to FieldValue.increment(1),
        "totalFlashcardsReviewed" to FieldValue.increment(1)
    )

    firestore.runTransaction { transaction ->
        transaction.update(userRef, updates)
    }.addOnSuccessListener {
        println("✅ Flashcards reviewed updated successfully.")
    }.addOnFailureListener { e ->
        println("❌ Error updating flashcards reviewed: ${e.message}")
    }
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
fun LoadingFlashcardsPlaceholder() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Column {
        repeat(2) { // Show 2 skeleton cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkieViolet)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4B2A72).copy(alpha = 0.3f),
                                    Color(0xFF5C3998).copy(alpha = 0.5f),
                                    Color(0xFF4B2A72).copy(alpha = 0.3f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(shimmerAnim, 0f)
                            )
                        )
                )
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


fun recordQuizScore(firestore: FirebaseFirestore, userId: String, score: Int) {
    val today = getCurrentDate()
    firestore.collection("users").document(userId)
        .collection("quizScores").document(today)
        .set(mapOf("score" to score))
        .addOnSuccessListener {
            println("✅ Quiz score recorded successfully.")
        }
        .addOnFailureListener { e ->
            println("❌ Error recording quiz score: ${e.message}")
        }
}