package com.quadrants.memorix.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.quadrants.memorix.network.CloudinaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object CloudinaryUploader {
    private const val CLOUD_NAME = "dp1tpyrrv" // ✅ Replace with your actual Cloudinary cloud name
    private const val UPLOAD_PRESET = "profilepic_preset" // ✅ Replace with your upload preset

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.cloudinary.com/v1_1/dp1tpyrrv/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    private val api: CloudinaryService = retrofit.create(CloudinaryService::class.java)

    suspend fun uploadImage(context: Context, uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val file = getFileFromUri(context, uri)
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val uploadPresetBody = RequestBody.create("text/plain".toMediaTypeOrNull(), UPLOAD_PRESET)

                val response = api.uploadImage("image/upload", body, uploadPresetBody)


                if (response.secure_url.isNotEmpty()) {
                    onSuccess(response.secure_url)
                } else {
                    onError("Upload failed: No URL returned")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
                Log.e("CloudinaryUploader", "Upload Error: ${e.message}", e)
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "upload_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
}
