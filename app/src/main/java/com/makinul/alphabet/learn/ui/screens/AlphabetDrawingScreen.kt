package com.makinul.alphabet.learn.ui.screens

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makinul.alphabet.learn.R
import com.makinul.alphabet.learn.utils.AppConstants
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetDrawingScreen() {
    var currentLetter by remember { mutableStateOf('A') }
    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var recognitionResult by remember { mutableStateOf<Boolean?>(null) }
    var recognitionFinished by remember { mutableStateOf(false) }
    var recognitionGeneration by remember { mutableStateOf(0) }

    val context = LocalContext.current
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    // Copyright
    // The Builder by Kevin MacLeod | https://incompetech.com/
    // Music promoted by https://www.chosic.com/free-music/all/
    // Creative Commons CC BY 3.0
    // https://creativecommons.org/licenses/by/3.0/
    //
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.background_music) }

    LaunchedEffect(paths, currentLetter) {
        val generation = recognitionGeneration
        if (paths.isNotEmpty() && !recognitionFinished && recognitionResult == null) {
            kotlinx.coroutines.delay(3_000)
            if (generation == recognitionGeneration && !recognitionFinished && recognitionResult == null) {
                recognitionFinished = true
                recognitionResult = AppConstants.isPathsSimilarToChar(paths, currentLetter)
            }
        }
    }

    LaunchedEffect(recognitionResult) {
        if (recognitionResult != null) {
            kotlinx.coroutines.delay(1_800)
            recognitionGeneration++
            recognitionResult = null
            paths = emptyList()
        }
    }

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
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            mediaPlayer.release()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Draw the letter: $currentLetter") }) }
    ) { inlinePadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inlinePadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Log.d("AlphabetDrawingScreen", "currentLetter: $currentLetter")
                if (ttsInitialized) {
                    textToSpeech?.speak(
                        currentLetter.toString(),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                } else {
                    Log.e("TTS", "TTS not initialized yet!")
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // Background letter
                        Text(
                            text = currentLetter.toString(),
                            color = Color.LightGray.copy(alpha = 0.5f),
                            fontSize = 200.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        // Drawing canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                        recognitionGeneration++
                                        recognitionFinished = false
                                        recognitionResult = null
                                            currentPath = Path().apply {
                                                moveTo(offset.x, offset.y)
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            currentPath?.let { path ->
                                                currentPath = Path().apply {
                                                    addPath(path)
                                                    lineTo(change.position.x, change.position.y)
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            currentPath?.let {
                                                val completedPaths = paths + it
                                                paths = completedPaths
                                                currentPath = null

                                                if (AppConstants.isPathsSimilarToChar(
                                                        completedPaths,
                                                        currentLetter
                                                    )
                                                ) {
                                                    Log.d(
                                                        "Drawing",
                                                        "Path recognized as letter: $currentLetter"
                                                    )
                                                } else {
                                                    Log.d(
                                                        "Drawing",
                                                        "Path not recognized as letter: $currentLetter"
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                        ) {
                            paths.forEach { path ->
                                drawPath(
                                    path = path,
                                    color = Color(0xFF42A5F5),
                                    style = Stroke(width = 25f, cap = StrokeCap.Round)
                                )
                            }
                            currentPath?.let { path ->
                                drawPath(
                                    path = path,
                                    color = Color(0xFF42A5F5),
                                    style = Stroke(width = 25f, cap = StrokeCap.Round)
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
                            currentLetter = if (currentLetter == 'A') 'Z' else currentLetter - 1
                            paths = emptyList()
                            recognitionResult = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                    ) {
                        Text(stringResource(R.string.previous_letter), color = Color.White)
                    }

                    Button(
                        onClick = {
                            paths = emptyList()
                            recognitionResult = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                    ) {
                        Text(stringResource(R.string.clear), color = Color.White)
                    }

                    Button(
                        onClick = {
                            currentLetter = if (currentLetter == 'Z') 'A' else currentLetter + 1
                            paths = emptyList()
                            recognitionResult = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
                    ) {
                        Text(stringResource(R.string.next_letter), color = Color.White)
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = recognitionResult != null,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (recognitionResult == true) "✓ Success!" else "✕ Try Again",
                color = if (recognitionResult == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
