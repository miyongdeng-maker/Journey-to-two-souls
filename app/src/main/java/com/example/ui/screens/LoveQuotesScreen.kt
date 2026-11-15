package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.data.LoveQuoteEntity
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.ItemActionRow
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop
import kotlinx.coroutines.launch

@Composable
fun LoveQuotesScreen(
    darkMode: Boolean,
    quotes: List<LoveQuoteEntity>,
    dailyQuote: LoveQuoteEntity?,
    onBack: () -> Unit,
    onSaveQuote: (LoveQuoteEntity) -> Unit,
    onDeleteQuote: (LoveQuoteEntity) -> Unit,
    onToggleFavorite: (LoveQuoteEntity) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var favoritesOnly by remember { mutableStateOf(false) }
    var editingQuote by remember { mutableStateOf<LoveQuoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deletingQuote by remember { mutableStateOf<LoveQuoteEntity?>(null) }

    val displayed = remember(quotes, favoritesOnly) {
        if (favoritesOnly) quotes.filter { it.isFavorite } else quotes
    }

    fun copyQuote(q: LoveQuoteEntity) {
        val formatted = "\"${q.quoteText}\" — ${q.author}"
        clipboard.setText(AnnotatedString(formatted))
        scope.launch { snackbarHostState.showSnackbar("Quote copied to clipboard") }
    }

    fun shareQuote(q: LoveQuoteEntity) {
        val formatted = "\"${q.quoteText}\" — ${q.author}\n\nShared from Journey of Two Souls"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, formatted)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Share Love Quote"))
        } catch (_: Exception) {
            scope.launch { snackbarHostState.showSnackbar("No sharing app available") }
        }
    }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                KeepsakeTopBar(
                    title = "Love Quotes",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { favoritesOnly = !favoritesOnly }) {
                            Icon(
                                imageVector = if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Filter favorite quotes",
                                tint = if (favoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingQuote = null
                        showEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_quote_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add personal quote")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (dailyQuote != null) {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.FormatQuote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DAILY LOVE QUOTE",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "\"${dailyQuote.quoteText}\"",
                                    style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "— ${dailyQuote.author}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Row {
                                        IconButton(onClick = { copyQuote(dailyQuote) }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy daily quote")
                                        }
                                        IconButton(onClick = { shareQuote(dailyQuote) }) {
                                            Icon(Icons.Default.Share, contentDescription = "Share daily quote")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !favoritesOnly,
                            onClick = { favoritesOnly = false },
                            label = { Text("All (${quotes.size})") }
                        )
                        FilterChip(
                            selected = favoritesOnly,
                            onClick = { favoritesOnly = true },
                            label = { Text("Favorites (${quotes.count { it.isFavorite }})") }
                        )
                    }
                }

                if (displayed.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            EmptyKeepsakeState(
                                title = "No Quotes Found",
                                subtitle = "Add personal quotes or lines that remind you of each other.",
                                actionLabel = "Add Quote",
                                onAction = {
                                    editingQuote = null
                                    showEditor = true
                                }
                            )
                        }
                    }
                } else {
                    items(displayed, key = { it.id }) { quote ->
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            tonalElevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "\"${quote.quoteText}\"",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "— ${quote.author}${if (quote.isPersonal) " • Personal" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                ItemActionRow(
                                    isFavorite = quote.isFavorite,
                                    onToggleFavorite = { onToggleFavorite(quote) },
                                    onEdit = {
                                        editingQuote = quote
                                        showEditor = true
                                    },
                                    onDelete = { deletingQuote = quote },
                                    extraActions = {
                                        IconButton(onClick = { copyQuote(quote) }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy quote")
                                        }
                                        IconButton(onClick = { shareQuote(quote) }) {
                                            Icon(Icons.Default.Share, contentDescription = "Share quote")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        QuoteEditorDialog(
            existing = editingQuote,
            onDismiss = { showEditor = false },
            onSave = { saved ->
                onSaveQuote(saved)
                showEditor = false
            }
        )
    }

    deletingQuote?.let { q ->
        DeleteConfirmDialog(
            title = "Delete Quote?",
            message = "Remove this quote from your collection?",
            onConfirm = {
                onDeleteQuote(q)
                deletingQuote = null
            },
            onDismiss = { deletingQuote = null }
        )
    }
}

@Composable
private fun QuoteEditorDialog(
    existing: LoveQuoteEntity?,
    onDismiss: () -> Unit,
    onSave: (LoveQuoteEntity) -> Unit
) {
    var text by remember { mutableStateOf(existing?.quoteText.orEmpty()) }
    var author by remember { mutableStateOf(existing?.author ?: "Miyong & Juliana") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add Personal Quote" else "Edit Quote") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Quote") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("quote_text_input")
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author / Spoken By") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entity = (existing ?: LoveQuoteEntity(
                        quoteText = text.trim(),
                        author = author.trim(),
                        isPersonal = true
                    )).copy(
                        quoteText = text.trim(),
                        author = author.trim().ifBlank { "Miyong & Juliana" }
                    )
                    onSave(entity)
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.testTag("save_quote_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
