package com.quadrants.memorix.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.DarkViolet
import com.quadrants.memorix.ui.theme.GoldenYellow
import com.quadrants.memorix.utils.CloudinaryUploader
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import com.quadrants.memorix.ui.theme.WorkSans


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = firebaseAuth.currentUser?.uid

    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Text(text = "Edit Profile", color = DarkViolet, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = WorkSans)
    // Image Picker Launcher

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            coroutineScope.launch {
                CloudinaryUploader.uploadImage(
                    context = context,
                    uri = it,
                    onSuccess = { uploadedUrl ->
                        Log.d("CloudinaryUpload", "Upload Successful: $uploadedUrl")
                        profilePictureUrl = uploadedUrl
                        updateProfilePictureInFirestore(userId, uploadedUrl, context)
                        isLoading = false

                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { error ->
                        Log.e("CloudinaryUpload", "Upload Failed: $error")
                        isLoading = false
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Image upload failed! $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

        }
    }


    // Fetch user data from Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: ""
                        fullName = document.getString("name") ?: ""
                        birthday = document.getString("birthday") ?: ""
                        course = document.getString("course") ?: ""
                        school = document.getString("school") ?: ""
                        profilePictureUrl = document.getString("profilePictureUrl") ?: ""
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // UI
    Box(
        modifier = Modifier.fillMaxSize().background(DarkViolet)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ Profile Picture
            AsyncImage(
                model = if (profilePictureUrl.isNotEmpty()) profilePictureUrl else R.drawable.profile_placeholder,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Change Profile Picture", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ User Input Fields
            CustomTextField(value = username, onValueChange = { username = it }, label = "Username", icon = Icons.Default.AccountCircle)
            CustomTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", icon = Icons.Default.Person)
            CustomTextField(value = birthday, onValueChange = { birthday = it }, label = "Birthday (YYYY-MM-DD)", icon = Icons.Default.Cake)
            CustomTextField(value = course, onValueChange = { course = it }, label = "Course", icon = Icons.Default.Book)
            CustomTextField(value = school, onValueChange = { school = it }, label = "School", icon = Icons.Default.School)

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Save Button
            Button(
                onClick = {
                    saveProfileChanges(userId, username, fullName, birthday, course, school, profilePictureUrl, context, navController)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.85f).height(55.dp)
            ) {
                Text(text = "Save Changes", color = DarkViolet, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = GoldenYellow)
            }
        }
    }
}



// ✅ Function to Save Profile Changes
fun saveProfileChanges(
    userId: String?,
    username: String,
    fullName: String,
    birthday: String,
    course: String,
    school: String,
    profilePictureUrl: String,
    context: Context,
    navController: NavController
) {
    if (userId.isNullOrEmpty()) {
        Toast.makeText(context, "User ID is missing.", Toast.LENGTH_SHORT).show()
        return
    }

    val userData: Map<String, Any> = mapOf(
        "username" to username,
        "name" to fullName,
        "birthday" to birthday,
        "course" to course,
        "school" to school,
        "profilePictureUrl" to profilePictureUrl
    )

    FirebaseFirestore.getInstance().collection("users").document(userId).update(userData)
        .addOnSuccessListener {
            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
            navController.navigate("profile")
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

// ✅ Function to Update Profile Picture in Firestore
fun updateProfilePictureInFirestore(userId: String?, uploadedUrl: String, context: Context) {
    userId?.let {
        FirebaseFirestore.getInstance().collection("users").document(it)
            .update("profilePictureUrl", uploadedUrl)
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save profile picture URL", Toast.LENGTH_SHORT).show()
            }
    }
}
