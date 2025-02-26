package com.quadrants.memorix

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.quadrants.memorix.ui.theme.MemorixTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // TODO: Handle file selection (Upload to Firebase or Save)
            }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                // TODO: Handle captured photo (Upload, Save, etc.)
            }
        }

    fun openFileOrCamera() {
        val options = arrayOf("Take Photo", "Choose File")
        android.app.AlertDialog.Builder(this)
            .setTitle("Upload Image/PDF")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhotoLauncher.launch(null) // Open Camera
                    1 -> pickFileLauncher.launch("*/*") // Open File Picker
                }
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // <-- Add this line
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MemorixTheme {
                AppNavigation(this)
            }
        }
    }
}