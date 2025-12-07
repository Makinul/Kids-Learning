package com.makinul.alphabet.learn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.makinul.alphabet.learn.ui.screens.AlphabetDrawingScreen
import com.makinul.alphabet.learn.ui.theme.AlphabetLearnTheme
import kotlinx.coroutines.delay

data object Home
data object Details
data object Drawing

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlphabetLearnTheme {
                val backStack = remember { mutableStateListOf<Any>(Home) }

                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        } else {
                            finish()
                        }
                    },
                    entryProvider = { key ->
                        when (key) {
                            Home -> NavEntry(key) {
                                HomeScreen {
                                    // Replace Home with Details so Home is not in the back stack
                                    if (backStack.isNotEmpty()) {
                                        backStack[backStack.lastIndex] = Details
                                    }
                                }
                            }

                            Details -> NavEntry(key) {
                                DetailsScreen {
                                    backStack.add(Drawing)
                                }
                            }

                            Drawing -> NavEntry(key) {
                                AlphabetDrawingScreen()
                            }

                            else -> NavEntry(key) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Unknown Destination")
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(5000)
        onTimeout()
    }
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DetailsScreen(onStartClick: () -> Unit) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onStartClick) {
                Text(text = "Start Learning")
            }
        }
    }
}
