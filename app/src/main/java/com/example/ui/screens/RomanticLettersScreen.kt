package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LoveLetterEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.ItemActionRow
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop
import com.example.ui.theme.LocalReadingFontScale

@Composable
fun RomanticLettersScreen(
    darkMode: Boolean,
    letters: List<LoveLetterEntity>,
    onBack: () -> Unit,
    onSaveLetter: (LoveLetterEntity) -> Unit,
    onDeleteLetter: (LoveLetterEntity) -> Unit,
    onToggleFavorite: (LoveLetterEntity) -> Unit
) {
    var favoritesOnly by remember { mutableStateOf(false) }
    var readingLetter by remember { mutableStateOf<LoveLetterEntity?>(null) }
    var editingLetter by remember { mutableStateOf<LoveLetterEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingLetter by remember { mutableStateOf<LoveLetterEntity?>(null) }

    val displayed = remember(letters, favoritesOnly) {
        if (favoritesOnly) letters.filter { it.isFavorite } else letters
    }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                KeepsakeTopBar(
                    title = "Romantic Letters",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { favoritesOnly = !favoritesOnly }) {
                            Icon(
                                imageVector = if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Filter favorite letters",
                                tint = if (favoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingLetter = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_letter_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Write new letter")
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
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !favoritesOnly,
                        onClick = { favoritesOnly = false },
                        label = { Text("All Letters (${letters.size})") }
                    )
                    FilterChip(
                        selected = favoritesOnly,
                        onClick = { favoritesOnly = true },
                        label = { Text("Favorites (${letters.count { it.isFavorite }})") }
                    )
                }

                if (displayed.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyKeepsakeState(
                            title = if (favoritesOnly) "No Favorite Letters Yet" else "Write Your First Love Letter",
                            subtitle = "Letters written from the heart stay preserved offline forever.",
                            actionLabel = "Write a Letter",
                            onAction = {
                                editingLetter = null
                                showEditor = true
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(displayed, key = { it.id }) { letter ->
                            StationeryLetterCard(
                                letter = letter,
                                onOpen = { readingLetter = letter },
                                onToggleFavorite = { onToggleFavorite(letter) },
                                onEdit = {
                                    editingLetter = letter
                                    showEditor = true
                                },
                                onDelete = { deletingLetter = letter }
                            )
                        }
                    }
                }
            }
        }
    }

    readingLetter?.let { letter ->
        LetterReaderDialog(
            letter = letter,
            onDismiss = { readingLetter = null }
        )
    }

    if (showEditor) {
        LetterEditorDialog(
            existing = editingLetter,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                onSaveLetter(saved)
                showEditor = false
            }
        )
    }

    deletingLetter?.let { letter ->
        DeleteConfirmDialog(
            title = "Delete Love Letter?",
            message = "Delete \"${letter.title}\" permanently from local storage?",
            onConfirm = {
                onDeleteLetter(letter)
                deletingLetter = null
            },
            onDismiss = { deletingLetter = null }
        )
    }
}

@Composable
private fun StationeryLetterCard(
    letter: LoveLetterEntity,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
        shadowElevation = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.img_romantic_parchment),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(MaterialTheme.shapes.large)
                    .alpha(0.22f)
            )
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = letter.dateLabel.ifBlank { "Undated" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ItemActionRow(
                        isFavorite = letter.isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
                Text(
                    text = letter.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "From ${letter.sender}  •  To ${letter.recipient}",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = letter.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to read full letter →",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LetterReaderDialog(
    letter: LoveLetterEntity,
    onDismiss: () -> Unit
) {
    val fontScale = LocalReadingFontScale.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(letter.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "From ${letter.sender} to ${letter.recipient} • ${letter.dateLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                Text(
                    text = letter.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (16 * fontScale).sp,
                        lineHeight = (26 * fontScale).sp
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun LetterEditorDialog(
    existing: LoveLetterEntity?,
    onDismiss: () -> Unit,
    onSave: (LoveLetterEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var sender by remember { mutableStateOf(existing?.sender ?: "Miyong Bangot") }
    var recipient by remember { mutableStateOf(existing?.recipient ?: "Juliana") }
    var dateLabel by remember { mutableStateOf(existing?.dateLabel.orEmpty()) }
    var content by remember { mutableStateOf(existing?.content.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Write Love Letter" else "Edit Love Letter") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Letter Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("letter_title_input")
                )
                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text("From") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("To") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text("Date (e.g., 2026-11-14)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Letter Message") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("letter_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entity = (existing ?: LoveLetterEntity(
                        title = title.trim(),
                        sender = sender.trim(),
                        recipient = recipient.trim(),
                        dateLabel = dateLabel.trim(),
                        content = content.trim()
                    )).copy(
                        title = title.trim(),
                        sender = sender.trim().ifBlank { "Miyong Bangot" },
                        recipient = recipient.trim().ifBlank { "Juliana" },
                        dateLabel = dateLabel.trim(),
                        content = content.trim()
                    )
                    onSave(entity)
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.testTag("save_letter_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
