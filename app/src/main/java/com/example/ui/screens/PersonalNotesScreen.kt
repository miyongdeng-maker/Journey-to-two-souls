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
import com.example.data.PersonalNoteEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.ItemActionRow
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop

private val NOTE_MOODS = listOf("Tender", "Grateful", "Playful", "Missing You", "Proud of Us")

@Composable
fun PersonalNotesScreen(
    darkMode: Boolean,
    notes: List<PersonalNoteEntity>,
    onBack: () -> Unit,
    onSaveNote: (PersonalNoteEntity) -> Unit,
    onDeleteNote: (PersonalNoteEntity) -> Unit,
    onToggleFavorite: (PersonalNoteEntity) -> Unit
) {
    var editingNote by remember { mutableStateOf<PersonalNoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingNote by remember { mutableStateOf<PersonalNoteEntity?>(null) }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KeepsakeTopBar(title = "Personal Love Notes", onBack = onBack) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingNote = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_note_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add love note")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyKeepsakeState(
                        title = "Leave a Sweet Note",
                        subtitle = "Jot down quiet thoughts, little reminders, or affectionate whispers for each other.",
                        actionLabel = "Write Note",
                        onAction = {
                            editingNote = null
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
                    items(notes, key = { it.id }) { note ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
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
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = "${note.mood} • ${note.author}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    ItemActionRow(
                                        isFavorite = note.isFavorite,
                                        onToggleFavorite = { onToggleFavorite(note) },
                                        onEdit = {
                                            editingNote = note
                                            showEditor = true
                                        },
                                        onDelete = { deletingNote = note }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(note.title, style = MaterialTheme.typography.titleLarge)
                                if (note.dateLabel.isNotBlank()) {
                                    Text(
                                        text = note.dateLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(note.body, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        NoteEditorDialog(
            existing = editingNote,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                onSaveNote(saved)
                showEditor = false
            }
        )
    }

    deletingNote?.let { n ->
        DeleteConfirmDialog(
            title = "Delete Love Note?",
            message = "Remove \"${n.title}\"?",
            onConfirm = {
                onDeleteNote(n)
                deletingNote = null
            },
            onDismiss = { deletingNote = null }
        )
    }
}

@Composable
private fun NoteEditorDialog(
    existing: PersonalNoteEntity?,
    onDismiss: () -> Unit,
    onSave: (PersonalNoteEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var body by remember { mutableStateOf(existing?.body.orEmpty()) }
    var author by remember { mutableStateOf(existing?.author ?: "Miyong Bangot") }
    var mood by remember { mutableStateOf(existing?.mood ?: "Tender") }
    var dateLabel by remember { mutableStateOf(existing?.dateLabel.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Personal Love Note" else "Edit Love Note") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(NOTE_MOODS) { m ->
                        FilterChip(
                            selected = mood == m,
                            onClick = { mood = m },
                            label = { Text(m) }
                        )
                    }
                }
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("From") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text("Date (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Your Note") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("note_body_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entity = (existing ?: PersonalNoteEntity(
                        title = title.trim(),
                        body = body.trim(),
                        author = author.trim(),
                        mood = mood,
                        dateLabel = dateLabel.trim()
                    )).copy(
                        title = title.trim(),
                        body = body.trim(),
                        author = author.trim().ifBlank { "Miyong Bangot" },
                        mood = mood,
                        dateLabel = dateLabel.trim()
                    )
                    onSave(entity)
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
