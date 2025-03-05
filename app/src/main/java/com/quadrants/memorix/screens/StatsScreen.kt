package com.quadrants.memorix.screens

import StudyTimeTracker
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.quadrants.memorix.utils.getCurrentDate



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController, activity: MainActivity, onPlusClick: () -> Unit, studyTimeTracker: StudyTimeTracker) {

    var showBottomSheet by remember { mutableStateOf(false) }

    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid

    var username by remember { mutableStateOf("User") }
    var totalStudyTimeInMinutes by remember { mutableStateOf(0L) }
    var totalFlashcardsReviewed by remember { mutableStateOf(0L) }
    var totalQuizzesReviewed by remember { mutableStateOf(0L) }
    var streak by remember { mutableStateOf(0L) }
    var dailyGoalInMinutes by remember { mutableStateOf(60L) }
    var dailyStats by remember { mutableStateOf(mapOf<String, Long>()) }
    var last5DaysStats by remember { mutableStateOf(listOf<Map<String, Long>>()) }

    LaunchedEffect(Unit) {
        studyTimeTracker.startTracking()
    }

    DisposableEffect(Unit) {
        onDispose {
            studyTimeTracker.stopTracking()
            val totalTime = studyTimeTracker.getTotalStudyTime()
            if (totalTime > 0 && userId != null) {
                updateStudyTime(userId, totalTime)
            }
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("❌ Firestore Error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val allDailyStats = snapshot.get("dailyStats") as? Map<String, Map<String, Long>> ?: emptyMap()
                        val last5Days = getLast5Days()

                        last5DaysStats = last5Days.map { date ->
                            allDailyStats[date] ?: mapOf(
                                "studyTimeInMinutes" to 0L,
                                "flashcardsReviewed" to 0L,
                                "quizzesReviewed" to 0L
                            )
                        }

                        dailyStats = allDailyStats[getCurrentDate()] ?: emptyMap()

                        // ✅ Fetch Total Stats from Firestore
                        totalStudyTimeInMinutes = snapshot.getLong("totalStudyTimeInMinutes") ?: 0L
                        totalFlashcardsReviewed = snapshot.getLong("totalFlashcardsReviewed") ?: 0L
                        totalQuizzesReviewed = snapshot.getLong("totalQuizzesReviewed") ?: 0L

                        // ✅ Debugging Logs
                        println("📊 Last 5 Days Stats: $last5DaysStats")
                        println("📊 Today's Stats: $dailyStats")
                        println("📊 Total Study Time: $totalStudyTimeInMinutes")
                        println("📊 Total Flashcards Reviewed: $totalFlashcardsReviewed")
                        println("📊 Total Quizzes Reviewed: $totalQuizzesReviewed")
                    }
                }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, currentScreen = "stats", onPlusClick = onPlusClick)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkViolet)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)  // ✅ Ensures spacing between sections
            ) {
                item {
                    Text(
                        text = "Progress",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Hey, $username!",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // ✅ Move BarChart to the Top
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)  // ✅ Ensures enough space for the chart
                    ) {
                        BarChart(
                            studyData = last5DaysStats.map { it["studyTimeInMinutes"]?.toFloat() ?: 0f }
                        )
                    }
                }

                item {
                    Text(
                        text = "Today's Stats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item { StatCard("Study Time", "${dailyStats["studyTimeInMinutes"] ?: 0}m", R.drawable.ic_time) }
                item { StatCard("Flashcards Reviewed", "${dailyStats["flashcardsReviewed"] ?: 0}", R.drawable.ic_flashcard) }
                item { StatCard("Quizzes Reviewed", "${dailyStats["quizzesReviewed"] ?: 0}", R.drawable.ic_quiz) }

                item {
                    Text(
                        text = "Total Stats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item { StatCard("Total Study Time", "${totalStudyTimeInMinutes / 60}h ${totalStudyTimeInMinutes % 60}m", R.drawable.ic_time) }
                item { StatCard("Total Flashcards Reviewed", totalFlashcardsReviewed.toString(), R.drawable.ic_flashcard) }
                item { StatCard("Total Quizzes Reviewed", totalQuizzesReviewed.toString(), R.drawable.ic_quiz) }

                item {
                    Text(
                        text = "Goals & Streak",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item { StatCard("🔥 Streak", "$streak days", R.drawable.ic_streak) }
                item { StatCard("Daily Goal", "${dailyGoalInMinutes / 60}h", R.drawable.ic_goal) }

                item {
                    Text(
                        text = "Last 5 Days' Stats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (last5DaysStats.all { it["studyTimeInMinutes"] == 0L && it["flashcardsReviewed"] == 0L && it["quizzesReviewed"] == 0L }) {
                    item {
                        Text(
                            text = "No data available for the last 5 days.",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(last5DaysStats.size) { index ->
                        val date = getLast5Days()[index]
                        StatCard(
                            title = date,
                            value = "${last5DaysStats[index]["studyTimeInMinutes"] ?: 0}m | " +
                                    "${last5DaysStats[index]["flashcardsReviewed"] ?: 0} 📚 | " +
                                    "${last5DaysStats[index]["quizzesReviewed"] ?: 0} 🧠",
                            iconId = R.drawable.ic_calendar
                        )
                    }
                }
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



private fun updateStudyTime(userId: String, timeSpent: Long) {
    val firestore = FirebaseFirestore.getInstance()
    val currentDate = getCurrentDate()
    val userRef = firestore.collection("users").document(userId)

    firestore.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)

        val currentStats = snapshot.get("dailyStats.$currentDate") as? Map<String, Long> ?: emptyMap()

        val existingTime = currentStats["studyTimeInMinutes"] ?: 0L
        val newTime = existingTime + timeSpent

        val studyTimeUpdate = mapOf(
            "dailyStats.$currentDate.studyTimeInMinutes" to newTime, // ✅ Add instead of overwriting
            "totalStudyTimeInMinutes" to FieldValue.increment(timeSpent)
        )

        println("📝 Updating study time for $currentDate: $newTime minutes")

        transaction.update(userRef, studyTimeUpdate)
    }.addOnSuccessListener {
        println("✅ Study time updated successfully!")
    }.addOnFailureListener { e ->
        println("❌ Error updating study time: ${e.message}")
    }

    // ✅ Also update daily stats
    updateDailyStats(userId, studyTimeInMinutes = timeSpent.toInt())
}


// ✅ Enhanced BarChart Function
@Composable
fun BarChart(studyData: List<Float>) {
    val last5DaysData = studyData.takeLast(5) // ✅ Ensure only the last 5 days (including today)
    val dates = getLast5Days().takeLast(5) // ✅ Get corresponding last 5 dates

    // ✅ Handle empty or all-zero data
    if (last5DaysData.all { it == 0f }) {
        Text(
            text = "No study data available for the last 5 days.",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val maxData = last5DaysData.maxOrNull()?.coerceAtLeast(1f) ?: 1f // ✅ Prevent division by zero

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // ✅ Adjusted to fit bars + labels
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // ✅ Fixed height for consistency
        ) {
            val barWidth = size.width / (last5DaysData.size * 2) // ✅ Adjust width based on available data
            val spaceBetweenBars = barWidth * 0.5f
            val scaleFactor = size.height / maxData // ✅ Prevent bars from being too small

            println("📊 Canvas Size: $size")
            println("📊 Bar Width: $barWidth")
            println("📊 Scale Factor: $scaleFactor")

            last5DaysData.forEachIndexed { index, value ->
                if (value > 0) {
                    val xOffset = index * (barWidth + spaceBetweenBars) + barWidth
                    println("📊 Drawing Bar at Index $index: Value = $value, X Offset = $xOffset")
                    drawRect(
                        color = Color.Cyan,
                        topLeft = Offset(xOffset, size.height - (value * scaleFactor)),
                        size = Size(barWidth, value * scaleFactor)
                    )
                }
            }
        }

        // ✅ Show Date Labels Below Each Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dates.forEach { date ->
                Text(
                    text = date.takeLast(2), // ✅ Show last 2 digits (day)
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}






private fun updateDailyStats(userId: String, flashcardsReviewed: Int = 0, quizzesReviewed: Int = 0, studyTimeInMinutes: Int = 0) {
    val firestore = FirebaseFirestore.getInstance()
    val currentDate = getCurrentDate()
    val userRef = firestore.collection("users").document(userId)

    firestore.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)

        val currentStats = snapshot.get("dailyStats.$currentDate") as? Map<String, Long> ?: emptyMap()

        val existingFlashcards = currentStats["flashcardsReviewed"] ?: 0L
        val existingQuizzes = currentStats["quizzesReviewed"] ?: 0L
        val existingTime = currentStats["studyTimeInMinutes"] ?: 0L

        val newTime = existingTime + studyTimeInMinutes
        val newFlashcards = existingFlashcards + flashcardsReviewed
        val newQuizzes = existingQuizzes + quizzesReviewed

        val dailyStatsUpdate = mapOf(
            "dailyStats.$currentDate.studyTimeInMinutes" to newTime,
            "dailyStats.$currentDate.flashcardsReviewed" to newFlashcards,
            "dailyStats.$currentDate.quizzesReviewed" to newQuizzes
        )

        val cumulativeStatsUpdate = mapOf(
            "totalStudyTimeInMinutes" to FieldValue.increment(studyTimeInMinutes.toLong()),
            "totalFlashcardsReviewed" to FieldValue.increment(flashcardsReviewed.toLong()),
            "totalQuizzesReviewed" to FieldValue.increment(quizzesReviewed.toLong())
        )

        println("📊 Updating Study Time: $existingTime + $studyTimeInMinutes = $newTime")
        println("📊 Updating Flashcards: $existingFlashcards + $flashcardsReviewed = $newFlashcards")
        println("📊 Updating Quizzes: $existingQuizzes + $quizzesReviewed = $newQuizzes")

        transaction.update(userRef, dailyStatsUpdate)
        transaction.update(userRef, cumulativeStatsUpdate)
    }.addOnSuccessListener {
        println("✅ Daily stats updated successfully")
    }.addOnFailureListener { e ->
        println("❌ Error updating daily stats: ${e.message}")
    }
}


private fun resetDailyStatsIfNeeded(userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    val currentDate = getCurrentDate()
    val userRef = firestore.collection("users").document(userId)

    firestore.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)
        val lastRecordedDate = snapshot.getString("lastRecordedDate")
        val todayStats = snapshot.get("dailyStats.$currentDate") as? Map<String, Long>

        // ✅ Only reset if the stats for today are missing
        if (lastRecordedDate != currentDate && (todayStats == null || todayStats.isEmpty())) {
            val resetUpdate = mapOf(
                "dailyStats.$currentDate.studyTimeInMinutes" to 0,
                "dailyStats.$currentDate.flashcardsReviewed" to 0,
                "dailyStats.$currentDate.quizzesReviewed" to 0,
                "lastRecordedDate" to currentDate
            )
            transaction.update(userRef, resetUpdate)
        }
    }.addOnSuccessListener {
        println("✅ Daily stats reset only if missing for $currentDate")
    }.addOnFailureListener { e ->
        println("❌ Error resetting daily stats: ${e.message}")
    }
}


// ✅ Get the last 5 days in yyyy-MM-dd format
fun getLast5Days(): List<String> {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val calendar = java.util.Calendar.getInstance()
    return List(5) {
        val date = sdf.format(calendar.time)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1) // Move back one day
        date
    }.reversed() // Reverse to get the most recent date first
}

@Composable
fun StatCard(title: String, value: String, iconId: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B5C))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
