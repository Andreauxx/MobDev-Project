package com.quadrants.memorix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import androidx.compose.foundation.Image

@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, currentScreen = "profile", onPlusClick = {})
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
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture & Name
                    Box(contentAlignment = Alignment.TopEnd) {
                        Image(
                            painter = painterResource(id = R.drawable.profile_placeholder),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_streak),
                            contentDescription = "Streak Icon",
                            tint = Color.Yellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("felix_mapagmahaluwu", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                    Text("Felix Abrasaldo Jr.", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Favorites Section
                    Text("Favorites", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FavoriteFolder(name = "Calculus", itemCount = 5)
                        FavoriteFolder(name = "Mobile Dev", itemCount = 5)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit Button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(
                            onClick = { /* Handle edit */ },
                            modifier = Modifier.size(48.dp) // Increased size
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit Profile",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp) // Increased icon size
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Footer
                    Text("Memorix Inc.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    )
}

@Composable
fun FavoriteFolder(name: String, itemCount: Int) {
    Card(
        modifier = Modifier
            .size(140.dp, 100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B5C))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = name,
                tint = Color.Blue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, fontSize = 14.sp, color = Color.White)
            Text(text = "$itemCount items", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}
