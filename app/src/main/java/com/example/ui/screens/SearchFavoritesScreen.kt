package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FutureDreamEntity
import com.example.data.GalleryPhotoEntity
import com.example.data.LoveLetterEntity
import com.example.data.LoveQuoteEntity
import com.example.data.MemoryEntity
import com.example.data.PersonalNoteEntity
import com.example.data.PromiseEntity
import com.example.data.SpecialMomentEntity
import com.example.data.StoryChapterEntity
import com.example.data.TimelineMilestoneEntity
import com.example.navigation.Routes
import com.example.ui.components.EmptyKeepsakeState
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.SunsetBackdrop
import com.example.viewmodel.SearchResultItem

@Composable
fun SearchFavoritesScreen(
    darkMode: Boolean,
    initialFavoritesOnly: Boolean,
    chapters: List<StoryChapterEntity>,
    letters: List<LoveLetterEntity>,
    quotes: List<LoveQuoteEntity>,
    memories: List<MemoryEntity>,
    moments: List<SpecialMomentEntity>,
    timeline: List<TimelineMilestoneEntity>,
    promises: List<PromiseEntity>,
    dreams: List<FutureDreamEntity>,
    photos: List<GalleryPhotoEntity>,
    notes: List<PersonalNoteEntity>,
    onBack: () -> Unit,
    onNavigateToSection: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var favoritesOnly by remember(initialFavoritesOnly) { mutableStateOf(initialFavoritesOnly) }

    val allItems = remember(
        chapters, letters, quotes, memories, moments, timeline, promises, dreams, photos, notes
    ) {
        buildList {
            chapters.forEach {
                add(SearchResultItem("ch_${it.id}", "Our Love Story", Routes.STORY, it.title, it.content, it.isFavorite))
            }
            letters.forEach {
                add(SearchResultItem("lt_${it.id}", "Romantic Letters", Routes.LETTERS, it.title, it.content, it.isFavorite))
            }
            quotes.forEach {
                add(SearchResultItem("qt_${it.id}", "Love Quotes", Routes.QUOTES, it.author, it.quoteText, it.isFavorite))
            }
            memories.forEach {
                add(SearchResultItem("mm_${it.id}", "Our Memories", Routes.MEMORIES, it.title, "${it.description} ${it.specialNotes}", it.isFavorite))
            }
            moments.forEach {
                add(SearchResultItem("sp_${it.id}", "Special Moments", Routes.MOMENTS, it.title, it.description, it.isFavorite))
            }
            timeline.forEach {
                add(SearchResultItem("tl_${it.id}", "Our Journey Timeline", Routes.TIMELINE, it.title, it.description, it.isFavorite))
            }
            promises.forEach {
                add(SearchResultItem("pr_${it.id}", "Promises to Each Other", Routes.PROMISES, it.title, it.description, it.isFavorite))
            }
            dreams.forEach {
                add(SearchResultItem("dr_${it.id}", "Our Future Together", Routes.FUTURE, it.title, it.notes, it.isFavorite))
            }
            photos.forEach {
                add(SearchResultItem("ph_${it.id}", "Photo Gallery", Routes.GALLERY, it.title, it.caption, it.isFavorite))
            }
            notes.forEach {
                add(SearchResultItem("nt_${it.id}", "Personal Love Notes", Routes.NOTES, it.title, it.body, it.isFavorite))
            }
        }
    }

    val filtered = remember(allItems, query, favoritesOnly) {
        val trimmed = query.trim().lowercase()
        allItems.filter { item ->
            val matchesFav = !favoritesOnly || item.isFavorite
            val matchesQuery = trimmed.isEmpty() ||
                item.title.lowercase().contains(trimmed) ||
                item.snippet.lowercase().contains(trimmed) ||
                item.sectionTitle.lowercase().contains(trimmed)
            matchesFav && matchesQuery
        }
    }

    SunsetBackdrop(darkMode = darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                KeepsakeTopBar(
                    title = if (favoritesOnly) "Saved Favorites" else "Search Keepsake",
                    onBack = onBack
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search letters, quotes, memories, chapters...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !favoritesOnly,
                        onClick = { favoritesOnly = false },
                        label = { Text("All Entries") }
                    )
                    FilterChip(
                        selected = favoritesOnly,
                        onClick = { favoritesOnly = true },
                        label = { Text("Favorites Only") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyKeepsakeState(
                            title = if (favoritesOnly) "No Favorites Saved Yet" else "No Matches Found",
                            subtitle = "Try another keyword or tap the heart icon on any chapter, letter, quote, or memory.",
                            actionLabel = "Back to Dashboard",
                            onAction = onBack
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                tonalElevation = 3.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToSection(item.route) }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.sectionTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (item.isFavorite) {
                                            Icon(
                                                Icons.Default.Favorite,
                                                contentDescription = "Favorite",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (item.snippet.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.snippet,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
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
}
