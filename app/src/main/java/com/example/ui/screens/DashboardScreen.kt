package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppPreferences
import com.example.navigation.Routes
import com.example.ui.components.DailyLoveMessageCard
import com.example.ui.components.RomanticDatePickerDialog
import com.example.ui.components.SunsetBackdrop

data class DashboardSectionItem(
    val route: String,
    val title: String,
    val subtitle: String,
    val count: Int,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun DashboardScreen(
    prefs: AppPreferences,
    dailyLoveMessage: String,
    counts: Map<String, Int>,
    onNavigate: (String) -> Unit,
    onToggleMusic: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onSetStartDate: (String) -> Unit,
    onSetAnniversaryDate: (String) -> Unit,
    onLockNow: () -> Unit
) {
    var pickingStartDate by remember { mutableStateOf(false) }
    var pickingAnniversaryDate by remember { mutableStateOf(false) }

    val sections = remember(counts) {
        listOf(
            DashboardSectionItem(
                route = Routes.STORY,
                title = "Our Love Story",
                subtitle = "Book of chapters",
                count = counts[Routes.STORY] ?: 0,
                icon = Icons.AutoMirrored.Filled.MenuBook,
                testTag = "section_our_love_story"
            ),
            DashboardSectionItem(
                route = Routes.LETTERS,
                title = "Romantic Letters",
                subtitle = "Letters to cherish",
                count = counts[Routes.LETTERS] ?: 0,
                icon = Icons.Default.Mail,
                testTag = "section_romantic_letters"
            ),
            DashboardSectionItem(
                route = Routes.QUOTES,
                title = "Love Quotes",
                subtitle = "Words & daily quote",
                count = counts[Routes.QUOTES] ?: 0,
                icon = Icons.Default.FormatQuote,
                testTag = "section_love_quotes"
            ),
            DashboardSectionItem(
                route = Routes.MEMORIES,
                title = "Our Memories",
                subtitle = "Photo & story journal",
                count = counts[Routes.MEMORIES] ?: 0,
                icon = Icons.Default.Collections,
                testTag = "section_our_memories"
            ),
            DashboardSectionItem(
                route = Routes.MOMENTS,
                title = "Special Moments",
                subtitle = "Dates & celebrations",
                count = counts[Routes.MOMENTS] ?: 0,
                icon = Icons.Default.Celebration,
                testTag = "section_special_moments"
            ),
            DashboardSectionItem(
                route = Routes.TIMELINE,
                title = "Journey Timeline",
                subtitle = "Heart milestones",
                count = counts[Routes.TIMELINE] ?: 0,
                icon = Icons.Default.Timeline,
                testTag = "section_journey_timeline"
            ),
            DashboardSectionItem(
                route = Routes.PROMISES,
                title = "Promises to Each Other",
                subtitle = "Vows & values",
                count = counts[Routes.PROMISES] ?: 0,
                icon = Icons.Default.Handshake,
                testTag = "section_promises"
            ),
            DashboardSectionItem(
                route = Routes.FUTURE,
                title = "Our Future Together",
                subtitle = "Dreams & travels",
                count = counts[Routes.FUTURE] ?: 0,
                icon = Icons.Default.AutoAwesome,
                testTag = "section_our_future"
            ),
            DashboardSectionItem(
                route = Routes.GALLERY,
                title = "Photo Gallery",
                subtitle = "Offline keepsake album",
                count = counts[Routes.GALLERY] ?: 0,
                icon = Icons.Default.PhotoLibrary,
                testTag = "section_photo_gallery"
            ),
            DashboardSectionItem(
                route = Routes.NOTES,
                title = "Personal Love Notes",
                subtitle = "Little daily notes",
                count = counts[Routes.NOTES] ?: 0,
                icon = Icons.Default.EditNote,
                testTag = "section_personal_notes"
            )
        )
    }

    if (pickingStartDate) {
        RomanticDatePickerDialog(
            onDateSelected = onSetStartDate,
            onDismiss = { pickingStartDate = false }
        )
    }
    if (pickingAnniversaryDate) {
        RomanticDatePickerDialog(
            onDateSelected = onSetAnniversaryDate,
            onDismiss = { pickingAnniversaryDate = false }
        )
    }

    SunsetBackdrop(darkMode = prefs.darkMode) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            val columns = if (maxWidth >= 720.dp) 3 else 2

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(columns) }) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Miyong & Juliana",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Our Private Sanctuary",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onToggleMusic,
                                    modifier = Modifier.testTag("music_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (prefs.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                        contentDescription = "Toggle romantic background music",
                                        tint = if (prefs.musicEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = onToggleDarkMode,
                                    modifier = Modifier.testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (prefs.darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle light or dark theme"
                                    )
                                }
                                IconButton(
                                    onClick = { onNavigate(Routes.SETTINGS) },
                                    modifier = Modifier.testTag("settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings and privacy"
                                    )
                                }
                                IconButton(
                                    onClick = onLockNow,
                                    modifier = Modifier.testTag("lock_now_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock keepsake",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search & Bookmarks Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = { onNavigate(Routes.SEARCH) },
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("dashboard_search_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Search chapters, letters, quotes...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                onClick = { onNavigate(Routes.FAVORITES) },
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("dashboard_favorites_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = "Favorites",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Saved",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DailyLoveMessageCard(
                            message = dailyLoveMessage,
                            startDateIso = prefs.relationshipStartDateIso,
                            anniversaryIso = prefs.anniversaryDateIso,
                            onEditStartDate = { pickingStartDate = true },
                            onEditAnniversary = { pickingAnniversaryDate = true }
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Keepsake Chapters & Corners",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                items(sections, key = { it.route }) { item ->
                    DashboardSectionCard(
                        item = item,
                        onClick = { onNavigate(item.route) }
                    )
                }

                item(span = { GridItemSpan(columns) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionCard(
    item: DashboardSectionItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(item.testTag)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = item.count.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
