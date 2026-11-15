package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.data.MemoryEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.ItemActionRow
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.LocalPhotoImage
import com.example.ui.components.SunsetBackdrop

@Composable
fun OurMemoriesScreen(
    darkMode: Boolean,
    memories: List<MemoryEntity>,
    onBack: () -> Unit,
    onSaveMemory: (
        existing: MemoryEntity?,
        title: String,
        description: String,
        dateLabel: String,
        specialNotes: String,
        newPhotoUri: Uri?,
        removePhoto: Boolean
    ) -> Unit,
    onDeleteMemory: (MemoryEntity) -> Unit,
    onToggleFavorite: (MemoryEntity) -> Unit
) {
    var editingMemory by remember { mutableStateOf<MemoryEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingMemory by remember { mutableStateOf<MemoryEntity?>(null) }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KeepsakeTopBar(title = "Our Memories", onBack = onBack) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingMemory = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_memory_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add memory")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            if (memories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyKeepsakeState(
                        title = "Preserve a Memory",
                        subtitle = "Save photos, places, and special notes completely offline.",
                        actionLabel = "Add First Memory",
                        onAction = {
                            editingMemory = null
                            showEditor = true
                        }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(memories, key = { it.id }) { memory ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            tonalElevation = 4.dp,
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (memory.photoPath.isNotBlank()) {
                                    LocalPhotoImage(
                                        photoPath = memory.photoPath,
                                        contentDescription = memory.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = memory.dateLabel.ifBlank { "Undated memory" },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    ItemActionRow(
                                        isFavorite = memory.isFavorite,
                                        onToggleFavorite = { onToggleFavorite(memory) },
                                        onEdit = {
                                            editingMemory = memory
                                            showEditor = true
                                        },
                                        onDelete = { deletingMemory = memory }
                                    )
                                }

                                Text(
                                    text = memory.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = memory.description,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (memory.specialNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    ) {
                                        Text(
                                            text = "Special note: ${memory.specialNotes}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                            modifier = Modifier.padding(10.dp)
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

    if (showEditor) {
        MemoryEditorDialog(
            existing = editingMemory,
            onDismiss = { showEditor = false },
            onSave = { title, desc, date, notes, uri, removePhoto ->
                onSaveMemory(editingMemory, title, desc, date, notes, uri, removePhoto)
                showEditor = false
            }
        )
    }

    deletingMemory?.let { mem ->
        DeleteConfirmDialog(
            title = "Delete Memory?",
            message = "Remove \"${mem.title}\" and its saved copy from Our Memories?",
            onConfirm = {
                onDeleteMemory(mem)
                deletingMemory = null
            },
            onDismiss = { deletingMemory = null }
        )
    }
}

@Composable
private fun MemoryEditorDialog(
    existing: MemoryEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Uri?, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    var dateLabel by remember { mutableStateOf(existing?.dateLabel.orEmpty()) }
    var specialNotes by remember { mutableStateOf(existing?.specialNotes.orEmpty()) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var removeExistingPhoto by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedUri = uri
            removeExistingPhoto = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Memory" else "Edit Memory") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Memory Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("memory_title_input")
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
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("memory_desc_input")
                )
                OutlinedTextField(
                    value = specialNotes,
                    onValueChange = { specialNotes = it },
                    label = { Text("Special Notes (optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when {
                                pickedUri != null -> "Photo Selected"
                                !existing?.photoPath.isNullOrBlank() && !removeExistingPhoto -> "Change Photo"
                                else -> "Attach Photo"
                            }
                        )
                    }
                    if ((!existing?.photoPath.isNullOrBlank() && !removeExistingPhoto) || pickedUri != null) {
                        TextButton(onClick = {
                            pickedUri = null
                            removeExistingPhoto = true
                        }) {
                            Text("Remove")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title.trim(),
                        description.trim(),
                        dateLabel.trim(),
                        specialNotes.trim(),
                        pickedUri,
                        removeExistingPhoto
                    )
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.testTag("save_memory_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
