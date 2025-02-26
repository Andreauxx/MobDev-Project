package com.quadrants.memorix.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.MainActivity
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.GoldenYellow
import com.quadrants.memorix.ui.theme.White


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, activity: MainActivity, onPlusClick: () -> Unit) {
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val userId = firebaseAuth.currentUser?.uid
    var showBottomSheet by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("Loading...") }
    var fullName by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("Loading...") }
    var birthday by remember { mutableStateOf("Not Set") }
    var course by remember { mutableStateOf("Not Set") }
    var school by remember { mutableStateOf("Not Set") }
    var isLoading by remember { mutableStateOf(true) }
    var profilePictureUrl by remember { mutableStateOf("") }


    // Fetch user data from Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: "Unknown"
                        fullName = document.getString("name") ?: "Unknown"
                        email = document.getString("email") ?: "Unknown"
                        birthday = document.getString("birthday") ?: "Not Set"
                        course = document.getString("course") ?: "Not Set"
                        school = document.getString("school") ?: "Not Set"
                        profilePictureUrl = document.getString("profilePictureUrl") ?: "" // ✅ Fetch Profile Picture
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(navController.context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }


    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, currentScreen = "profile", onPlusClick = onPlusClick )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkViolet)
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = GoldenYellow,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ✅ Profile Picture & Name
                        Box(contentAlignment = Alignment.TopEnd) {
                            AsyncImage(
                                model = if (profilePictureUrl.isNotEmpty()) profilePictureUrl else R.drawable.profile_placeholder,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_streak),
                                contentDescription = "Streak Icon",
                                tint = GoldenYellow,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(username, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                        Text(fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        // ✅ Favorites Section
                        Text("Favorites", fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FavoriteFolder(name = "Calculus", itemCount = 5)
                            FavoriteFolder(name = "Mobile Dev", itemCount = 5)
                        }
                        // Button to Navigate to SelectContentScreen
                        Button(
                            onClick = { navController.navigate("select_content") },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = "Customize Home Screen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkViolet
                            )
                        }


                        Spacer(modifier = Modifier.height(14.dp))

                        // ✅ User Information
                        ProfileDetail(icon = Icons.Default.Email, label = "Email", value = email)
                        ProfileDetail(icon = Icons.Default.Cake, label = "Birthday", value = birthday)
                        ProfileDetail(icon = Icons.Default.School, label = "School", value = school)
                        ProfileDetail(icon = Icons.Default.Book, label = "Course", value = course)

                        Spacer(modifier = Modifier.height(16.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between buttons
                        ) {
                            // Edit Profile Button
                            OutlinedButton(
                                onClick = { navController.navigate("edit_profile") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier.weight(1f), // Equal width
                                border = BorderStroke(1.dp, GoldenYellow)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Edit Profile", fontSize = 14.sp, color = Color.White)
                            }

                            // Logout Button
                            OutlinedButton(
                                onClick = { logout(navController) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier.weight(1f), // Equal width
                                border = BorderStroke(1.dp, Color.Red)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp, // Logout icon
                                    contentDescription = "Log Out",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Log Out", fontSize = 14.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.weight(0.2f))
                        // ✅ Footer
                        Text("Memorix Inc.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    }

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

@Composable
fun ProfileDetail(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = GoldenYellow, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Text(text = value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FavoriteFolder(name: String, itemCount: Int) {
    Card(
        modifier = Modifier.size(120.dp, 80.dp),
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


fun logout(navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    firebaseAuth.signOut() // Logs out the user

    // ✅ Remove stored user ID from SharedPreferences
    val context = navController.context
    val sharedPreferences = context.getSharedPreferences("MemorixPrefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove("userId").apply()

    // ✅ Navigate to Signup instead of Login
    navController.navigate("signup") {
        popUpTo("home") { inclusive = true } // Clears navigation stack
    }
}
