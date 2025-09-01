package com.example.voice_to_textkeyboard

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false

    fun startRecording(): Boolean {
        return try {
            // Create output file
            outputFile = File(context.cacheDir, "voice_recording_${System.currentTimeMillis()}.m4a")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile!!.absolutePath)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)

                prepare()
                start()
            }

            isRecording = true
            true
        } catch (e: IOException) {
            e.printStackTrace()
            cleanup()
            false
        }
    }

    fun stopRecording(): File? {
        return if (isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
                outputFile
            } catch (e: Exception) {
                e.printStackTrace()
                cleanup()
                null
            }
        } else {
            null
        }
    }

    fun isRecording(): Boolean = isRecording

    private fun cleanup() {
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
        outputFile = null
    }
}