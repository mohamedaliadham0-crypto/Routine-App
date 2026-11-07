package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

val MANUAL_ITEMS = listOf(
    "Dashboard" to "Your study command center: today's progress, current streak, quick tasks, 14-day activity heatmap, and daily routine schedule.",
    "Calendar" to "Each day displays tiny colored subject dots and a completed/total counter badge. Tap any day to inspect and manage its tasks in real time.",
    "Editing Tasks" to "Every field on a task is editable directly from its card: name, date, priority, subject, revision tags, and detailed notes.",
    "Prayer Times" to "Five daily prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) computed using the Egyptian General Authority of Survey method for Alexandria. Tap the status to cycle Done → Missed → Reset, or tap the time to set custom times.",
    "Adding Tasks" to "Type in the quick add box and tap Add (or press Enter on keyboard) to quickly schedule tasks for the selected date.",
    "Copying Tasks" to "Open the task options menu (⋮) and choose 'Copy to dates...' to duplicate a task across any dates in the month.",
    "Moving Tasks" to "Open the task options menu (⋮) and choose 'Move to date...' to reschedule a task to another day with a single tap.",
    "Notes" to "Create unlimited study notes with titles and rich text content. Everything auto-saves instantly to local device storage.",
    "Subjects & Color Tagging" to "Assign subjects to your tasks; each subject automatically receives a consistent vibrant color accent throughout the app.",
    "Backup & Compatibility" to "Export and import JSON backups seamlessly. Routine StudyOS for Android uses the exact same backup structure as the web application!"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManualScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("manual_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "User Manual",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Complete guide to Routine StudyOS features",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        itemsIndexed(MANUAL_ITEMS) { index, (title, description) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${index + 1}. $title",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
