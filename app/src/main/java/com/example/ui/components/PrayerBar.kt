package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.prayer.PrayerInfo
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed

@Composable
fun PrayerBar(
    prayers: List<PrayerInfo>,
    onCycleStatus: (String) -> Unit,
    onSetTime: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRAYERS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val doneCount = prayers.count { it.status == "done" }
            Text(
                text = "$doneCount/5 done",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (doneCount == 5) BrandGreen else MaterialTheme.colorScheme.primary
            )
        }

        // Horizontal flowing row of prayer capsules
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            prayers.forEach { prayer ->
                val (bg, contentColor, borderColor) = when (prayer.status) {
                    "done" -> Triple(BrandGreen.copy(alpha = 0.2f), BrandGreen, BrandGreen)
                    "missed" -> Triple(BrandRed.copy(alpha = 0.15f), BrandRed, BrandRed.copy(alpha = 0.6f))
                    else -> Triple(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                }

                val animatedBg by animateColorAsState(targetValue = bg, label = "prayerBg")

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(animatedBg)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .testTag("prayer_${prayer.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top row with status toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCycleStatus(prayer.id) }
                                .padding(vertical = 2.dp)
                        ) {
                            when (prayer.status) {
                                "done" -> Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = contentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                "missed" -> Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Missed",
                                    tint = contentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                else -> Icon(
                                    imageVector = Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Not done",
                                    tint = contentColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = prayer.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = contentColor,
                                fontSize = 11.sp
                            )
                        }

                        // Time button for inline time pick
                        Text(
                            text = prayer.time,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    val parts = prayer.time.split(":")
                                    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
                                    val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    TimePickerDialog(
                                        context,
                                        { _, selectedHour, selectedMin ->
                                            val formatted = String.format("%02d:%02d", selectedHour, selectedMin)
                                            onSetTime(prayer.id, formatted)
                                        },
                                        hour,
                                        min,
                                        true
                                    ).show()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
