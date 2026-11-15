package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.TimelineMilestoneEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.ItemActionRow
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop

@Composable
fun JourneyTimelineScreen(
    darkMode: Boolean,
    milestones: List<TimelineMilestoneEntity>,
    onBack: () -> Unit,
    onSaveMilestone: (TimelineMilestoneEntity) -> Unit,
    onDeleteMilestone: (TimelineMilestoneEntity) -> Unit,
    onToggleFavorite: (TimelineMilestoneEntity) -> Unit
) {
    var editingMilestone by remember { mutableStateOf<TimelineMilestoneEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingMilestone by remember { mutableStateOf<TimelineMilestoneEntity?>(null) }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KeepsakeTopBar(title = "Our Journey Timeline", onBack = onBack) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingMilestone = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_milestone_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add milestone")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            if (milestones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyKeepsakeState(
                        title = "Begin Your Timeline",
                        subtitle = "Plot every milestone chronologically with heart-shaped markers.",
                        actionLabel = "Add Milestone",
                        onAction = {
                            editingMilestone = null
                            showEditor = true
                        }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(18.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    itemsIndexed(milestones, key = { _, m -> m.id }) { index, item ->
                        TimelineMilestoneRow(
                            milestone = item,
                            isFirst = index == 0,
                            isLast = index == milestones.lastIndex,
                            onToggleFavorite = { onToggleFavorite(item) },
                            onEdit = {
                                editingMilestone = item
                                showEditor = true
                            },
                            onDelete = { deletingMilestone = item }
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {
        MilestoneEditorDialog(
            existing = editingMilestone,
            defaultOrder = (milestones.maxOfOrNull { it.sortOrder } ?: 0) + 1,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                onSaveMilestone(saved)
                showEditor = false
            }
        )
    }

    deletingMilestone?.let { m ->
        DeleteConfirmDialog(
            title = "Delete Milestone?",
            message = "Remove \"${m.title}\" from Our Journey Timeline?",
            onConfirm = {
                onDeleteMilestone(m)
                deletingMilestone = null
            },
            onDismiss = { deletingMilestone = null }
        )
    }
}

@Composable
private fun TimelineMilestoneRow(
    milestone: TimelineMilestoneEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val lineColor = MaterialTheme.colorScheme.secondary
    val heartColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Canvas(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight()
        ) {
            val centerX = size.width / 2f
            val markerY = 34.dp.toPx()

            if (!isFirst) {
                drawLine(
                    color = lineColor.copy(alpha = 0.6f),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, markerY - 14.dp.toPx()),
                    strokeWidth = 3.dp.toPx()
                )
            }
            if (!isLast) {
                drawLine(
                    color = lineColor.copy(alpha = 0.6f),
                    start = Offset(centerX, markerY + 14.dp.toPx()),
                    end = Offset(centerX, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Heart-shaped marker
            val s = 12.dp.toPx()
            val heartPath = Path().apply {
                moveTo(centerX, markerY + s * 0.45f)
                cubicTo(
                    centerX - s * 1.1f, markerY - s * 0.25f,
                    centerX - s * 0.55f, markerY - s * 1.05f,
                    centerX, markerY - s * 0.35f
                )
                cubicTo(
                    centerX + s * 0.55f, markerY - s * 1.05f,
                    centerX + s * 1.1f, markerY - s * 0.25f,
                    centerX, markerY + s * 0.45f
                )
                close()
            }
            drawPath(heartPath, color = heartColor)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 3.dp,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = milestone.dateLabel.ifBlank { "Milestone #${milestone.sortOrder}" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ItemActionRow(
                        isFavorite = milestone.isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
                Text(milestone.title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Text(milestone.description, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun MilestoneEditorDialog(
    existing: TimelineMilestoneEntity?,
    defaultOrder: Int,
    onDismiss: () -> Unit,
    onSave: (TimelineMilestoneEntity) -> Unit
) {
    var orderText by remember { mutableStateOf((existing?.sortOrder ?: defaultOrder).toString()) }
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var dateLabel by remember { mutableStateOf(existing?.dateLabel.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Milestone" else "Edit Milestone") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = orderText,
                    onValueChange = { orderText = it.filter { c -> c.isDigit() } },
                    label = { Text("Chronological Order #") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Milestone Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("milestone_title_input")
                )
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("What Happened") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("milestone_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val order = orderText.toIntOrNull() ?: defaultOrder
                    val entity = (existing ?: TimelineMilestoneEntity(
                        title = title.trim(),
                        dateLabel = dateLabel.trim(),
                        description = description.trim(),
                        sortOrder = order
                    )).copy(
                        title = title.trim(),
                        dateLabel = dateLabel.trim(),
                        description = description.trim(),
                        sortOrder = order
                    )
                    onSave(entity)
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.testTag("save_milestone_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
