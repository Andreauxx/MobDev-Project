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
import com.quadrants.memorix.R
import com.quadrants.memorix.ui.theme.*

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val oneTapClient = remember { Identity.getSignInClient(context) }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(DarkViolet, darkIcons = false)
        systemUiController.setNavigationBarColor(DarkViolet, darkIcons = false)
    }

    // ✅ Setup Google Sign-Up Request
    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("YOUR_WEB_CLIENT_ID") // Replace with Firebase Web Client ID
                    .setFilterByAuthorizedAccounts(false) // Show all Google accounts
                    .build()
            )
            .build()
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val googleIdToken = credential.googleIdToken

            if (googleIdToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Signed up with Google!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") // Redirect to home
                        } else {
                            Toast.makeText(context, "Google Sign-Up failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
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

            // ✅ Email Sign-Up Button
            Button(
                onClick = { navController.navigate("email_signup") },
                colors = ButtonDefaults.buttonColors(containerColor = DarkieViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(55.dp)
                    .border(width = 1.dp, color = GoldenYellow, shape = RoundedCornerShape(12.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sign up with Email", color = White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(160.dp))

            // ✅ Log in Redirection
            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(55.dp)
                    .border(width = 1.dp, color = GoldenYellow, shape = RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "Log in",
                    color = White,
                    fontSize = 12.sp,
                    fontFamily = WorkSans
                )
            }
        }
    }
}



@Composable
fun EmailSignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

        Button(onClick = {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home")
                    } else {
                        Toast.makeText(context, "Registration Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
        }) {
            Text("Sign Up")
        }
    }
}
