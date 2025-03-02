package com.quadrants.memorix

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
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.json.JSONException

class MainActivity : ComponentActivity() {
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
        val options = arrayOf("Generate Flashcards", "Generate Quiz")

        AlertDialog.Builder(this)
            .setTitle("Choose Generation Type")
            .setItems(options) { _, which ->
                val generationType = if (which == 0) "flashcards" else "quiz"
                lifecycleScope.launch {
                    val extractedText = extractTextFromFile(uri)
                    if (extractedText.isNotEmpty()) {
                        generateQuizOrFlashcards(extractedText, generationType)
                    }
                }
            }
            .show()
    }

    private fun handleCapturedImage(bitmap: Bitmap) {
        val options = arrayOf("Generate Flashcards", "Generate Quiz")

        AlertDialog.Builder(this)
            .setTitle("Choose Generation Type")
            .setItems(options) { _, which ->
                val generationType = if (which == 0) "flashcards" else "quiz"
                lifecycleScope.launch {
                    val file = bitmapToFile(bitmap)
                    file?.let {
                        val extractedText = extractTextFromFile(Uri.fromFile(it))
                        if (extractedText.isNotEmpty()) {
                            generateQuizOrFlashcards(extractedText, generationType)
                        }
                    }
                }
            }
            .show()
    }

    private suspend fun extractTextFromFile(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = "AIzaSyAIRoBDg2FPihYgqtnVZ52zM2QbrY2UL2I"  // Replace with your actual API Key
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
                            val extractedText = textAnnotations.optJSONObject(0)?.optString("description", "")
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
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
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

    private fun generateQuizOrFlashcards(text: String, type: String) {
        val generationType = type
        val apiKey = "AIzaSyCJpVfHH6SDX604xS_MSAfwA8UNlPCHtGE"  // ✅ Replace this with a working key
        val modelName = "gemini-1.5-flash-001"  // ✅ Use an available model
        val url = "https://generativelanguage.googleapis.com/v1/models/$modelName:generateContent?key=$apiKey"

        // ✅ Clean and preprocess the input text
        val cleanedText = text
            .replace("[^a-zA-Z0-9\\s.]".toRegex(), "") // Remove special characters
            .replace("\\s+".toRegex(), " ") // Remove extra spaces
            .trim()

        // ✅ Validate the input text
        if (cleanedText.isEmpty() || cleanedText.length < 10) {
            println("❌ Input text is too short or invalid.")
            return
        }

        // ✅ AI Instruction: Explicitly ask for Flashcards or Quiz
        val prompt = """
    You are an AI that generates **educational study materials** based on text input. 
    Given the following input text, generate **$generationType** in a structured JSON format:
    
    If **flashcards**, create a **list of term-definition pairs** and **multiple-choice questions**.
    If **quiz**, create **only multiple-choice questions** with **a 30-second time limit per question**.
    
    Example Output:
    {
      "cards": [
        {
          "type": "multiple-choice",
          "question": "What is the capital of France?",
          "answers": ["Berlin", "Madrid", "Paris", "Rome"],
          "correctAnswerIndex": 2,
          "explanation": "Paris is the capital of France."
        },
        {
          "type": "term-definition",
          "term": "Photosynthesis",
          "definition": "The process by which green plants and some other organisms use sunlight to synthesize foods from carbon dioxide and water."
        }
      ]
    }
    
    Now, generate structured $generationType from the following text:
    $cleanedText
""".trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt) // ✅ Pass the AI instruction prompt
                        })
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

        println("🔍 Using API Key: $apiKey") // ✅ Debug API Key
        println("🔍 Request URL: $url") // ✅ Debug Request URL
        println("🔍 Request Body: $jsonBody") // ✅ Debug Request Body

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("❌ Gemini API Request Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    val responseBody = res.body?.string()
                    println("✅ Gemini API Raw Response: $responseBody")

                    if (!responseBody.isNullOrEmpty()) {
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("error")) {
                            val error = jsonResponse.optJSONObject("error")
                            println("❌ Gemini API Error: ${error?.optString("message")}")
                        } else {
                            val aiGeneratedData = jsonResponse.optJSONArray("candidates")
                                ?.optJSONObject(0)
                                ?.optJSONObject("content")
                                ?.optJSONArray("parts")
                                ?.optJSONObject(0)
                                ?.optString("text", "No response from Gemini")

                            if (!aiGeneratedData.isNullOrEmpty()) {
                                // ✅ Extract JSON from Markdown syntax
                                val cleanedJson = aiGeneratedData
                                    .replace("```json", "") // Remove Markdown start
                                    .replace("```", "") // Remove Markdown end
                                    .trim() // Remove leading/trailing whitespace

                                try {
                                    // ✅ Parse the cleaned JSON string
                                    val parsedJson = JSONObject(cleanedJson)
                                    println("✅ Parsed Gemini Response: $parsedJson")

                                    // ✅ Save the generated data to Firestore
                                    saveGeneratedDataToFirestore(parsedJson, generationType)
                                } catch (e: JSONException) {
                                    println("❌ Error parsing JSON: ${e.message}")
                                }
                            } else {
                                println("❌ No valid AI-generated content found")
                            }
                        }
                    } else {
                        println("❌ Gemini API returned an empty response")
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






    private fun saveGeneratedDataToFirestore(generatedData: JSONObject, generationType: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            println("❌ User is not authenticated. Cannot save data.")
            return
        }

        val userId = user.uid // ✅ Get authenticated user's ID
        val firestore = FirebaseFirestore.getInstance()

        // ✅ Extract category dynamically
        val category = generatedData.optString("category", "General Knowledge") // Default if not found
        val title = category // ✅ Use category as title

        val setID = firestore.collection(if (generationType == "quiz") "quiz_questions" else "flashcard_sets").document().id

        val cards: MutableList<Map<String, Any>> = mutableListOf()

        val aiGeneratedCards = generatedData.optJSONArray("cards")

        if (aiGeneratedCards != null && aiGeneratedCards.length() > 0) {
            for (i in 0 until aiGeneratedCards.length()) {
                val card = aiGeneratedCards.optJSONObject(i)
                if (card != null) {
                    val type = card.optString("type")

                    if (type == "multiple-choice") {
                        cards.add(
                            mapOf(
                                "question" to card.optString("question"),
                                "answers" to (card.optJSONArray("answers")?.toStringList() ?: emptyList()),
                                "correctAnswerIndex" to card.optInt("correctAnswerIndex"),
                                "explanation" to card.optString("explanation"),
                                "type" to "multiple-choice" // ✅ Explicitly set type
                            )
                        )
                    } else if (type == "term-definition") {
                        cards.add(
                            mapOf(
                                "term" to card.optString("term"),
                                "definition" to card.optString("definition"),
                                "explanation" to card.optString("explanation"),
                                "type" to "term-definition" // ✅ Explicitly set type
                            )
                        )
                    }
                }
            }
        }

        if (cards.isEmpty()) {
            println("❌ No flashcards or quizzes generated. Skipping Firestore save.")
            return
        }

        if (generationType == "quiz") {
            // ✅ Save each MCQ separately in `quiz_questions`
            for (card in cards) {
                if (card["type"] == "multiple-choice") {
                    val quizID = firestore.collection("quiz_questions").document().id
                    val quizData = mapOf(
                        "quizID" to quizID,
                        "question" to card["question"]!!,
                        "answers" to card["answers"]!!,
                        "correctAnswerIndex" to card["correctAnswerIndex"]!!,
                        "explanation" to card["explanation"]!!,
                        "category" to category,
                        "title" to title,
                        "timeLimit" to 10 // ✅ Default time limit for quizzes
                    )

                    firestore.collection("quiz_questions").document(quizID).set(quizData)
                        .addOnSuccessListener {
                            println("✅ AI-generated quiz question saved successfully")
                        }
                        .addOnFailureListener { e ->
                            println("❌ Error saving AI quiz question: ${e.message}")
                        }
                }
            }
        } else {
            // ✅ Save flashcards in `flashcard_sets`
            val flashcardSetData = mapOf(
                "setID" to setID,
                "title" to title,
                "category" to category,
                "createdBy" to userId,
                "isPublic" to true,
                "cards" to cards // ✅ Store term-definition & MCQs inside `cards` array
            )

            firestore.collection("flashcard_sets").document(setID).set(flashcardSetData)
                .addOnSuccessListener {
                    println("✅ AI-generated flashcards saved successfully")
                }
                .addOnFailureListener { e ->
                    println("❌ Error saving AI flashcards: ${e.message}")
                }
        }

        // ✅ Update user profile with the created set
        val userDocumentRef = firestore.collection("users").document(userId)
        val fieldToUpdate = if (generationType == "quiz") "createdQuizzes" else "createdFlashcards"

        firestore.runTransaction { transaction ->
            val userDocument = transaction.get(userDocumentRef)
            val currentSets = userDocument.get(fieldToUpdate) as? MutableList<String> ?: mutableListOf()
            currentSets.add(setID)
            transaction.update(userDocumentRef, fieldToUpdate, currentSets)
        }.addOnSuccessListener {
            println("✅ User's $fieldToUpdate updated successfully with setID: $setID")
        }.addOnFailureListener { e ->
            println("❌ Error updating user's $fieldToUpdate: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MemorixTheme {
                val systemUiController = rememberSystemUiController()

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = DarkViolet,
                        darkIcons = false
                    )
                }

                AppNavigation(this)

            }
        }
        // Call listModels asynchronously
        lifecycleScope.launch {
            listModels()
        }
    }

    fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until this.length()) {
            list.add(this.optString(i, ""))
        }
        return list
    }
}