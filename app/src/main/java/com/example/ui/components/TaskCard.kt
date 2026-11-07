package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.getSubjectColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskEntity,
    onToggleDone: () -> Unit,
    onUpdate: (TaskEntity) -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val subjectColor = getSubjectColor(task.subject)
    val cardBg = if (task.done) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (task.done) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Subject color bar indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(subjectColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Completion Checkbox
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("task_done_${task.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (task.done) BrandGreen else Color.Transparent)
                        .border(
                            2.dp,
                            if (task.done) BrandGreen else subjectColor,
                            RoundedCornerShape(7.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.done) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Main info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.done) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Badges row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (task.subject.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = subjectColor.copy(alpha = 0.15f),
                            contentColor = subjectColor
                        ) {
                            Text(
                                text = task.subject,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Priority Badge
                    val priorityColor = when (task.priority) {
                        "High" -> BrandRed
                        "Low" -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.15f),
                        contentColor = priorityColor
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Revision Badge
                    if (task.revision) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (task.revised) BrandGreen.copy(alpha = 0.15f) else Color(0xFFFFB800).copy(alpha = 0.15f),
                            contentColor = if (task.revised) BrandGreen else Color(0xFFFFB800)
                        ) {
                            Text(
                                text = if (task.revised) "Revised ✓" else "Revise",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (task.notes.isNotBlank() && !expanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Actions Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("task_menu_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Copy to dates...") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onCopy()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to date...") },
                        leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onMove()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = BrandRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = BrandRed) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }

        // Expanded inline editor
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = spring()) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring()) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                var editName by remember(task.name) { mutableStateOf(task.name) }
                var editSubject by remember(task.subject) { mutableStateOf(task.subject) }
                var editPriority by remember(task.priority) { mutableStateOf(task.priority) }
                var editNotes by remember(task.notes) { mutableStateOf(task.notes) }
                var editRevision by remember(task.revision) { mutableStateOf(task.revision) }
                var editRevised by remember(task.revised) { mutableStateOf(task.revised) }

                OutlinedTextField(
                    value = editName,
                    onValueChange = {
                        editName = it
                        onUpdate(task.copy(name = it))
                    },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = {
                            editSubject = it
                            onUpdate(task.copy(subject = it))
                        },
                        label = { Text("Subject") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Priority selector chips
                    Row(
                        modifier = Modifier
                            .weight(1.2f)
                            .align(Alignment.CenterVertically),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Low", "Normal", "High").forEach { p ->
                            FilterChip(
                                selected = editPriority == p,
                                onClick = {
                                    editPriority = p
                                    onUpdate(task.copy(priority = p))
                                },
                                label = { Text(p, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Revision Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val next = !editRevision
                            editRevision = next
                            onUpdate(task.copy(revision = next))
                        }
                    ) {
                        Checkbox(
                            checked = editRevision,
                            onCheckedChange = {
                                editRevision = it
                                onUpdate(task.copy(revision = it))
                            }
                        )
                        Text("Needs revision", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (editRevision) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val next = !editRevised
                                editRevised = next
                                onUpdate(task.copy(revised = next))
                            }
                        ) {
                            Checkbox(
                                checked = editRevised,
                                onCheckedChange = {
                                    editRevised = it
                                    onUpdate(task.copy(revised = it))
                                }
                            )
                            Text("Revised ✓", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editNotes,
                    onValueChange = {
                        editNotes = it
                        onUpdate(task.copy(notes = it))
                    },
                    label = { Text("Notes & details") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4
                )
            }
        }
    }
}
