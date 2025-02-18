package com.quadrants.memorix.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.*
import java.util.*

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val oneTapClient = remember { Identity.getSignInClient(context) }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(DarkViolet, darkIcons = false)
        systemUiController.setNavigationBarColor(DarkViolet, darkIcons = false)
    }

    // ✅ Google Sign-Up Request Setup
    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("661088903021-bt8rrjs6kn0g6qr58agfafstv0f1amsd.apps.googleusercontent.com") // Replace this!
                    .setFilterByAuthorizedAccounts(false) // Show all Google accounts
                    .build()
            )
            .build()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val googleIdToken = credential.googleIdToken
                println("✅ Google ID Token received: $googleIdToken")

                if (googleIdToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                                val email = firebaseAuth.currentUser?.email ?: ""
                                val name = firebaseAuth.currentUser?.displayName ?: ""

                                firestore.collection("users").document(userId).get()
                                    .addOnSuccessListener { document ->
                                        if (!document.exists()) {
                                            println("🔹 Creating new user in Firestore")

                                            val newUser = hashMapOf(
                                                "name" to name,
                                                "email" to email,
                                                "age" to 0,
                                                "school" to "",
                                                "createdFlashcards" to emptyList<String>(),
                                                "createdQuizzes" to emptyList<String>(),
                                                "preferences" to hashMapOf(
                                                    "subjects" to emptyList<String>(),
                                                    "difficulty" to ""
                                                ),
                                                "sharedWithMe" to emptyList<String>()
                                            )

                                            firestore.collection("users").document(userId)
                                                .set(newUser)
                                                .addOnSuccessListener {
                                                    println("✅ User profile created in Firestore")
                                                    navController.navigate("user_details/$userId/$name/$email")
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            println("✅ User already exists in Firestore")
                                            Toast.makeText(context, "Welcome back, $name!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("home")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error checking user: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Google Sign-In failed: No token received!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Google Sign-In error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Google Sign-In canceled!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkViolet)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Icon(
                painter = painterResource(id = R.drawable.owl_icon),
                contentDescription = "Owl Icon",
                tint = White,
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "The best way to study.\nSign up for free.",
                fontSize = 22.sp,
                color = White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontFamily = WorkSans
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "By signing up, you accept Memorix's Terms of Service and Privacy Policy",
                fontSize = 14.sp,
                color = White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontFamily = WorkSans,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ Google Sign-Up Button
            Button(
                onClick = {
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { result ->
                            googleSignInLauncher.launch(
                                IntentSenderRequest.Builder(result.pendingIntent).build()
                            )
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Google Sign-Up Failed", Toast.LENGTH_SHORT).show()
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkieViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(55.dp)
                    .border(width = 1.dp, color = GoldenYellow, shape = RoundedCornerShape(12.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sign up with Google", color = White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Email Sign-Up Button (RESTORED)
            Button(
                onClick = { navController.navigate("email_signup") },
                colors = ButtonDefaults.buttonColors(containerColor = DarkieViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.85f).height(55.dp).border(width = 1.dp, color = GoldenYellow, shape = RoundedCornerShape(12.dp))
            ) {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Sign up with Email", color = White, fontSize = 16.sp)

            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Login Button (RESTORED)
            TextButton(
                onClick = { navController.navigate("login") }
            ) {
                Text("Already have an account? Log in", color = White, fontSize = 14.sp)
            }
        }
    }
}
