package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.example.data.PromiseEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.ItemActionRow
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop

private val PROMISE_CATEGORIES = listOf("Promise", "Commitment", "Relationship Value")

@Composable
fun PromisesScreen(
    darkMode: Boolean,
    promises: List<PromiseEntity>,
    onBack: () -> Unit,
    onSavePromise: (PromiseEntity) -> Unit,
    onDeletePromise: (PromiseEntity) -> Unit,
    onToggleFavorite: (PromiseEntity) -> Unit
) {
    var editingPromise by remember { mutableStateOf<PromiseEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingPromise by remember { mutableStateOf<PromiseEntity?>(null) }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KeepsakeTopBar(title = "Promises to Each Other", onBack = onBack) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingPromise = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_promise_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add promise")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            if (promises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyKeepsakeState(
                        title = "Write a Promise",
                        subtitle = "Preserve your vows, commitments, and shared relationship values.",
                        actionLabel = "Add Promise",
                        onAction = {
                            editingPromise = null
                            showEditor = true
                        }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(promises, key = { it.id }) { promise ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
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
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${promise.category} • ${promise.madeBy}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    ItemActionRow(
                                        isFavorite = promise.isFavorite,
                                        onToggleFavorite = { onToggleFavorite(promise) },
                                        onEdit = {
                                            editingPromise = promise
                                            showEditor = true
                                        },
                                        onDelete = { deletingPromise = promise }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(promise.title, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(promise.description, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        PromiseEditorDialog(
            existing = editingPromise,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                onSavePromise(saved)
                showEditor = false
            }
        )
    }

    deletingPromise?.let { p ->
        DeleteConfirmDialog(
            title = "Delete Promise?",
            message = "Remove \"${p.title}\"?",
            onConfirm = {
                onDeletePromise(p)
                deletingPromise = null
            },
            onDismiss = { deletingPromise = null }
        )
    }
}

@Composable
private fun PromiseEditorDialog(
    existing: PromiseEntity?,
    onDismiss: () -> Unit,
    onSave: (PromiseEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    var category by remember { mutableStateOf(existing?.category ?: "Promise") }
    var madeBy by remember { mutableStateOf(existing?.madeBy ?: "Miyong & Juliana") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Promise" else "Edit Promise") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Promise Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("promise_title_input")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(PROMISE_CATEGORIES) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = madeBy,
                    onValueChange = { madeBy = it },
                    label = { Text("Made By") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Promise Details") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("promise_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entity = (existing ?: PromiseEntity(
                        title = title.trim(),
                        description = description.trim(),
                        category = category,
                        madeBy = madeBy.trim()
                    )).copy(
                        title = title.trim(),
                        description = description.trim(),
                        category = category,
                        madeBy = madeBy.trim().ifBlank { "Miyong & Juliana" }
                    )
                    onSave(entity)
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.testTag("save_promise_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
