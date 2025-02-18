package com.quadrants.memorix.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface CloudinaryService {
    @Multipart
    @POST
    suspend fun uploadImage(
        @Url url: String, // Dynamic URL for Cloudinary
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryResponse
}

data class CloudinaryResponse(
    val secure_url: String
)
