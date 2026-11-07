package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.ActivityHeatmap
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val bestStreak by viewModel.bestStreak.collectAsState()
    val subjectStats by viewModel.subjectStatsList.collectAsState()
    val heatmapItems by viewModel.heatmap14Days.collectAsState()

    val totalTasks = allTasks.size
    val completedTasks = allTasks.count { it.done }
    val completionRate = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0
    val studyDaysCount = allTasks.filter { it.done }.map { it.date }.toSet().size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Deep dive into your study metrics & productivity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4 Summary Metrics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Tasks",
                        value = totalTasks.toString(),
                        subtitle = "all tracked items",
                        gradient = PrimaryGradient,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completion Rate",
                        value = "$completionRate%",
                        subtitle = "$completedTasks completed",
                        gradient = CyanBlueGradient,
                        progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Best Streak",
                        value = "🏆 $bestStreak",
                        subtitle = "days in a row",
                        gradient = OrangeGoldGradient,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Study Days",
                        value = studyDaysCount.toString(),
                        subtitle = "active days logged",
                        gradient = BluePurpleGradient,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 14-Day Activity Heatmap
        item {
            ActivityHeatmap(
                items = heatmapItems,
                onSelectDay = { viewModel.selectDate(it) }
            )
        }

        // Subject Performance Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Subject Performance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (subjectStats.isEmpty()) {
                        Text(
                            text = "No tasks or subjects recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            subjectStats.forEach { stat ->
                                val subjColor = getSubjectColor(if (stat.subject == "No subject") "" else stat.subject)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(subjColor)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stat.subject,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Text(
                                            text = "${stat.completedTasks}/${stat.totalTasks} (${stat.progressPct}%)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val animatedPct by animateFloatAsState(
                                        targetValue = stat.progressPct / 100f,
                                        animationSpec = tween(600),
                                        label = "subjPct"
                                    )

                                    LinearProgressIndicator(
                                        progress = { animatedPct },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = subjColor,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
