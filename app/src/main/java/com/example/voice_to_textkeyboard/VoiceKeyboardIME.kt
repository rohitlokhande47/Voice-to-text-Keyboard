package com.example.voice_to_textkeyboard

import android.Manifest
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.voice_to_textkeyboard.ui.KeyboardState
import com.example.voice_to_textkeyboard.ui.VoiceKeyboardUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class VoiceKeyboardIME : InputMethodService(), ViewModelStoreOwner, SavedStateRegistryOwner, LifecycleOwner {

    private val TAG = "VoiceKeyboardIME"

    private lateinit var audioRecorder: AudioRecorder
    private lateinit var whisperApiService: WhisperApiService
    private var keyboardState by mutableStateOf(KeyboardState.IDLE)
    private var transcriptionMode by mutableStateOf(TranscriptionMode.NORMAL)
    private var composeView: ComposeView? = null

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override val viewModelStore: ViewModelStore = ViewModelStore()

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        audioRecorder = AudioRecorder(this)
        whisperApiService = WhisperApiService()

        val window = window?.window
        if (window != null) {
            window.decorView.let { decorView ->
                decorView.setViewTreeLifecycleOwner(this)
                decorView.setViewTreeViewModelStoreOwner(this)
                decorView.setViewTreeSavedStateRegistryOwner(this)
            }
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onCreateInputView(): View {
        composeView = ComposeView(this).apply {
            setContent {
                VoiceKeyboardUI(
                    keyboardState = keyboardState,
                    transcriptionMode = transcriptionMode,
                    onTranscriptionModeChanged = { mode ->
                        transcriptionMode = mode
                    },
                    onStartRecording = { startRecording() },
                    onStopRecording = { stopRecording() },
                    onBackspace = { handleBackspace() },
                    onSpacePressed = { handleSpacePressed() },
                    onEnterPressed = { handleEnterPressed() },
                    onUndoPressed = { handleUndo() },
                    onRedoPressed = { handleRedo() },
                    onSelectAllPressed = { handleSelectAll() }
                )
            }
        }
        return composeView!!
    }

    private fun startRecording() {
        if (checkAudioPermission()) {
            if (audioRecorder.startRecording()) {
                keyboardState = KeyboardState.RECORDING
            }
        }
    }

    private fun stopRecording() {
        val audioFile = audioRecorder.stopRecording()
        if (audioFile != null) {
            keyboardState = KeyboardState.PROCESSING
            scope.launch {
                processTranscription(audioFile)
            }
        } else {
            keyboardState = KeyboardState.IDLE
        }
    }

    private suspend fun processTranscription(audioFile: File) {
        try {
            val result = if (transcriptionMode == TranscriptionMode.SUMMARIZE) {
                // Call summarization API
                whisperApiService.summarizeAudio(audioFile)
            } else {
                // Regular transcription
                whisperApiService.transcribeAudio(audioFile)
            }

            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { transcription ->
                        insertText(transcription)
                        keyboardState = KeyboardState.IDLE
                    },
                    onFailure = {
                        keyboardState = KeyboardState.IDLE
                    }
                )
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                keyboardState = KeyboardState.IDLE
            }
        } finally {
            audioFile.delete()
        }
    }

    private fun insertText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun handleBackspace() {
        Log.d(TAG, "Backspace pressed, inputConnection = $currentInputConnection")
        currentInputConnection?.let { ic ->
            val selectedText = ic.getSelectedText(0)
            if (selectedText.isNullOrEmpty()) {
                // No text selected, delete one character before the cursor
                ic.deleteSurroundingText(1, 0)
            } else {
                // Delete selected text
                ic.commitText("", 1)
            }
        }
    }

    private fun handleSpacePressed() {
        Log.d(TAG, "Space pressed, inputConnection = $currentInputConnection")
        currentInputConnection?.commitText(" ", 1)
    }

    private fun handleEnterPressed() {
        Log.d(TAG, "Search pressed, inputConnection = $currentInputConnection")
        currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
    }

    private fun handleUndo() {
        Log.d(TAG, "Undo pressed, simulating Ctrl+Z")
        currentInputConnection?.sendKeyEvent(
            KeyEvent(
                0,
                0,
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_Z,
                0,
                KeyEvent.META_CTRL_ON
            )
        )
        currentInputConnection?.sendKeyEvent(
            KeyEvent(
                0,
                0,
                KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_Z,
                0,
                KeyEvent.META_CTRL_ON
            )
        )
    }

    private fun handleRedo() {
        Log.d(TAG, "Redo pressed, simulating Ctrl+Y")
        currentInputConnection?.sendKeyEvent(
            KeyEvent(
                0,
                0,
                KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_Y,
                0,
                KeyEvent.META_CTRL_ON
            )
        )
        currentInputConnection?.sendKeyEvent(
            KeyEvent(
                0,
                0,
                KeyEvent.ACTION_UP,
                KeyEvent.KEYCODE_Y,
                0,
                KeyEvent.META_CTRL_ON
            )
        )
    }

    private fun handleSelectAll() {
        Log.d(TAG, "Select All pressed, inputConnection = $currentInputConnection")
        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
    }

    private fun checkAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        job.cancel()
        viewModelStore.clear()
    }
}