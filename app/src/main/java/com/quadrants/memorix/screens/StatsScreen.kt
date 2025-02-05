package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.WorkSans
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

@Composable
fun StatsScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, currentScreen = "stats", onPlusClick = {}) },
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
                            text = "Hey, Lodi !",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Bar Chart
                    Text(
                        text = "Study Progress Chart",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    BarChart()

                    Spacer(modifier = Modifier.height(20.dp))

                    // Statistics Cards
                    StatCard("Study Time", "4h 20m", R.drawable.ic_time)
                    StatCard("Daily Goal", "45m", R.drawable.ic_goal)
                    StatCard("Items Reviewed", "142", R.drawable.ic_review)
                    StatCard("🔥 Streak", "5 days", R.drawable.ic_streak)
                }
            }
        }
    )
}

// ✅ Placeholder for StatCard Function
@Composable
fun StatCard(title: String, value: String, iconId: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                Text(text = title, fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ✅ Placeholder for BarChart Function
@Composable
fun BarChart() {
    val studyData = listOf(4f, 5.5f, 3f, 6f, 4.5f) // Sample data
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val barWidth = size.width / (studyData.size * 2)
        val maxData = studyData.maxOrNull() ?: 1f
        val scaleFactor = size.height / maxData

        studyData.forEachIndexed { index, value ->
            drawRect(
                color = Color.Cyan,
                topLeft = Offset(index * 2 * barWidth + barWidth / 2, size.height - (value * scaleFactor)),
                size = Size(barWidth, value * scaleFactor)
            )
        }
    }
}
