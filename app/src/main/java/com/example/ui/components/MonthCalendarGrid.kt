package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.getSubjectColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthCalendarGrid(
    currentMonthCursor: LocalDate,
    selectedDate: String,
    allTasks: List<TaskEntity>,
    onSelectDate: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = currentMonthCursor.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
            " " + currentMonthCursor.year

    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(36.dp).testTag("cal_prev_month")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onGoToToday,
                        modifier = Modifier.height(32.dp).testTag("cal_today_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(36.dp).testTag("cal_next_month")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Days of week header
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Days grid calculation
            val year = currentMonthCursor.year
            val month = currentMonthCursor.monthValue
            val firstDayOfMonth = LocalDate.of(year, month, 1)
            val firstDayOfWeekIndex = firstDayOfMonth.dayOfWeek.value % 7 // Sun=0, Mon=1, etc.
            val daysInMonth = firstDayOfMonth.lengthOfMonth()

            val totalCells = if (firstDayOfWeekIndex + daysInMonth > 35) 42 else 35
            val startDate = firstDayOfMonth.minusDays(firstDayOfWeekIndex.toLong())

            val rows = totalCells / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val date = startDate.plusDays(cellIndex.toLong())
                            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val isCurrentMonth = date.monthValue == month
                            val isSelected = dateString == selectedDate
                            val isToday = dateString == todayStr

                            val dayTasks = allTasks.filter { it.date == dateString }
                            val totalTasks = dayTasks.size
                            val completedTasks = dayTasks.count { it.done }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.95f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            !isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else if (isToday) 1.5.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectDate(dateString) }
                                    .padding(4.dp)
                                    .testTag("cal_cell_$dateString"),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // Day Number
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                            isToday -> MaterialTheme.colorScheme.primary
                                            !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontSize = 11.sp
                                    )

                                    // Subject dots (up to 5)
                                    if (totalTasks > 0) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        ) {
                                            dayTasks.take(5).forEach { task ->
                                                val dotColor = getSubjectColor(task.subject)
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .padding(0.5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (task.done) dotColor.copy(alpha = 0.4f) else dotColor)
                                                )
                                            }
                                        }

                                        // Done / Total badge
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = if (completedTasks == totalTasks) BrandGreen else MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = "$completedTasks/$totalTasks",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
