package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.GalleryPhotoEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.LocalPhotoImage
import com.example.ui.components.SunsetBackdrop
import com.example.util.DateHelper

@Composable
fun PhotoGalleryScreen(
    darkMode: Boolean,
    photos: List<GalleryPhotoEntity>,
    onBack: () -> Unit,
    onImportPhoto: (Uri, String, String, String) -> Unit,
    onUpdatePhoto: (GalleryPhotoEntity) -> Unit,
    onDeletePhoto: (GalleryPhotoEntity) -> Unit,
    onToggleFavorite: (GalleryPhotoEntity) -> Unit
) {
    var pendingPickedUri by remember { mutableStateOf<Uri?>(null) }
    var viewingPhoto by remember { mutableStateOf<GalleryPhotoEntity?>(null) }
    var editingPhoto by remember { mutableStateOf<GalleryPhotoEntity?>(null) }
    var deletingPhoto by remember { mutableStateOf<GalleryPhotoEntity?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingPickedUri = uri
        }
    }

    fun launchPicker() {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { KeepsakeTopBar(title = "Photo Gallery", onBack = onBack) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = ::launchPicker,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("import_photo_fab")
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Import photo from device")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyKeepsakeState(
                        title = "Your Private Photo Album",
                        subtitle = "Import photographs from your phone to store them securely offline inside your keepsake.",
                        actionLabel = "Import Photo",
                        onAction = ::launchPicker
                    )
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    val cols = if (maxWidth >= 680.dp) 3 else 2
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(cols),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                tonalElevation = 3.dp,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewingPhoto = photo }
                            ) {
                                Column {
                                    Box {
                                        LocalPhotoImage(
                                            photoPath = photo.photoPath,
                                            contentDescription = photo.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                        )
                                        IconButton(
                                            onClick = { onToggleFavorite(photo) },
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Icon(
                                                imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite photo",
                                                tint = if (photo.isFavorite) MaterialTheme.colorScheme.primary else Color.White
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = photo.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = photo.dateLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    pendingPickedUri?.let { uri ->
        ImportPhotoDetailsDialog(
            onDismiss = { pendingPickedUri = null },
            onSave = { title, caption, dateLabel ->
                onImportPhoto(uri, title, caption, dateLabel)
                pendingPickedUri = null
            }
        )
    }

    viewingPhoto?.let { photo ->
        AlertDialog(
            onDismissRequest = { viewingPhoto = null },
            title = { Text(photo.title, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    LocalPhotoImage(
                        photoPath = photo.photoPath,
                        contentDescription = photo.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = photo.dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (photo.caption.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(photo.caption, style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            editingPhoto = photo
                            viewingPhoto = null
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit photo details")
                        }
                        IconButton(onClick = {
                            deletingPhoto = photo
                            viewingPhoto = null
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete photo",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingPhoto = null }) { Text("Close") }
            }
        )
    }

    editingPhoto?.let { photo ->
        EditPhotoDetailsDialog(
            photo = photo,
            onDismiss = { editingPhoto = null },
            onSave = { updated ->
                onUpdatePhoto(updated)
                editingPhoto = null
            }
        )
    }

    deletingPhoto?.let { photo ->
        DeleteConfirmDialog(
            title = "Delete Photo?",
            message = "Remove \"${photo.title}\" from your offline gallery?",
            onConfirm = {
                onDeletePhoto(photo)
                deletingPhoto = null
            },
            onDismiss = { deletingPhoto = null }
        )
    }
}

@Composable
private fun ImportPhotoDetailsDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var dateLabel by remember { mutableStateOf(DateHelper.formatDisplayDate(DateHelper.todayIso())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Photograph Offline") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Photo Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption (optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title.trim(), caption.trim(), dateLabel.trim()) }) {
                Text("Save to Gallery")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditPhotoDetailsDialog(
    photo: GalleryPhotoEntity,
    onDismiss: () -> Unit,
    onSave: (GalleryPhotoEntity) -> Unit
) {
    var title by remember { mutableStateOf(photo.title) }
    var caption by remember { mutableStateOf(photo.caption) }
    var dateLabel by remember { mutableStateOf(photo.dateLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Photo Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        photo.copy(
                            title = title.trim().ifBlank { "Cherished Moment" },
                            caption = caption.trim(),
                            dateLabel = dateLabel.trim()
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
