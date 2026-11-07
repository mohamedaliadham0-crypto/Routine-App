package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.StudyBottomNavigation
import com.example.ui.screens.*
import com.example.ui.theme.RoutineStudyOSTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            RoutineStudyOSTheme(darkTheme = isDarkTheme) {
                RoutineStudyOSApp(viewModel)
            }
        }
    }
}

@Composable
fun RoutineStudyOSApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button behavior
    BackHandler(enabled = currentScreen != Screen.Dashboard) {
        if (currentScreen == Screen.UserManual) {
            viewModel.navigateTo(Screen.Settings)
        } else {
            viewModel.navigateTo(Screen.Dashboard)
        }
    }

    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (currentScreen != Screen.UserManual) {
                StudyBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == Screen.Dashboard || currentScreen == Screen.Calendar) {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    Screen.Dashboard -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCalendar = { viewModel.navigateTo(Screen.Calendar) }
                    )
                    Screen.Calendar -> CalendarScreen(viewModel = viewModel)
                    Screen.Notes -> NotesScreen(viewModel = viewModel)
                    Screen.Analytics -> AnalyticsScreen(viewModel = viewModel)
                    Screen.Settings -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToManual = { viewModel.navigateTo(Screen.UserManual) }
                    )
                    Screen.UserManual -> UserManualScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.Settings) }
                    )
                }
            }
        }
    }

    // Quick Task Dialog for FAB
    if (showAddTaskDialog) {
        var taskName by remember { mutableStateOf("") }
        var taskSubject by remember { mutableStateOf("") }
        var taskPriority by remember { mutableStateOf("Normal") }
        var taskNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add New Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Task Title *") },
                        modifier = Modifier.fillMaxWidth().testTag("fab_task_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = taskSubject,
                        onValueChange = { taskSubject = it },
                        label = { Text("Subject (e.g. Math, Physics)") },
                        modifier = Modifier.fillMaxWidth().testTag("fab_task_subject_input"),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Low", "Normal", "High").forEach { p ->
                            FilterChip(
                                selected = taskPriority == p,
                                onClick = { taskPriority = p },
                                label = { Text(p) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = taskNotes,
                        onValueChange = { taskNotes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskName.isNotBlank()) {
                            viewModel.addTask(
                                name = taskName,
                                priority = taskPriority,
                                subject = taskSubject,
                                notes = taskNotes
                            )
                            showAddTaskDialog = false
                        }
                    },
                    enabled = taskName.isNotBlank(),
                    modifier = Modifier.testTag("fab_confirm_add_task")
                ) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
