package com.makinul.alphabet.learn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.makinul.alphabet.learn.ui.screens.AlphabetDrawingScreen
import com.makinul.alphabet.learn.ui.screens.DrawingContent
import com.makinul.alphabet.learn.ui.theme.AlphabetLearnTheme
import kotlinx.coroutines.delay

data object Home
data object Details

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
                                DetailsScreen()
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
        delay(2000)
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
fun DetailsScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(2) }
    val tabs = listOf("Drawing", "Details", "Settings")

    Scaffold(
        topBar = {
            SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            when (selectedTabIndex) {
                0 -> DrawingContent()
                1 -> DetailsContentTab()
                2 -> SettingsTab()
            }
        }
    }
}

@Composable
fun DetailsContentTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Details Content Area")
    }
}

@Composable
fun SettingsTab() {
    var isAuthenticated by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isAuthenticated) {
            Text("User Authenticated")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { isAuthenticated = false }) {
                Text("Logout")
            }
        } else {
            Text("Authentication Required")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { isAuthenticated = true }) {
                Text("Login")
            }
        }
    }
}
