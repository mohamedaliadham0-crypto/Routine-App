package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CopyTaskModal(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onConfirmCopy: (Set<String>) -> Unit
) {
    val selectedDates = remember { mutableStateOf(setOf<String>()) }

    val taskDate = try {
        LocalDate.parse(task.date)
    } catch (_: Exception) {
        LocalDate.now()
    }

    val year = taskDate.year
    val month = taskDate.monthValue
    val daysInMonth = taskDate.lengthOfMonth()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("copy_task_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Copy Task to Dates",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Copying “${task.name}” across ${taskDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} $year",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of days in this month
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(daysInMonth) { dayIndex ->
                        val dayNum = dayIndex + 1
                        val date = LocalDate.of(year, month, dayNum)
                        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val isSelected = selectedDates.value.contains(dateStr)

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    val current = selectedDates.value.toMutableSet()
                                    if (isSelected) current.remove(dateStr) else current.add(dateStr)
                                    selectedDates.value = current
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNum.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                                    fontSize = 8.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (selectedDates.value.isNotEmpty()) {
                                onConfirmCopy(selectedDates.value)
                            }
                        },
                        enabled = selectedDates.value.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Copy (${selectedDates.value.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun MoveTaskDatePicker(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val parsed = try { LocalDate.parse(currentDate) } catch (_: Exception) { LocalDate.now() }

    LaunchedEffect(Unit) {
        val dp = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(chosen.format(DateTimeFormatter.ISO_LOCAL_DATE))
            },
            parsed.year,
            parsed.monthValue - 1,
            parsed.dayOfMonth
        )
        dp.setOnDismissListener { onDismiss() }
        dp.show()
    }
}
