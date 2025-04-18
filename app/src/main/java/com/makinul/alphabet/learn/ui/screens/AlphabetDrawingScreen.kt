package com.makinul.alphabet.learn.ui.screens

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makinul.alphabet.learn.R
import com.makinul.alphabet.learn.utils.AppConstants
import java.util.Locale

@Preview
@Composable
fun AlphabetDrawingScreen() {
    var currentLetter by remember { mutableStateOf('A') }
    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    val context = LocalContext.current
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US) // Or another locale
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language specified is not supported!")
                } else {
                    ttsInitialized = true
                }
            } else {
                Log.e("TTS", "Initialization Failed!")
            }
        }

        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Draw the letter: $currentLetter",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Log.d("AlphabetDrawingScreen", "currentLetter: $currentLetter")
        if (ttsInitialized) {
            textToSpeech?.speak(currentLetter.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("TTS", "TTS not initialized yet!")
            // Optionally, show a message to the user
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Background letter
                Text(
                    text = currentLetter.toString(),
                    color = Color.LightGray,
                    fontSize = 200.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Drawing canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = Path().apply {
                                        moveTo(offset.x, offset.y)
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    currentPath?.lineTo(
                                        change.position.x,
                                        change.position.y
                                    )
                                },
                                onDragEnd = {
                                    currentPath?.let {
                                        paths = paths + it
                                        currentPath = null

                                        // Check if the drawn path is similar to the current letter
                                        if (AppConstants.isPathSimilarToChar(it, currentLetter)) {
                                            Log.d(
                                                "Drawing",
                                                "Path recognized as letter: $currentLetter"
                                            )
                                            // Handle correct drawing (e.g., move to the next letter, show success message)
                                            // For example:
                                            // currentLetter = if (currentLetter == 'Z') 'A' else currentLetter + 1
                                            // paths = emptyList()
                                        } else {
                                            Log.d(
                                                "Drawing",
                                                "Path not recognized as letter: $currentLetter"
                                            )
                                            // Handle incorrect drawing (e.g., clear the path, show an error message)
                                            // For example:
                                            // paths = emptyList()
                                            // Show error message (you'll need to implement this in your UI)
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    paths.forEach { path ->
                        drawPath(
                            path = path,
                            color = Color.Blue,
                            style = Stroke(width = 20f, cap = StrokeCap.Round)
                        )
                    }
                    currentPath?.let { path ->
                        drawPath(
                            path = path,
                            color = Color.Blue,
                            style = Stroke(width = 20f, cap = StrokeCap.Round)
                        )
                    }
                    Log.d("drawPath", "currentLetter: $currentLetter")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    paths = emptyList()
                }
            ) {
                Text(stringResource(R.string.clear))
            }

            Button(
                onClick = {
                    currentLetter = if (currentLetter == 'Z') 'A' else currentLetter + 1
                    paths = emptyList()
                }
            ) {
                Text(stringResource(R.string.next_letter))
            }
        }
    }
}