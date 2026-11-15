package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoryChapterEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop
import com.example.ui.theme.LocalReadingFontScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OurLoveStoryScreen(
    darkMode: Boolean,
    chapters: List<StoryChapterEntity>,
    onBack: () -> Unit,
    onSaveChapter: (StoryChapterEntity) -> Unit,
    onDeleteChapter: (StoryChapterEntity) -> Unit,
    onToggleFavorite: (StoryChapterEntity) -> Unit
) {
    val fontScale = LocalReadingFontScale.current
    var currentIndex by remember(chapters.size) { mutableIntStateOf(0) }
    val safeIndex = currentIndex.coerceIn(0, (chapters.size - 1).coerceAtLeast(0))
    val currentChapter = chapters.getOrNull(safeIndex)

    var showChapterIndexSheet by remember { mutableStateOf(false) }
    var editingChapter by remember { mutableStateOf<StoryChapterEntity?>(null) }
    var showEditorDialog by remember { mutableStateOf(false) }
    var deletingChapter by remember { mutableStateOf<StoryChapterEntity?>(null) }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                KeepsakeTopBar(
                    title = "Our Love Story",
                    onBack = onBack,
                    actions = {
                        if (chapters.isNotEmpty()) {
                            IconButton(
                                onClick = { showChapterIndexSheet = true },
                                modifier = Modifier.testTag("story_index_button")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = "Chapters index"
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingChapter = null
                        showEditorDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_chapter_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add chapter")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            if (currentChapter == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyKeepsakeState(
                        title = "Your Book Awaits Its First Page",
                        subtitle = "Write the story of Miyong and Juliana chapter by chapter.",
                        actionLabel = "Write Chapter 1",
                        onAction = {
                            editingChapter = null
                            showEditorDialog = true
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Book page card
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                        ),
                        shadowElevation = 8.dp,
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CHAPTER ${currentChapter.chapterNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Row {
                                    IconButton(onClick = { onToggleFavorite(currentChapter) }) {
                                        Icon(
                                            imageVector = if (currentChapter.isFavorite) {
                                                Icons.Default.Favorite
                                            } else {
                                                Icons.Default.FavoriteBorder
                                            },
                                            contentDescription = "Favorite chapter",
                                            tint = if (currentChapter.isFavorite) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            editingChapter = currentChapter
                                            showEditorDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit chapter")
                                    }
                                    IconButton(onClick = { deletingChapter = currentChapter }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete chapter",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            Text(
                                text = currentChapter.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentChapter.dateLabel.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentChapter.dateLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontStyle = FontStyle.Italic
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                            )
                            Text(
                                text = currentChapter.content,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = (17 * fontScale).sp,
                                    lineHeight = (28 * fontScale).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Book Page Navigation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (safeIndex > 0) currentIndex = safeIndex - 1 },
                            enabled = safeIndex > 0,
                            modifier = Modifier.testTag("prev_chapter_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }

                        Text(
                            text = "Page ${safeIndex + 1} of ${chapters.size}",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )

                        OutlinedButton(
                            onClick = { if (safeIndex < chapters.lastIndex) currentIndex = safeIndex + 1 },
                            enabled = safeIndex < chapters.lastIndex,
                            modifier = Modifier.testTag("next_chapter_button")
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    if (showChapterIndexSheet) {
        ModalBottomSheet(onDismissRequest = { showChapterIndexSheet = false }) {
            Text(
                text = "Table of Contents",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(chapters) { idx, ch ->
                    Surface(
                        color = if (idx == safeIndex) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        } else {
                            Color.Transparent
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentIndex = idx
                                showChapterIndexSheet = false
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Chapter ${ch.chapterNumber}: ${ch.title}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (ch.dateLabel.isNotBlank()) {
                                Text(
                                    text = ch.dateLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditorDialog) {
        ChapterEditorDialog(
            existing = editingChapter,
            defaultNumber = (chapters.maxOfOrNull { it.chapterNumber } ?: 0) + 1,
            onDismiss = { showEditorDialog = false },
            onSave = { saved ->
                onSaveChapter(saved)
                showEditorDialog = false
            }
        )
    }

    deletingChapter?.let { ch ->
        DeleteConfirmDialog(
            title = "Delete Chapter?",
            message = "Remove \"${ch.title}\" from Our Love Story?",
            onConfirm = {
                onDeleteChapter(ch)
                deletingChapter = null
            },
            onDismiss = { deletingChapter = null }
        )
    }
}

@Composable
private fun ChapterEditorDialog(
    existing: StoryChapterEntity?,
    defaultNumber: Int,
    onDismiss: () -> Unit,
    onSave: (StoryChapterEntity) -> Unit
) {
    var numberText by remember { mutableStateOf((existing?.chapterNumber ?: defaultNumber).toString()) }
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var dateLabel by remember { mutableStateOf(existing?.dateLabel.orEmpty()) }
    var content by remember { mutableStateOf(existing?.content.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Chapter" else "Edit Chapter") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { numberText = it.filter { c -> c.isDigit() } },
                    label = { Text("Chapter Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Chapter Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("chapter_title_input")
                )
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text("Date or Season (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Your Story") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("chapter_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = numberText.toIntOrNull() ?: defaultNumber
                    val entity = (existing ?: StoryChapterEntity(
                        chapterNumber = num,
                        title = title.trim(),
                        dateLabel = dateLabel.trim(),
                        content = content.trim()
                    )).copy(
                        chapterNumber = num,
                        title = title.trim().ifBlank { "Untitled Chapter" },
                        dateLabel = dateLabel.trim(),
                        content = content.trim()
                    )
                    onSave(entity)
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.testTag("save_chapter_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
