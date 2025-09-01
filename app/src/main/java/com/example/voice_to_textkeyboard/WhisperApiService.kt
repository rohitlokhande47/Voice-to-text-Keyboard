package com.example.voice_to_textkeyboard

import android.util.Log
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

    @Multipart
    @POST("openai/v1/audio/transcriptions")
    suspend fun summarizeAudio(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("prompt") prompt: RequestBody,
        @Part("response_format") responseFormat: RequestBody
    ): Response<TranscriptionResponse>
}

class WhisperApiService {
    private val apiKey = BuildConfig.WHISPER_API_KEY

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
        Log.d("WhisperAPI", "Starting transcription for file: ${audioFile.name}, size: ${audioFile.length()}")
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
                Log.d("WhisperAPI", "Starting transcription for file: ${audioFile.name}, size: ${audioFile.length()}")
                Result.success(transcription)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("WhisperAPI", "API Error: ${response.code()} ${response.message()} - Body: $errorBody")
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun summarizeAudio(audioFile: File): Result<String> {
        Log.d("WhisperAPI", "Starting summarization for file: ${audioFile.name}, size: ${audioFile.length()}")
        return try {
            val requestFile = RequestBody.create(
                "audio/wav".toMediaTypeOrNull(),
                audioFile
            )

            val filePart = MultipartBody.Part.createFormData(
                "file",
                audioFile.name,
                requestFile
            )

            // In summarizeAudio method
            val model = RequestBody.create("text/plain".toMediaTypeOrNull(), "whisper-large-v3")
            val prompt = RequestBody.create("text/plain".toMediaTypeOrNull(),
                "Please summarize the following speech concisely.")
            val responseFormat = RequestBody.create("text/plain".toMediaTypeOrNull(), "json")

            val response = api.summarizeAudio(
                authorization = "Bearer $apiKey",
                file = filePart,
                model = model,
                prompt = prompt,
                responseFormat = responseFormat
            )

            if (response.isSuccessful) {
                val summarizedText = response.body()?.text ?: ""
                Log.d("WhisperAPI", "Summarization successful: $summarizedText")
                Result.success(summarizedText)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("WhisperAPI", "API Error: ${response.code()} ${response.message()} - Body: $errorBody")
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("WhisperAPI", "Summarization failed", e)
            Result.failure(e)
        }
    }
}