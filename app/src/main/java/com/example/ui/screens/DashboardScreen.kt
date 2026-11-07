package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val todayTasks by viewModel.todayTasks.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val pendingReviews by viewModel.pendingReviewsCount.collectAsState()
    val heatmapItems by viewModel.heatmap14Days.collectAsState()
    val prayers by viewModel.prayerListForSelectedDate.collectAsState()

    var quickTaskText by remember { mutableStateOf("") }
    var taskToCopy by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToMove by remember { mutableStateOf<TaskEntity?>(null) }

    val formattedDateText = remember(selectedDate) {
        try {
            val d = LocalDate.parse(selectedDate)
            d.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
        } catch (_: Exception) {
            selectedDate
        }
    }

    val totalCompletedAllTime = allTasks.count { it.done }
    val todayTotal = todayTasks.size
    val todayDone = todayTasks.count { it.done }
    val todayPct = if (todayTotal > 0) (todayDone * 100 / todayTotal) else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Top welcome header
        item {
            Column {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedDateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4 Stat Cards in 2x2 grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Today's Progress",
                        value = "$todayPct%",
                        subtitle = "$todayDone of $todayTotal tasks",
                        gradient = PrimaryGradient,
                        progress = if (todayTotal > 0) todayDone.toFloat() / todayTotal else 0f,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completed Tasks",
                        value = totalCompletedAllTime.toString(),
                        subtitle = "all time",
                        gradient = CyanBlueGradient,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Current Streak",
                        value = "🔥 $currentStreak",
                        subtitle = "days in a row",
                        gradient = OrangeGoldGradient,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending Reviews",
                        value = pendingReviews.toString(),
                        subtitle = "revision tasks",
                        gradient = BluePurpleGradient,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Prayer Bar Capsule Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    PrayerBar(
                        prayers = prayers,
                        onCycleStatus = { viewModel.cyclePrayerStatus(it) },
                        onSetTime = { id, time -> viewModel.setPrayerCustomTime(id, time) }
                    )
                }
            }
        }

        // 14-Day Activity Heatmap
        item {
            ActivityHeatmap(
                items = heatmapItems,
                onSelectDay = { dateStr ->
                    viewModel.selectDate(dateStr)
                }
            )
        }

        // Today's Routine Schedule Header & Quick Add
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Routine",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = onNavigateToCalendar,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Full Calendar", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Add Task Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quickTaskText,
                            onValueChange = { quickTaskText = it },
                            placeholder = { Text("＋ Add a task, press Enter…", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_add_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (quickTaskText.isNotBlank()) {
                                    viewModel.addTask(quickTaskText)
                                    quickTaskText = ""
                                }
                            })
                        )

                        Button(
                            onClick = {
                                if (quickTaskText.isNotBlank()) {
                                    viewModel.addTask(quickTaskText)
                                    quickTaskText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("quick_add_btn")
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }

        // Daily Tasks List
        if (todayTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✨ Nothing planned",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add a task above to build your study routine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleDone = { viewModel.toggleTaskDone(task) },
                    onUpdate = { viewModel.updateTask(it) },
                    onDelete = { viewModel.deleteTask(task) },
                    onCopy = { taskToCopy = task },
                    onMove = { taskToMove = task }
                )
            }
        }
    }

    // Copy Task Modal
    taskToCopy?.let { task ->
        CopyTaskModal(
            task = task,
            onDismiss = { taskToCopy = null },
            onConfirmCopy = { dates ->
                viewModel.copyTaskToDates(task, dates)
                taskToCopy = null
            }
        )
    }

    // Move Task DatePicker
    taskToMove?.let { task ->
        MoveTaskDatePicker(
            currentDate = task.date,
            onDateSelected = { newDate ->
                viewModel.moveTask(task, newDate)
                taskToMove = null
            },
            onDismiss = { taskToMove = null }
        )
    }
}
