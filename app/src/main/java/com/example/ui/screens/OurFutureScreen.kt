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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.FutureDreamEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.ItemActionRow
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop

private val DREAM_CATEGORIES = listOf("All", "Marriage", "Travel", "Home", "Adventure", "Life Goal")

@Composable
fun OurFutureScreen(
    darkMode: Boolean,
    dreams: List<FutureDreamEntity>,
    onBack: () -> Unit,
    onSaveDream: (FutureDreamEntity) -> Unit,
    onDeleteDream: (FutureDreamEntity) -> Unit,
    onToggleAchieved: (FutureDreamEntity) -> Unit,
    onToggleFavorite: (FutureDreamEntity) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var editingDream by remember { mutableStateOf<FutureDreamEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingDream by remember { mutableStateOf<FutureDreamEntity?>(null) }

    val filtered = remember(dreams, selectedCategory) {
        if (selectedCategory == "All") dreams else dreams.filter { it.category == selectedCategory }
    }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KeepsakeTopBar(title = "Our Future Together", onBack = onBack) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingDream = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_dream_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add dream")
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
                    items(DREAM_CATEGORIES) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyKeepsakeState(
                            title = "Dream Together",
                            subtitle = "Save marriage plans, travel destinations, future home ideas, and shared goals.",
                            actionLabel = "Add a Dream",
                            onAction = {
                                editingDream = null
                                showEditor = true
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { dream ->
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = dream.isAchieved,
                                                onCheckedChange = { onToggleAchieved(dream) }
                                            )
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.tertiaryContainer
                                            ) {
                                                Text(
                                                    text = if (dream.isAchieved) "${dream.category} • Fulfilled" else dream.category,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        ItemActionRow(
                                            isFavorite = dream.isFavorite,
                                            onToggleFavorite = { onToggleFavorite(dream) },
                                            onEdit = {
                                                editingDream = dream
                                                showEditor = true
                                            },
                                            onDelete = { deletingDream = dream }
                                        )
                                    }
                                    Text(
                                        text = dream.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            textDecoration = if (dream.isAchieved) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    )
                                    if (dream.targetTimeframe.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Timeframe: ${dream.targetTimeframe}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (dream.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(dream.notes, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        DreamEditorDialog(
            existing = editingDream,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                onSaveDream(saved)
                showEditor = false
            }
        )
    }

    deletingDream?.let { d ->
        DeleteConfirmDialog(
            title = "Delete Dream?",
            message = "Remove \"${d.title}\" from Our Future Together?",
            onConfirm = {
                onDeleteDream(d)
                deletingDream = null
            },
            onDismiss = { deletingDream = null }
        )
    }
}

@Composable
private fun DreamEditorDialog(
    existing: FutureDreamEntity?,
    onDismiss: () -> Unit,
    onSave: (FutureDreamEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var category by remember { mutableStateOf(existing?.category ?: "Travel") }
    var timeframe by remember { mutableStateOf(existing?.targetTimeframe.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Dream Together" else "Edit Dream") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Dream / Goal Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dream_title_input")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DREAM_CATEGORIES.drop(1)) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = timeframe,
                    onValueChange = { timeframe = it },
                    label = { Text("Target Timeframe (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Details & Wishes") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("dream_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entity = (existing ?: FutureDreamEntity(
                        title = title.trim(),
                        category = category,
                        targetTimeframe = timeframe.trim(),
                        notes = notes.trim()
                    )).copy(
                        title = title.trim(),
                        category = category,
                        targetTimeframe = timeframe.trim(),
                        notes = notes.trim()
                    )
                    onSave(entity)
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_dream_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
