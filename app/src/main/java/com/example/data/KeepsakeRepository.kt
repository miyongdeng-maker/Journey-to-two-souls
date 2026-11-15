package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

class KeepsakeRepository(
    private val context: Context,
    private val dao: KeepsakeDao,
    val prefs: PreferencesRepository
) {
    val chapters: Flow<List<StoryChapterEntity>> = dao.observeChapters()
    val letters: Flow<List<LoveLetterEntity>> = dao.observeLetters()
    val quotes: Flow<List<LoveQuoteEntity>> = dao.observeQuotes()
    val memories: Flow<List<MemoryEntity>> = dao.observeMemories()
    val moments: Flow<List<SpecialMomentEntity>> = dao.observeMoments()
    val timeline: Flow<List<TimelineMilestoneEntity>> = dao.observeTimeline()
    val promises: Flow<List<PromiseEntity>> = dao.observePromises()
    val dreams: Flow<List<FutureDreamEntity>> = dao.observeDreams()
    val photos: Flow<List<GalleryPhotoEntity>> = dao.observePhotos()
    val notes: Flow<List<PersonalNoteEntity>> = dao.observeNotes()

    // Chapters
    suspend fun saveChapter(chapter: StoryChapterEntity) {
        if (chapter.id == 0L) dao.insertChapter(chapter) else dao.updateChapter(chapter)
    }

    suspend fun deleteChapter(chapter: StoryChapterEntity) = dao.deleteChapter(chapter)

    // Letters
    suspend fun saveLetter(letter: LoveLetterEntity) {
        if (letter.id == 0L) dao.insertLetter(letter) else dao.updateLetter(letter)
    }

    suspend fun deleteLetter(letter: LoveLetterEntity) = dao.deleteLetter(letter)

    // Quotes
    suspend fun saveQuote(quote: LoveQuoteEntity) {
        if (quote.id == 0L) dao.insertQuote(quote) else dao.updateQuote(quote)
    }

    suspend fun deleteQuote(quote: LoveQuoteEntity) = dao.deleteQuote(quote)

    // Memories
    suspend fun saveMemory(memory: MemoryEntity) {
        if (memory.id == 0L) dao.insertMemory(memory) else dao.updateMemory(memory)
    }

    suspend fun deleteMemory(memory: MemoryEntity) {
        LocalPhotoStorage.deleteLocalPhotoIfOwned(context, memory.photoPath)
        dao.deleteMemory(memory)
    }

    // Special Moments
    suspend fun saveMoment(moment: SpecialMomentEntity) {
        if (moment.id == 0L) dao.insertMoment(moment) else dao.updateMoment(moment)
    }

    suspend fun deleteMoment(moment: SpecialMomentEntity) = dao.deleteMoment(moment)

    // Timeline Milestones
    suspend fun saveMilestone(milestone: TimelineMilestoneEntity) {
        if (milestone.id == 0L) dao.insertMilestone(milestone) else dao.updateMilestone(milestone)
    }

    suspend fun deleteMilestone(milestone: TimelineMilestoneEntity) = dao.deleteMilestone(milestone)

    // Promises
    suspend fun savePromise(promise: PromiseEntity) {
        if (promise.id == 0L) dao.insertPromise(promise) else dao.updatePromise(promise)
    }

    suspend fun deletePromise(promise: PromiseEntity) = dao.deletePromise(promise)

    // Future Dreams
    suspend fun saveDream(dream: FutureDreamEntity) {
        if (dream.id == 0L) dao.insertDream(dream) else dao.updateDream(dream)
    }

    suspend fun deleteDream(dream: FutureDreamEntity) = dao.deleteDream(dream)

    // Gallery Photos
    suspend fun savePhoto(photo: GalleryPhotoEntity) {
        if (photo.id == 0L) dao.insertPhoto(photo) else dao.updatePhoto(photo)
    }

    suspend fun deletePhoto(photo: GalleryPhotoEntity) {
        LocalPhotoStorage.deleteLocalPhotoIfOwned(context, photo.photoPath)
        dao.deletePhoto(photo)
    }

    // Personal Notes
    suspend fun saveNote(note: PersonalNoteEntity) {
        if (note.id == 0L) dao.insertNote(note) else dao.updateNote(note)
    }

    suspend fun deleteNote(note: PersonalNoteEntity) = dao.deleteNote(note)

    // Local image import
    suspend fun importPickedPhoto(uri: Uri): String? =
        LocalPhotoStorage.savePickedImageLocally(context, uri)

    // Local JSON Backup & Restore
    suspend fun exportBackup(uri: Uri, currentPrefs: AppPreferences): Result<Int> =
        BackupManager.exportToUri(context, uri, dao, currentPrefs)

    suspend fun importBackup(uri: Uri): Result<Int> =
        BackupManager.importFromUri(context, uri, dao, prefs)
}
