package com.quadrants.memorix

import StudyTimeTracker
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.lifecycleScope
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.firestore.FirebaseFirestore
import com.quadrants.memorix.ui.theme.MemorixTheme
import com.quadrants.memorix.ui.theme.DarkViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.os.ParcelFileDescriptor
import android.text.InputType
import android.util.Base64
import android.widget.EditText
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.json.JSONException

class MainActivity : ComponentActivity() {

    private lateinit var studyTimeTracker: StudyTimeTracker // ✅ Change val to lateinit var

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studyTimeTracker = StudyTimeTracker(this) // ✅ Now it works!

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MemorixTheme {
                AppNavigation(this, studyTimeTracker)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        studyTimeTracker.startTracking() // ✅ Start tracking when app is active
    }

    override fun onPause() {
        super.onPause()
        studyTimeTracker.stopTracking() // ✅ Save time when app is paused
        saveStudyTimeToFirestore()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveStudyTimeToFirestore() // ✅ Ensure time is saved when app closes
    }

    private fun saveStudyTimeToFirestore() {
        val totalTime = studyTimeTracker.getTotalStudyTime()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && totalTime > 0) {
            updateStudyTime(userId, totalTime)
        }
    }

    private fun updateStudyTime(userId: String, timeSpent: Long) {
        val firestore = FirebaseFirestore.getInstance()
        val currentDate = com.quadrants.memorix.utils.getCurrentDate() // ✅ Ensure correct import
        val userRef = firestore.collection("users").document(userId)

        val studyTimeUpdate = mapOf(
            "dailyStats.$currentDate.studyTimeInMinutes" to FieldValue.increment(timeSpent),
            "totalStudyTimeInMinutes" to FieldValue.increment(timeSpent)
        )

        firestore.runTransaction { transaction ->
            transaction.update(userRef, studyTimeUpdate)
        }.addOnSuccessListener {
            println("✅ Study time updated: $timeSpent minutes")
        }.addOnFailureListener { e ->
            println("❌ Error updating study time: ${e.message}")
        }
    }


    private val firestore = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { handleFileUpload(it) }
        }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let { handleCapturedImage(it) }
        }

    fun openFileOrCamera() {
        val options = arrayOf("Take Photo", "Choose File")
        AlertDialog.Builder(this)
            .setTitle("Upload Image/PDF")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhotoLauncher.launch(null) // Open Camera
                    1 -> pickFileLauncher.launch("*/*") // Open File Picker
                }
            }
            .show()
    }

    private fun handleFileUpload(uri: Uri) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val titleInput = EditText(context).apply { hint = "Enter Title" }
        val categoryInput = EditText(context).apply { hint = "Enter Category" }
        val numFlashcardsInput = EditText(context).apply {
            hint = "Number of Flashcards"; inputType = InputType.TYPE_CLASS_NUMBER
        }
        val numQuizQuestionsInput = EditText(context).apply {
            hint = "Number of Quiz Questions"; inputType = InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(titleInput)
        layout.addView(categoryInput)
        layout.addView(numFlashcardsInput)
        layout.addView(numQuizQuestionsInput)

        var generationType = "flashcards" // Default value

        AlertDialog.Builder(context)
            .setTitle("Customize Generation")
            .setView(layout)
            .setSingleChoiceItems(arrayOf("Generate Flashcards", "Generate Quiz"), -1) { _, which ->
                generationType = if (which == 0) "flashcards" else "quiz"
            }
            .setPositiveButton("Generate") { _, _ ->
                val title = titleInput.text.toString().ifEmpty { "Untitled Set" }
                val category = categoryInput.text.toString().ifEmpty { "General Knowledge" }
                val numFlashcards = numFlashcardsInput.text.toString().toIntOrNull() ?: 10
                val numQuizQuestions = numQuizQuestionsInput.text.toString().toIntOrNull() ?: 5

                lifecycleScope.launch {
                    val file = uriToFile(uri)
                    file?.let {
                        val extractedText = extractTextFromFile(Uri.fromFile(it))
                        if (extractedText.isNotEmpty()) {
                            generateQuizOrFlashcards(
                                extractedText,
                                generationType,
                                title,
                                category,
                                numFlashcards,
                                numQuizQuestions
                            )
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun handleCapturedImage(bitmap: Bitmap) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val titleInput = EditText(context).apply { hint = "Enter Title" }
        val categoryInput = EditText(context).apply { hint = "Enter Category" }
        val numFlashcardsInput = EditText(context).apply {
            hint = "Number of Flashcards"; inputType = InputType.TYPE_CLASS_NUMBER
        }
        val numQuizQuestionsInput = EditText(context).apply {
            hint = "Number of Quiz Questions"; inputType = InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(titleInput)
        layout.addView(categoryInput)
        layout.addView(numFlashcardsInput)
        layout.addView(numQuizQuestionsInput)

        var generationType = "flashcards" // Default value

        AlertDialog.Builder(context)
            .setTitle("Customize Generation")
            .setView(layout)
            .setSingleChoiceItems(arrayOf("Generate Flashcards", "Generate Quiz"), -1) { _, which ->
                generationType = if (which == 0) "flashcards" else "quiz"
            }
            .setPositiveButton("Generate") { _, _ ->
                val title = titleInput.text.toString().ifEmpty { "Untitled Set" }
                val category = categoryInput.text.toString().ifEmpty { "General Knowledge" }
                val numFlashcards = numFlashcardsInput.text.toString().toIntOrNull() ?: 10
                val numQuizQuestions = numQuizQuestionsInput.text.toString().toIntOrNull() ?: 5

                lifecycleScope.launch {
                    val file = bitmapToFile(bitmap)
                    file?.let {
                        val extractedText = extractTextFromFile(Uri.fromFile(it))
                        if (extractedText.isNotEmpty()) {
                            generateQuizOrFlashcards(
                                extractedText,
                                generationType,
                                title,
                                category,
                                numFlashcards,
                                numQuizQuestions
                            )
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun extractTextFromFile(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey =
                    "AIzaSyAIRoBDg2FPihYgqtnVZ52zM2QbrY2UL2I"  // Replace with your actual API Key
                val apiUrl = "https://vision.googleapis.com/v1/images:annotate?key=$apiKey"

                val file = uriToFile(uri) ?: return@withContext ""
                println("✅ File details: ${file.absolutePath}, Size: ${file.length()} bytes, Format: ${file.extension}")

                // Read the file as Base64
                val base64Image = file.readBytes().encodeBase64()
                val requestBody = createGoogleVisionRequest(base64Image)

                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                response.use { res ->
                    val responseBody = res.body?.string()
                    println("✅ Google Vision API Response: $responseBody") // Log full response

                    if (res.isSuccessful && !responseBody.isNullOrEmpty()) {
                        val jsonResponse = JSONObject(responseBody)
                        val textAnnotations = jsonResponse.optJSONArray("responses")
                            ?.optJSONObject(0)
                            ?.optJSONArray("textAnnotations")

                        if (textAnnotations != null && textAnnotations.length() > 0) {
                            val extractedText =
                                textAnnotations.optJSONObject(0)?.optString("description", "")
                            println("✅ Extracted Text: $extractedText")
                            return@withContext extractedText ?: ""
                        } else {
                            println("❌ No text found in image")
                            return@withContext ""
                        }
                    } else {
                        println("❌ Google Vision API Error: ${res.message}")
                        return@withContext ""
                    }
                }
            } catch (e: IOException) {
                println("❌ Error Extracting Text: ${e.message}")
                return@withContext ""
            }
        }
    }

    fun createGoogleVisionRequest(base64Image: String): RequestBody {
        val jsonBody = """
    {
        "requests": [
            {
                "image": {
                    "content": "$base64Image"
                },
                "features": [
                    {
                        "type": "TEXT_DETECTION"
                    }
                ]
            }
        ]
    }
    """.trimIndent()

        val mediaType = "application/json".toMediaTypeOrNull() ?: "application/json".toMediaType()
        return jsonBody.toRequestBody(mediaType)
    }

    fun ByteArray.encodeBase64(): String {
        return Base64.encodeToString(this, Base64.NO_WRAP)
    }

    private fun isPdfValid(file: File): Boolean {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        return try {
            parcelFileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfRenderer(parcelFileDescriptor).use { pdfRenderer ->
                pdfRenderer.pageCount > 0
            }
        } catch (e: Exception) {
            println("❌ PDF is invalid: ${e.message}")
            false
        } finally {
            parcelFileDescriptor?.close()
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_file")
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            println("✅ File created: ${file.absolutePath}")
            file
        } catch (e: IOException) {
            println("❌ Error converting URI to file: ${e.message}")
            null
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File? {
        return try {
            val file = File(cacheDir, "captured_image.jpg")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            println("✅ Bitmap saved to file: ${file.absolutePath}")
            file
        } catch (e: IOException) {
            println("❌ Error saving bitmap to file: ${e.message}")
            null
        }
    }


    private fun generateQuizOrFlashcards(
        text: String,
        generationType: String,
        title: String,
        category: String,
        numFlashcards: Int,
        numQuizQuestions: Int
    ) {
        val apiKey = "AIzaSyCJpVfHH6SDX604xS_MSAfwA8UNlPCHtGE"  // ✅ Replace this with a working key
        val modelName = "gemini-1.5-flash-001"
        val url = "https://generativelanguage.googleapis.com/v1/models/$modelName:generateContent?key=$apiKey"

        val prompt = """
    Generate a study set titled **"$title"** under the category **"$category"**.

    - If **flashcards**: Generate **$numFlashcards** flashcards (half multiple-choice, half term-definition).
    - If **quiz**: Generate **$numQuizQuestions** multiple-choice questions.

    Example Output:
    ```json
    {
      "cards": [
        { "type": "multiple-choice", "question": "What is the capital of France?", "answers": ["Berlin", "Madrid", "Paris", "Rome"], "correctAnswerIndex": 2 },
        { "type": "term-definition", "term": "Photosynthesis", "definition": "The process where plants convert sunlight into energy." }
      ]
    }
    ```

    Now generate the requested content based on this text:
    $text
    """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        val mediaType = "application/json".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ Gemini API Request Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val responseBody = res.body?.string()
                    println("✅ Gemini API Raw Response: $responseBody")

                    if (!responseBody.isNullOrEmpty()) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val aiGeneratedData = jsonResponse.optJSONArray("candidates")
                                ?.optJSONObject(0)
                                ?.optJSONObject("content")
                                ?.optJSONArray("parts")
                                ?.optJSONObject(0)
                                ?.optString("text", "")

                            if (!aiGeneratedData.isNullOrEmpty()) {
                                // ✅ Remove Markdown-style code block
                                val cleanedJson = aiGeneratedData
                                    .replace("```json", "") // Remove Markdown start
                                    .replace("```", "") // Remove Markdown end
                                    .trim()

                                // ✅ Parse the cleaned JSON string
                                val parsedJson = JSONObject(cleanedJson)
                                println("✅ Parsed Gemini Response: $parsedJson")

                                // ✅ Extract the `cards` array safely
                                val cardsArray = parsedJson.optJSONArray("cards")
                                if (cardsArray != null && cardsArray.length() > 0) {
                                    // ✅ Save the generated data to Firestore
                                    saveGeneratedDataToFirestore(parsedJson.toString(), title, category, generationType)
                                } else {
                                    println("❌ No flashcards or quizzes found in API response.")
                                }
                            } else {
                                println("❌ AI-generated data is empty.")
                            }

                        } catch (e: JSONException) {
                            println("❌ Error parsing JSON: ${e.message}")
                        }
                    } else {
                        println("❌ API Response was empty.")
                    }
                }
            }
        })
    }






    private fun listModels() {
        val apiKey = "AIzaSyCJpVfHH6SDX604xS_MSAfwA8UNlPCHtGE"
        val url = "https://generativelanguage.googleapis.com/v1/models?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ Gemini API Request Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val responseBody = res.body?.string()
                    println("✅ Gemini API Raw Response: $responseBody")
                }
            }
        })
    }


    private fun saveGeneratedDataToFirestore(
        generatedDataString: String, // ✅ Expect a string instead of JSONObject
        title: String,
        category: String,
        generationType: String
    ) {
        try {
            val parsedJson = JSONObject(generatedDataString) // ✅ Parse inside function
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val firestore = FirebaseFirestore.getInstance()

            val setID = firestore.collection(if (generationType == "quiz") "quiz_questions" else "flashcard_sets").document().id
            val cardsArray = parsedJson.optJSONArray("cards") ?: JSONArray()

            if (cardsArray.length() == 0) {
                println("❌ No flashcards or quiz data found.")
                return
            }

            val cards: MutableList<Map<String, Any>> = mutableListOf()

            for (i in 0 until cardsArray.length()) {
                val card = cardsArray.optJSONObject(i)
                if (card != null) {
                    val type = card.optString("type", "")
                    if (type == "multiple-choice") {
                        cards.add(
                            mapOf(
                                "question" to card.optString("question", "No question"),
                                "answers" to (card.optJSONArray("answers")?.toStringList() ?: emptyList()),
                                "correctAnswerIndex" to card.optInt("correctAnswerIndex", 0),
                                "type" to "multiple-choice"
                            )
                        )
                    } else if (type == "term-definition") {
                        cards.add(
                            mapOf(
                                "term" to card.optString("term", "Unknown"),
                                "definition" to card.optString("definition", "No definition"),
                                "type" to "term-definition"
                            )
                        )
                    }
                }
            }

            if (cards.isEmpty()) {
                println("❌ No flashcards or quizzes found in extracted data.")
                return
            }

            val flashcardSetData = mapOf(
                "setID" to setID,
                "title" to title,
                "category" to category,
                "createdBy" to userId,
                "cards" to cards
            )

            firestore.collection(if (generationType == "quiz") "quiz_questions" else "flashcard_sets")
                .document(setID)
                .set(flashcardSetData)
                .addOnSuccessListener {
                    println("✅ AI-generated flashcards saved successfully with title: $title and category: $category")
                }
                .addOnFailureListener { e ->
                    println("❌ Error saving AI flashcards: ${e.message}")
                }

        } catch (e: JSONException) {
            println("❌ Error processing AI-generated JSON: ${e.message}")
        }
    }




    // Convert JSONArray to List<String>
    fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until this.length()) {
            list.add(this.optString(i, ""))
        }
        return list
    }
}