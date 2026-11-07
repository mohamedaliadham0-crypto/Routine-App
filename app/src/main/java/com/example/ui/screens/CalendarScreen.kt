package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.MainViewModel
import com.example.ui.components.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentMonthCursor by viewModel.currentMonthCursor.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val prayers by viewModel.prayerListForSelectedDate.collectAsState()

    val dayTasks = remember(allTasks, selectedDate) {
        allTasks.filter { it.date == selectedDate }
    }

    var quickTaskText by remember { mutableStateOf("") }
    var taskToCopy by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToMove by remember { mutableStateOf<TaskEntity?>(null) }

    val formattedSelectedDate = remember(selectedDate) {
        try {
            val d = LocalDate.parse(selectedDate)
            d.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
        } catch (_: Exception) {
            selectedDate
        }
    }

    val dayDone = dayTasks.count { it.done }
    val dayTotal = dayTasks.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Month Calendar
        item {
            MonthCalendarGrid(
                currentMonthCursor = currentMonthCursor,
                selectedDate = selectedDate,
                allTasks = allTasks,
                onSelectDate = { viewModel.selectDate(it) },
                onPrevMonth = { viewModel.prevMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onGoToToday = { viewModel.goToToday() }
            )
        }

        // Daily Schedule Header & Prayers for Selected Date
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
                            text = formattedSelectedDate,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$dayDone/$dayTotal done",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prayer times for this date
                    PrayerBar(
                        prayers = prayers,
                        onCycleStatus = { viewModel.cyclePrayerStatus(it) },
                        onSetTime = { id, time -> viewModel.setPrayerCustomTime(id, time) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Add Task Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quickTaskText,
                            onValueChange = { quickTaskText = it },
                            placeholder = { Text("＋ Add task for this day…", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calendar_quick_add_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (quickTaskText.isNotBlank()) {
                                    viewModel.addTask(quickTaskText, date = selectedDate)
                                    quickTaskText = ""
                                }
                            })
                        )

                        Button(
                            onClick = {
                                if (quickTaskText.isNotBlank()) {
                                    viewModel.addTask(quickTaskText, date = selectedDate)
                                    quickTaskText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("calendar_quick_add_btn")
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }

        // Tasks on selected date
        if (dayTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tasks for this day",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap Add above to schedule tasks on this date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(dayTasks, key = { it.id }) { task ->
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
