package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.SpecialMomentEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.ItemActionRow
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop

private val MOMENT_CATEGORIES = listOf("All", "Anniversary", "Birthday", "Conversation", "Celebration", "Milestone")

@Composable
fun SpecialMomentsScreen(
    darkMode: Boolean,
    moments: List<SpecialMomentEntity>,
    onBack: () -> Unit,
    onSaveMoment: (SpecialMomentEntity) -> Unit,
    onDeleteMoment: (SpecialMomentEntity) -> Unit,
    onToggleFavorite: (SpecialMomentEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var editingMoment by remember { mutableStateOf<SpecialMomentEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingMoment by remember { mutableStateOf<SpecialMomentEntity?>(null) }

    val filtered = remember(moments, selectedCategory) {
        if (selectedCategory == "All") moments else moments.filter { it.category == selectedCategory }
    }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KeepsakeTopBar(title = "Special Moments", onBack = onBack) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingMoment = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_moment_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add special moment")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MOMENT_CATEGORIES) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyKeepsakeState(
                            title = "No Moments in $selectedCategory",
                            subtitle = "Save anniversaries, birthdays, late-night talks, and joyful celebrations.",
                            actionLabel = "Add Special Moment",
                            onAction = {
                                editingMoment = null
                                showEditor = true
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { moment ->
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                tonalElevation = 3.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = "${moment.category} • ${moment.dateLabel}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        ItemActionRow(
                                            isFavorite = moment.isFavorite,
                                            onToggleFavorite = { onToggleFavorite(moment) },
                                            onEdit = {
                                                editingMoment = moment
                                                showEditor = true
                                            },
                                            onDelete = { deletingMoment = moment }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(moment.title, style = MaterialTheme.typography.titleLarge)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(moment.description, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        MomentEditorDialog(
            existing = editingMoment,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                onSaveMoment(saved)
                showEditor = false
            }
        )
    }

    deletingMoment?.let { m ->
        DeleteConfirmDialog(
            title = "Delete Special Moment?",
            message = "Remove \"${m.title}\"?",
            onConfirm = {
                onDeleteMoment(m)
                deletingMoment = null
            },
            onDismiss = { deletingMoment = null }
        )
    }
}

@Composable
private fun MomentEditorDialog(
    existing: SpecialMomentEntity?,
    onDismiss: () -> Unit,
    onSave: (SpecialMomentEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var category by remember { mutableStateOf(existing?.category ?: "Anniversary") }
    var dateLabel by remember { mutableStateOf(existing?.dateLabel.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Special Moment" else "Edit Special Moment") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Moment Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("moment_title_input")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MOMENT_CATEGORIES.drop(1)) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
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
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("moment_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entity = (existing ?: SpecialMomentEntity(
                        title = title.trim(),
                        category = category,
                        dateLabel = dateLabel.trim(),
                        description = description.trim()
                    )).copy(
                        title = title.trim(),
                        category = category,
                        dateLabel = dateLabel.trim(),
                        description = description.trim()
                    )
                    onSave(entity)
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.testTag("save_moment_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
