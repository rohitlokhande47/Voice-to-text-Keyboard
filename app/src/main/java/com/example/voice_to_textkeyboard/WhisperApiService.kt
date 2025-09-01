package com.example.voice_to_textkeyboard

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.concurrent.TimeUnit

data class TranscriptionRequest(
    val file: MultipartBody.Part,
    val model: RequestBody,
    val temperature: RequestBody,
    val response_format: RequestBody
)

data class TranscriptionResponse(
    val text: String
)

interface WhisperApiInterface {
    @Multipart
    @POST("openai/v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("temperature") temperature: RequestBody,
        @Part("response_format") responseFormat: RequestBody
    ): Response<TranscriptionResponse>
}

class WhisperApiService {
    private val apiKey = "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" // Replace with actual API key

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.groq.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WhisperApiInterface::class.java)

    suspend fun transcribeAudio(audioFile: File): Result<String> {
        return try {
            val requestFile = RequestBody.create(
                "audio/m4a".toMediaTypeOrNull(),
                audioFile
            )

            val filePart = MultipartBody.Part.createFormData(
                "file",
                audioFile.name,
                requestFile
            )

            val model = RequestBody.create("text/plain".toMediaTypeOrNull(), "whisper-large-v3")
            val temperature = RequestBody.create("text/plain".toMediaTypeOrNull(), "0")
            val responseFormat = RequestBody.create("text/plain".toMediaTypeOrNull(), "json")

            val response = api.transcribeAudio(
                authorization = "Bearer $apiKey",
                file = filePart,
                model = model,
                temperature = temperature,
                responseFormat = responseFormat
            )

            if (response.isSuccessful) {
                val transcription = response.body()?.text ?: ""
                Result.success(transcription)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}