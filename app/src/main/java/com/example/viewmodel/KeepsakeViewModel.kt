package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbientMusicPlayer
import com.example.data.AppPreferences
import com.example.data.FutureDreamEntity
import com.example.data.GalleryPhotoEntity
import com.example.data.KeepsakeDatabase
import com.example.data.KeepsakeRepository
import com.example.data.LocalPhotoStorage
import com.example.data.LoveLetterEntity
import com.example.data.LoveQuoteEntity
import com.example.data.MemoryEntity
import com.example.data.PersonalNoteEntity
import com.example.data.PreferencesRepository
import com.example.data.PromiseEntity
import com.example.data.SpecialMomentEntity
import com.example.data.StoryChapterEntity
import com.example.data.TimelineMilestoneEntity
import com.example.util.DateHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class SearchResultItem(
    val id: String,
    val sectionTitle: String,
    val route: String,
    val title: String,
    val snippet: String,
    val isFavorite: Boolean
)

class KeepsakeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = KeepsakeDatabase.getInstance(application)
    private val prefsRepo = PreferencesRepository(application)
    private val repository = KeepsakeRepository(application, database.keepsakeDao(), prefsRepo)
    private val musicPlayer = AmbientMusicPlayer()

    val preferences: StateFlow<AppPreferences> = prefsRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppPreferences())

    val chapters: StateFlow<List<StoryChapterEntity>> = repository.chapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val letters: StateFlow<List<LoveLetterEntity>> = repository.letters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quotes: StateFlow<List<LoveQuoteEntity>> = repository.quotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.memories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moments: StateFlow<List<SpecialMomentEntity>> = repository.moments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeline: StateFlow<List<TimelineMilestoneEntity>> = repository.timeline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promises: StateFlow<List<PromiseEntity>> = repository.promises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dreams: StateFlow<List<FutureDreamEntity>> = repository.dreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<GalleryPhotoEntity>> = repository.photos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<PersonalNoteEntity>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var inactivityJob: Job? = null
    private var backgroundedAtMillis: Long? = null

    init {
        viewModelScope.launch {
            prefsRepo.ensurePasswordInitialized()
            KeepsakeDatabase.seedStarterPlaceholders(database.keepsakeDao())
        }
        viewModelScope.launch {
            preferences.collect { prefs ->
                musicPlayer.setEnabled(prefs.musicEnabled)
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun attemptUnlock(password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val valid = prefsRepo.verifyPassword(password)
            if (valid) {
                _authError.value = null
                _isUnlocked.value = true
                resetInactivityTimer()
                onSuccess()
            } else {
                _authError.value = "That password didn't open our keepsake. Please try again with love."
            }
        }
    }

    fun lockKeepsake() {
        inactivityJob?.cancel()
        _isUnlocked.value = false
        _authError.value = null
    }

    fun recordUserActivity() {
        if (_isUnlocked.value) {
            resetInactivityTimer()
        }
    }

    fun onAppBackgrounded() {
        backgroundedAtMillis = System.currentTimeMillis()
        musicPlayer.stop()
        if (preferences.value.inactivityTimeoutMinutes == 0) {
            lockKeepsake()
        }
    }

    fun onAppForegrounded() {
        val bgAt = backgroundedAtMillis
        backgroundedAtMillis = null
        val timeoutMin = preferences.value.inactivityTimeoutMinutes
        if (_isUnlocked.value && bgAt != null) {
            if (timeoutMin == 0) {
                lockKeepsake()
            } else {
                val elapsedMs = System.currentTimeMillis() - bgAt
                if (elapsedMs >= timeoutMin * 60_000L) {
                    lockKeepsake()
                } else {
                    resetInactivityTimer()
                }
            }
        }
        if (preferences.value.musicEnabled) {
            musicPlayer.setEnabled(true)
        }
    }

    private fun resetInactivityTimer() {
        inactivityJob?.cancel()
        val timeoutMin = preferences.value.inactivityTimeoutMinutes
        if (timeoutMin > 0 && _isUnlocked.value) {
            inactivityJob = viewModelScope.launch {
                delay(timeoutMin * 60_000L)
                _isUnlocked.value = false
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            when {
                currentPassword.isBlank() -> onResult(false, "Please enter your current password.")
                newPassword.length < 4 -> onResult(false, "New password must be at least 4 characters.")
                newPassword != confirmPassword -> onResult(false, "New passwords do not match.")
                else -> {
                    val changed = prefsRepo.changePassword(currentPassword, newPassword)
                    if (changed) {
                        onResult(true, "Your keepsake password has been updated securely.")
                    } else {
                        onResult(false, "Current password is incorrect.")
                    }
                }
            }
        }
    }

    // Preferences updates
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { prefsRepo.setDarkMode(enabled) }
    fun setReadingFontScale(scale: Float) = viewModelScope.launch { prefsRepo.setReadingFontScale(scale) }
    fun setInactivityTimeout(minutes: Int) = viewModelScope.launch {
        prefsRepo.setInactivityTimeoutMinutes(minutes)
        resetInactivityTimer()
    }
    fun toggleMusic() = viewModelScope.launch {
        prefsRepo.setMusicEnabled(!preferences.value.musicEnabled)
    }
    fun setMusicEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setMusicEnabled(enabled)
    }
    fun setRelationshipStartDate(iso: String) = viewModelScope.launch {
        prefsRepo.setRelationshipStartDateIso(iso)
    }
    fun setAnniversaryDate(iso: String) = viewModelScope.launch {
        prefsRepo.setAnniversaryDateIso(iso)
    }

    fun updateHeroPhoto(uri: Uri) = viewModelScope.launch {
        val oldPath = preferences.value.heroPhotoPath
        val newPath = repository.importPickedPhoto(uri)
        if (newPath != null) {
            LocalPhotoStorage.deleteLocalPhotoIfOwned(getApplication(), oldPath)
            prefsRepo.setHeroPhotoPath(newPath)
            _statusMessage.value = "Homepage couple portrait updated locally."
        } else {
            _statusMessage.value = "Could not import selected photo."
        }
    }

    fun resetHeroPhoto() = viewModelScope.launch {
        LocalPhotoStorage.deleteLocalPhotoIfOwned(getApplication(), preferences.value.heroPhotoPath)
        prefsRepo.setHeroPhotoPath("")
        _statusMessage.value = "Restored default romantic couple portrait."
    }

    // Daily love message (offline rotating devotionals for Miyong & Juliana)
    fun getDailyLoveMessage(): String {
        val messages = listOf(
            "Every sunrise is gentler knowing Miyong and Juliana walk through life hand in hand.",
            "Love is not measured in hurried hours, but in the quiet peace you bring to each other's heart.",
            "Two souls, one steady rhythm—grateful for today, and every tomorrow waiting ahead.",
            "Even in silence, your hearts speak the warmest language in the world.",
            "Hold onto the small moments; one day you'll realize they were the greatest parts of your story.",
            "Wherever life takes you both, home is simply wherever the two of you stand together.",
            "May your patience stay deep, your laughter stay easy, and your love stay timeless."
        )
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return messages[dayOfYear % messages.size]
    }

    fun getDailyQuote(allQuotes: List<LoveQuoteEntity>): LoveQuoteEntity? {
        if (allQuotes.isEmpty()) return null
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return allQuotes[dayOfYear % allQuotes.size]
    }

    // CRUD: Story Chapters
    fun saveChapter(chapter: StoryChapterEntity) = viewModelScope.launch {
        repository.saveChapter(chapter)
    }
    fun deleteChapter(chapter: StoryChapterEntity) = viewModelScope.launch {
        repository.deleteChapter(chapter)
    }
    fun toggleChapterFavorite(chapter: StoryChapterEntity) = viewModelScope.launch {
        repository.saveChapter(chapter.copy(isFavorite = !chapter.isFavorite))
    }

    // CRUD: Romantic Letters
    fun saveLetter(letter: LoveLetterEntity) = viewModelScope.launch {
        repository.saveLetter(letter)
    }
    fun deleteLetter(letter: LoveLetterEntity) = viewModelScope.launch {
        repository.deleteLetter(letter)
    }
    fun toggleLetterFavorite(letter: LoveLetterEntity) = viewModelScope.launch {
        repository.saveLetter(letter.copy(isFavorite = !letter.isFavorite))
    }

    // CRUD: Love Quotes
    fun saveQuote(quote: LoveQuoteEntity) = viewModelScope.launch {
        repository.saveQuote(quote)
    }
    fun deleteQuote(quote: LoveQuoteEntity) = viewModelScope.launch {
        repository.deleteQuote(quote)
    }
    fun toggleQuoteFavorite(quote: LoveQuoteEntity) = viewModelScope.launch {
        repository.saveQuote(quote.copy(isFavorite = !quote.isFavorite))
    }

    // CRUD: Memories (with optional local photo import)
    fun saveMemory(
        existing: MemoryEntity?,
        title: String,
        description: String,
        dateLabel: String,
        specialNotes: String,
        newPhotoUri: Uri?,
        removePhoto: Boolean
    ) = viewModelScope.launch {
        var finalPath = if (removePhoto) "" else existing?.photoPath.orEmpty()
        if (removePhoto && !existing?.photoPath.isNullOrBlank()) {
            LocalPhotoStorage.deleteLocalPhotoIfOwned(getApplication(), existing!!.photoPath)
        }
        if (newPhotoUri != null) {
            val imported = repository.importPickedPhoto(newPhotoUri)
            if (imported != null) {
                if (!existing?.photoPath.isNullOrBlank()) {
                    LocalPhotoStorage.deleteLocalPhotoIfOwned(getApplication(), existing!!.photoPath)
                }
                finalPath = imported
            }
        }
        val entity = (existing ?: MemoryEntity(title = title, description = description, dateLabel = dateLabel))
            .copy(
                title = title,
                description = description,
                dateLabel = dateLabel,
                photoPath = finalPath,
                specialNotes = specialNotes
            )
        repository.saveMemory(entity)
    }

    fun deleteMemory(memory: MemoryEntity) = viewModelScope.launch {
        repository.deleteMemory(memory)
    }
    fun toggleMemoryFavorite(memory: MemoryEntity) = viewModelScope.launch {
        repository.saveMemory(memory.copy(isFavorite = !memory.isFavorite))
    }

    // CRUD: Special Moments
    fun saveMoment(moment: SpecialMomentEntity) = viewModelScope.launch {
        repository.saveMoment(moment)
    }
    fun deleteMoment(moment: SpecialMomentEntity) = viewModelScope.launch {
        repository.deleteMoment(moment)
    }
    fun toggleMomentFavorite(moment: SpecialMomentEntity) = viewModelScope.launch {
        repository.saveMoment(moment.copy(isFavorite = !moment.isFavorite))
    }

    // CRUD: Timeline Milestones
    fun saveMilestone(milestone: TimelineMilestoneEntity) = viewModelScope.launch {
        repository.saveMilestone(milestone)
    }
    fun deleteMilestone(milestone: TimelineMilestoneEntity) = viewModelScope.launch {
        repository.deleteMilestone(milestone)
    }
    fun toggleMilestoneFavorite(milestone: TimelineMilestoneEntity) = viewModelScope.launch {
        repository.saveMilestone(milestone.copy(isFavorite = !milestone.isFavorite))
    }

    // CRUD: Promises
    fun savePromise(promise: PromiseEntity) = viewModelScope.launch {
        repository.savePromise(promise)
    }
    fun deletePromise(promise: PromiseEntity) = viewModelScope.launch {
        repository.deletePromise(promise)
    }
    fun togglePromiseFavorite(promise: PromiseEntity) = viewModelScope.launch {
        repository.savePromise(promise.copy(isFavorite = !promise.isFavorite))
    }

    // CRUD: Future Dreams
    fun saveDream(dream: FutureDreamEntity) = viewModelScope.launch {
        repository.saveDream(dream)
    }
    fun deleteDream(dream: FutureDreamEntity) = viewModelScope.launch {
        repository.deleteDream(dream)
    }
    fun toggleDreamAchieved(dream: FutureDreamEntity) = viewModelScope.launch {
        repository.saveDream(dream.copy(isAchieved = !dream.isAchieved))
    }
    fun toggleDreamFavorite(dream: FutureDreamEntity) = viewModelScope.launch {
        repository.saveDream(dream.copy(isFavorite = !dream.isFavorite))
    }

    // CRUD: Photo Gallery
    fun importGalleryPhoto(uri: Uri, title: String, caption: String, dateLabel: String) =
        viewModelScope.launch {
            val savedPath = repository.importPickedPhoto(uri)
            if (savedPath != null) {
                repository.savePhoto(
                    GalleryPhotoEntity(
                        title = title.ifBlank { "Cherished Moment" },
                        caption = caption,
                        dateLabel = dateLabel.ifBlank { DateHelper.formatDisplayDate(DateHelper.todayIso()) },
                        photoPath = savedPath
                    )
                )
                _statusMessage.value = "Photo saved safely offline."
            } else {
                _statusMessage.value = "Could not import selected photo."
            }
        }

    fun updatePhotoDetails(photo: GalleryPhotoEntity) = viewModelScope.launch {
        repository.savePhoto(photo)
    }
    fun deletePhoto(photo: GalleryPhotoEntity) = viewModelScope.launch {
        repository.deletePhoto(photo)
    }
    fun togglePhotoFavorite(photo: GalleryPhotoEntity) = viewModelScope.launch {
        repository.savePhoto(photo.copy(isFavorite = !photo.isFavorite))
    }

    // CRUD: Personal Notes
    fun saveNote(note: PersonalNoteEntity) = viewModelScope.launch {
        repository.saveNote(note)
    }
    fun deleteNote(note: PersonalNoteEntity) = viewModelScope.launch {
        repository.deleteNote(note)
    }
    fun toggleNoteFavorite(note: PersonalNoteEntity) = viewModelScope.launch {
        repository.saveNote(note.copy(isFavorite = !note.isFavorite))
    }

    // Local JSON Backup & Restore
    fun exportBackup(uri: Uri) = viewModelScope.launch {
        val result = repository.exportBackup(uri, preferences.value)
        _statusMessage.value = result.fold(
            onSuccess = { count -> "Backed up $count keepsake entries to local storage." },
            onFailure = { err -> "Backup failed: ${err.localizedMessage ?: "Unknown error"}" }
        )
    }

    fun importBackup(uri: Uri) = viewModelScope.launch {
        val result = repository.importBackup(uri)
        _statusMessage.value = result.fold(
            onSuccess = { count -> "Restored $count keepsake entries from local backup." },
            onFailure = { err -> "Restore failed: ${err.localizedMessage ?: "Invalid backup file"}" }
        )
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayer.stop()
    }
}
