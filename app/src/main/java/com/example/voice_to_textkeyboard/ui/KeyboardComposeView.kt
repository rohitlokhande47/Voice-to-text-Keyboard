package com.example.voice_to_textkeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voice_to_textkeyboard.R

enum class KeyboardState {
    IDLE, RECORDING, PROCESSING
}

@Composable
fun VoiceKeyboardUI(
    keyboardState: KeyboardState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onBackspace: () -> Unit = {},
    onSpacePressed: () -> Unit = {},
    onEnterPressed: () -> Unit = {},
    onUndoPressed: () -> Unit = {},
    onRedoPressed: () -> Unit = {},
    onSelectAllPressed: () -> Unit = {}
) {
    val backgroundColor = when (keyboardState) {
        KeyboardState.IDLE -> Color(0xFF4CAF50)
        KeyboardState.RECORDING -> Color(0xFFE53935)
        KeyboardState.PROCESSING -> Color(0xFFFF9800)
    }

    val buttonText = when (keyboardState) {
        KeyboardState.IDLE -> "Hold to Record"
        KeyboardState.RECORDING -> "Recording..."
        KeyboardState.PROCESSING -> "Processing..."
    }

    var isPressed by remember { mutableStateOf(false) }
    var isRecordingPressed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238))
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voice recording button with enhanced visual feedback
        Box(
            modifier = Modifier
                .size(width = 320.dp, height = 70.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(
                    if (isRecordingPressed) backgroundColor.copy(alpha = 0.8f)
                    else backgroundColor
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            if (keyboardState == KeyboardState.IDLE) {
                                isPressed = true
                                isRecordingPressed = true
                                onStartRecording()
                                // Wait for release
                                tryAwaitRelease()
                                if (isPressed) {
                                    isPressed = false
                                    isRecordingPressed = false
                                    onStopRecording()
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.wave_sound),
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )

                if (keyboardState == KeyboardState.RECORDING) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Processing indicator
        if (keyboardState == KeyboardState.PROCESSING) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFFF9800),
                trackColor = Color(0xFF37474F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Converting speech to text...",
                fontSize = 12.sp,
                color = Color(0xFFB0BEC5),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Action buttons row (backspace, space, enter)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Backspace button
            var backspacePressed by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(60.dp, 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (backspacePressed) Color(0xFF455A64)
                        else Color(0xFF37474F)
                    )
                    .clickable {
                        onBackspace()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.backspace),
                    contentDescription = "Backspace",
                    tint = Color(0xFFB0BEC5),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Space bar
            var spacePressed by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (spacePressed) Color(0xFF455A64)
                        else Color(0xFF37474F)
                    )
                    .clickable {
                        onSpacePressed()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.space),
                        contentDescription = "Space",
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Space",
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Enter button
            var enterPressed by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(60.dp, 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (enterPressed) Color(0xFF455A64)
                        else Color(0xFF37474F)
                    )
                    .clickable {
                        onEnterPressed()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⏎",
                    fontSize = 18.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // New row for undo, redo, select all buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo button
            var undoPressed by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(80.dp, 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (undoPressed) Color(0xFF455A64)
                        else Color(0xFF37474F)
                    )
                    .clickable {
                        onUndoPressed()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Undo",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5),
                    fontWeight = FontWeight.Medium
                )
            }

            // Redo button
            var redoPressed by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(80.dp, 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (redoPressed) Color(0xFF455A64)
                        else Color(0xFF37474F)
                    )
                    .clickable {
                        onRedoPressed()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Redo",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5),
                    fontWeight = FontWeight.Medium
                )
            }

            // Select All button
            var selectAllPressed by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(80.dp, 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selectAllPressed) Color(0xFF455A64)
                        else Color(0xFF37474F)
                    )
                    .clickable {
                        onSelectAllPressed()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select All",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}