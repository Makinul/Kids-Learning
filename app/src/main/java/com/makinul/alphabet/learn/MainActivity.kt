package com.makinul.alphabet.learn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.makinul.alphabet.learn.ui.screens.AlphabetDrawingScreen
import com.makinul.alphabet.learn.ui.theme.AlphabetLearnTheme

data object InitialScreen
data class SecondaryScreen(val name: String)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlphabetLearnTheme {
                val backStack = remember { mutableStateListOf<Any>(SecondaryScreen("123")) }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is InitialScreen -> NavEntry(key) {
                                Greeting("Greetings") {
                                    backStack.add(SecondaryScreen("123"))
                                }
                            }

                            is SecondaryScreen -> NavEntry(key) {
                                AlphabetDrawingScreen()
                            }

                            else -> error("Invalid key: $key")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, onClick: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Button(
            onClick = { onClick },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Hello $name!")
        }
    }
}