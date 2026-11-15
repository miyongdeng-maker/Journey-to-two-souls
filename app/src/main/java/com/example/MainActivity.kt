package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.navigation.Routes
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JourneyTimelineScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.LoveQuotesScreen
import com.example.ui.screens.OurFutureScreen
import com.example.ui.screens.OurLoveStoryScreen
import com.example.ui.screens.OurMemoriesScreen
import com.example.ui.screens.PersonalNotesScreen
import com.example.ui.screens.PhotoGalleryScreen
import com.example.ui.screens.PromisesScreen
import com.example.ui.screens.RomanticLettersScreen
import com.example.ui.screens.SearchFavoritesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpecialMomentsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.JourneyOfTwoSoulsTheme
import com.example.viewmodel.KeepsakeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: KeepsakeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JourneyOfTwoSoulsApp(viewModel = viewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }
}

@Composable
fun JourneyOfTwoSoulsApp(
    viewModel: KeepsakeViewModel,
    skipSplash: Boolean = false
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val letters by viewModel.letters.collectAsStateWithLifecycle()
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val moments by viewModel.moments.collectAsStateWithLifecycle()
    val timeline by viewModel.timeline.collectAsStateWithLifecycle()
    val promises by viewModel.promises.collectAsStateWithLifecycle()
    val dreams by viewModel.dreams.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Enforce lock protection: any private route redirects immediately to LockScreen if locked
    LaunchedEffect(isUnlocked, currentRoute) {
        val publicRoutes = setOf(Routes.SPLASH, Routes.HOME, Routes.LOCK)
        if (!isUnlocked && currentRoute != null && currentRoute !in publicRoutes) {
            navController.navigate(Routes.LOCK) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    JourneyOfTwoSoulsTheme(
        darkTheme = prefs.darkMode,
        readingFontScale = prefs.readingFontScale
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        viewModel.recordUserActivity()
                    }
                }
            }
        ) {
            KeepsakeNavHost(
                navController = navController,
                startDestination = if (skipSplash) Routes.HOME else Routes.SPLASH,
                viewModel = viewModel,
                prefs = prefs,
                isUnlocked = isUnlocked,
                authError = authError,
                statusMessage = statusMessage,
                chapters = chapters,
                letters = letters,
                quotes = quotes,
                memories = memories,
                moments = moments,
                timeline = timeline,
                promises = promises,
                dreams = dreams,
                photos = photos,
                notes = notes
            )
        }
    }
}

@Composable
private fun KeepsakeNavHost(
    navController: NavHostController,
    startDestination: String,
    viewModel: KeepsakeViewModel,
    prefs: com.example.data.AppPreferences,
    isUnlocked: Boolean,
    authError: String?,
    statusMessage: String?,
    chapters: List<com.example.data.StoryChapterEntity>,
    letters: List<com.example.data.LoveLetterEntity>,
    quotes: List<com.example.data.LoveQuoteEntity>,
    memories: List<com.example.data.MemoryEntity>,
    moments: List<com.example.data.SpecialMomentEntity>,
    timeline: List<com.example.data.TimelineMilestoneEntity>,
    promises: List<com.example.data.PromiseEntity>,
    dreams: List<com.example.data.FutureDreamEntity>,
    photos: List<com.example.data.GalleryPhotoEntity>,
    notes: List<com.example.data.PersonalNoteEntity>
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                darkMode = prefs.darkMode,
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                darkMode = prefs.darkMode,
                heroPhotoPath = prefs.heroPhotoPath,
                isUnlocked = isUnlocked,
                onEnterLoveStory = {
                    if (isUnlocked) {
                        navController.navigate(Routes.DASHBOARD)
                    } else {
                        navController.navigate(Routes.LOCK)
                    }
                }
            )
        }

        composable(Routes.LOCK) {
            LockScreen(
                darkMode = prefs.darkMode,
                errorMessage = authError,
                onClearError = { viewModel.clearAuthError() },
                onUnlockAttempt = { input ->
                    viewModel.attemptUnlock(input) {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOCK) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onBackToCover = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            val counts = mapOf(
                Routes.STORY to chapters.size,
                Routes.LETTERS to letters.size,
                Routes.QUOTES to quotes.size,
                Routes.MEMORIES to memories.size,
                Routes.MOMENTS to moments.size,
                Routes.TIMELINE to timeline.size,
                Routes.PROMISES to promises.size,
                Routes.FUTURE to dreams.size,
                Routes.GALLERY to photos.size,
                Routes.NOTES to notes.size
            )
            DashboardScreen(
                prefs = prefs,
                dailyLoveMessage = viewModel.getDailyLoveMessage(),
                counts = counts,
                onNavigate = { route -> navController.navigate(route) },
                onToggleMusic = { viewModel.toggleMusic() },
                onToggleDarkMode = { viewModel.setDarkMode(!prefs.darkMode) },
                onSetStartDate = { viewModel.setRelationshipStartDate(it) },
                onSetAnniversaryDate = { viewModel.setAnniversaryDate(it) },
                onLockNow = {
                    viewModel.lockKeepsake()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.STORY) {
            OurLoveStoryScreen(
                darkMode = prefs.darkMode,
                chapters = chapters,
                onBack = { navController.popBackStack() },
                onSaveChapter = { viewModel.saveChapter(it) },
                onDeleteChapter = { viewModel.deleteChapter(it) },
                onToggleFavorite = { viewModel.toggleChapterFavorite(it) }
            )
        }

        composable(Routes.LETTERS) {
            RomanticLettersScreen(
                darkMode = prefs.darkMode,
                letters = letters,
                onBack = { navController.popBackStack() },
                onSaveLetter = { viewModel.saveLetter(it) },
                onDeleteLetter = { viewModel.deleteLetter(it) },
                onToggleFavorite = { viewModel.toggleLetterFavorite(it) }
            )
        }

        composable(Routes.QUOTES) {
            LoveQuotesScreen(
                darkMode = prefs.darkMode,
                quotes = quotes,
                dailyQuote = viewModel.getDailyQuote(quotes),
                onBack = { navController.popBackStack() },
                onSaveQuote = { viewModel.saveQuote(it) },
                onDeleteQuote = { viewModel.deleteQuote(it) },
                onToggleFavorite = { viewModel.toggleQuoteFavorite(it) }
            )
        }

        composable(Routes.MEMORIES) {
            OurMemoriesScreen(
                darkMode = prefs.darkMode,
                memories = memories,
                onBack = { navController.popBackStack() },
                onSaveMemory = { existing, title, desc, date, notesText, uri, removePhoto ->
                    viewModel.saveMemory(existing, title, desc, date, notesText, uri, removePhoto)
                },
                onDeleteMemory = { viewModel.deleteMemory(it) },
                onToggleFavorite = { viewModel.toggleMemoryFavorite(it) }
            )
        }

        composable(Routes.MOMENTS) {
            SpecialMomentsScreen(
                darkMode = prefs.darkMode,
                moments = moments,
                onBack = { navController.popBackStack() },
                onSaveMoment = { viewModel.saveMoment(it) },
                onDeleteMoment = { viewModel.deleteMoment(it) },
                onToggleFavorite = { viewModel.toggleMomentFavorite(it) }
            )
        }

        composable(Routes.TIMELINE) {
            JourneyTimelineScreen(
                darkMode = prefs.darkMode,
                milestones = timeline,
                onBack = { navController.popBackStack() },
                onSaveMilestone = { viewModel.saveMilestone(it) },
                onDeleteMilestone = { viewModel.deleteMilestone(it) },
                onToggleFavorite = { viewModel.toggleMilestoneFavorite(it) }
            )
        }

        composable(Routes.PROMISES) {
            PromisesScreen(
                darkMode = prefs.darkMode,
                promises = promises,
                onBack = { navController.popBackStack() },
                onSavePromise = { viewModel.savePromise(it) },
                onDeletePromise = { viewModel.deletePromise(it) },
                onToggleFavorite = { viewModel.togglePromiseFavorite(it) }
            )
        }

        composable(Routes.FUTURE) {
            OurFutureScreen(
                darkMode = prefs.darkMode,
                dreams = dreams,
                onBack = { navController.popBackStack() },
                onSaveDream = { viewModel.saveDream(it) },
                onDeleteDream = { viewModel.deleteDream(it) },
                onToggleAchieved = { viewModel.toggleDreamAchieved(it) },
                onToggleFavorite = { viewModel.toggleDreamFavorite(it) }
            )
        }

        composable(Routes.GALLERY) {
            PhotoGalleryScreen(
                darkMode = prefs.darkMode,
                photos = photos,
                onBack = { navController.popBackStack() },
                onImportPhoto = { uri, title, caption, dateLabel ->
                    viewModel.importGalleryPhoto(uri, title, caption, dateLabel)
                },
                onUpdatePhoto = { viewModel.updatePhotoDetails(it) },
                onDeletePhoto = { viewModel.deletePhoto(it) },
                onToggleFavorite = { viewModel.togglePhotoFavorite(it) }
            )
        }

        composable(Routes.NOTES) {
            PersonalNotesScreen(
                darkMode = prefs.darkMode,
                notes = notes,
                onBack = { navController.popBackStack() },
                onSaveNote = { viewModel.saveNote(it) },
                onDeleteNote = { viewModel.deleteNote(it) },
                onToggleFavorite = { viewModel.toggleNoteFavorite(it) }
            )
        }

        composable(Routes.SEARCH) {
            SearchFavoritesScreen(
                darkMode = prefs.darkMode,
                initialFavoritesOnly = false,
                chapters = chapters,
                letters = letters,
                quotes = quotes,
                memories = memories,
                moments = moments,
                timeline = timeline,
                promises = promises,
                dreams = dreams,
                photos = photos,
                notes = notes,
                onBack = { navController.popBackStack() },
                onNavigateToSection = { route -> navController.navigate(route) }
            )
        }

        composable(Routes.FAVORITES) {
            SearchFavoritesScreen(
                darkMode = prefs.darkMode,
                initialFavoritesOnly = true,
                chapters = chapters,
                letters = letters,
                quotes = quotes,
                memories = memories,
                moments = moments,
                timeline = timeline,
                promises = promises,
                dreams = dreams,
                photos = photos,
                notes = notes,
                onBack = { navController.popBackStack() },
                onNavigateToSection = { route -> navController.navigate(route) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                prefs = prefs,
                statusMessage = statusMessage,
                onClearStatusMessage = { viewModel.clearStatusMessage() },
                onBack = { navController.popBackStack() },
                onSetDarkMode = { viewModel.setDarkMode(it) },
                onSetFontScale = { viewModel.setReadingFontScale(it) },
                onSetMusicEnabled = { viewModel.setMusicEnabled(it) },
                onSetInactivityTimeout = { viewModel.setInactivityTimeout(it) },
                onSetStartDate = { viewModel.setRelationshipStartDate(it) },
                onSetAnniversaryDate = { viewModel.setAnniversaryDate(it) },
                onUpdateHeroPhoto = { viewModel.updateHeroPhoto(it) },
                onResetHeroPhoto = { viewModel.resetHeroPhoto() },
                onChangePassword = { curr, next, confirm, cb ->
                    viewModel.changePassword(curr, next, confirm, cb)
                },
                onExportBackup = { viewModel.exportBackup(it) },
                onImportBackup = { viewModel.importBackup(it) },
                onLockNow = {
                    viewModel.lockKeepsake()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
