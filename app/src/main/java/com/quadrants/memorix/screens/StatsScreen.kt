package com.quadrants.memorix.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController, activity: MainActivity, onPlusClick: () -> Unit) {
    var showBottomSheet by remember { mutableStateOf(false) }

    // Firebase instances
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid

    // User stats state
    var username by remember { mutableStateOf("User") }
    var totalStudyTimeInMinutes by remember { mutableStateOf(0L) }
    var dailyGoalInMinutes by remember { mutableStateOf(60L) }
    var itemsReviewed by remember { mutableStateOf(0L) }
    var streak by remember { mutableStateOf(0L) }
    var studyData by remember { mutableStateOf(listOf<Float>()) }
    val coroutineScope = rememberCoroutineScope()

    // ✅ Real-time Listener for User Stats
    LaunchedEffect(userId) {
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)

            userRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("❌ Firestore Error: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    username = snapshot.getString("username") ?: "User"
                    totalStudyTimeInMinutes = snapshot.getLong("totalStudyTimeInMinutes") ?: 0L
                    dailyGoalInMinutes = snapshot.getLong("dailyGoalInMinutes") ?: 60L
                    itemsReviewed = snapshot.getLong("itemsReviewed") ?: 0L
                    streak = snapshot.getLong("streak") ?: 0L
                }
            }

            // ✅ Fetch Daily Stats for Last 5 Days
            val last5Days = getLast5Days()
            firestore.collection("users").document(userId)
                .collection("dailyStats")
                .whereIn("date", last5Days)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        println("❌ Error fetching daily stats: ${e.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        studyData = last5Days.map { date ->
                            val doc = snapshot.documents.find { it.id == date }
                            doc?.getLong("studyTimeInMin")?.toFloat()?.div(60) ?: 0f
                        }
                    }
                }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, currentScreen = "stats", onPlusClick = onPlusClick)
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkViolet)
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                ) {
                    // Profile & Title
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
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

                    // Bar Chart
                    Text(
                        text = "Study Progress Chart (Last 5 Days)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    BarChart(studyData = studyData)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Statistics Cards
                    StatCard("Total Study Time", "${totalStudyTimeInMinutes / 60}h ${totalStudyTimeInMinutes % 60}m", R.drawable.ic_time)
                    StatCard("Daily Goal", "${dailyGoalInMinutes / 60}h", R.drawable.ic_goal)
                    StatCard("Items Reviewed", itemsReviewed.toString(), R.drawable.ic_review)
                    StatCard("🔥 Streak", "$streak days", R.drawable.ic_streak)
                }
            }
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            BottomSheetContent(navController, activity) { showBottomSheet = false }
        }
    }
}

// ✅ Enhanced BarChart Function
@Composable
fun BarChart(studyData: List<Float>) {
    val maxData = studyData.maxOrNull() ?: 1f
    val barColors = studyData.map { if (it == maxData) Color.Cyan.copy(alpha = 1f) else Color.Cyan.copy(alpha = 0.7f) }
    val dates = getLast5Days()

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val barWidth = size.width / (studyData.size * 2)
            val scaleFactor = size.height / maxData

            studyData.forEachIndexed { index, value ->
                drawRect(
                    color = barColors[index],
                    topLeft = Offset(index * 2 * barWidth + barWidth / 2, size.height - (value * scaleFactor)),
                    size = Size(barWidth, value * scaleFactor)
                )
            }
        }

        // ✅ Labels under each bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dates.forEachIndexed { index, date ->
                Text(
                    text = "${date.takeLast(2)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ✅ Get the current date in yyyy-MM-dd format
fun getCurrentDate(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date())
}

// ✅ Get the last 5 days in yyyy-MM-dd format
fun getLast5Days(): List<String> {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    val calendar = java.util.Calendar.getInstance()
    return List(5) {
        val date = sdf.format(calendar.time)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        date
    }.reversed()
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
