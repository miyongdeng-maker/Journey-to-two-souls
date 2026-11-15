package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KeepsakeDao {
    // Story Chapters
    @Query("SELECT * FROM story_chapters ORDER BY chapterNumber ASC, id ASC")
    fun observeChapters(): Flow<List<StoryChapterEntity>>

    @Query("SELECT * FROM story_chapters ORDER BY chapterNumber ASC, id ASC")
    suspend fun getAllChapters(): List<StoryChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(item: StoryChapterEntity): Long

    @Update
    suspend fun updateChapter(item: StoryChapterEntity)

    @Delete
    suspend fun deleteChapter(item: StoryChapterEntity)

    @Query("DELETE FROM story_chapters")
    suspend fun clearChapters()

    // Love Letters
    @Query("SELECT * FROM love_letters ORDER BY dateLabel DESC, id DESC")
    fun observeLetters(): Flow<List<LoveLetterEntity>>

    @Query("SELECT * FROM love_letters ORDER BY dateLabel DESC, id DESC")
    suspend fun getAllLetters(): List<LoveLetterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(item: LoveLetterEntity): Long

    @Update
    suspend fun updateLetter(item: LoveLetterEntity)

    @Delete
    suspend fun deleteLetter(item: LoveLetterEntity)

    @Query("DELETE FROM love_letters")
    suspend fun clearLetters()

    // Love Quotes
    @Query("SELECT * FROM love_quotes ORDER BY isFavorite DESC, id DESC")
    fun observeQuotes(): Flow<List<LoveQuoteEntity>>

    @Query("SELECT * FROM love_quotes ORDER BY id DESC")
    suspend fun getAllQuotes(): List<LoveQuoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(item: LoveQuoteEntity): Long

    @Update
    suspend fun updateQuote(item: LoveQuoteEntity)

    @Delete
    suspend fun deleteQuote(item: LoveQuoteEntity)

    @Query("DELETE FROM love_quotes")
    suspend fun clearQuotes()

    // Memories
    @Query("SELECT * FROM memories ORDER BY id DESC")
    fun observeMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY id DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(item: MemoryEntity): Long

    @Update
    suspend fun updateMemory(item: MemoryEntity)

    @Delete
    suspend fun deleteMemory(item: MemoryEntity)

    @Query("DELETE FROM memories")
    suspend fun clearMemories()

    // Special Moments
    @Query("SELECT * FROM special_moments ORDER BY id DESC")
    fun observeMoments(): Flow<List<SpecialMomentEntity>>

    @Query("SELECT * FROM special_moments ORDER BY id DESC")
    suspend fun getAllMoments(): List<SpecialMomentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(item: SpecialMomentEntity): Long

    @Update
    suspend fun updateMoment(item: SpecialMomentEntity)

    @Delete
    suspend fun deleteMoment(item: SpecialMomentEntity)

    @Query("DELETE FROM special_moments")
    suspend fun clearMoments()

    // Timeline Milestones
    @Query("SELECT * FROM timeline_milestones ORDER BY sortOrder ASC, id ASC")
    fun observeTimeline(): Flow<List<TimelineMilestoneEntity>>

    @Query("SELECT * FROM timeline_milestones ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllTimeline(): List<TimelineMilestoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(item: TimelineMilestoneEntity): Long

    @Update
    suspend fun updateMilestone(item: TimelineMilestoneEntity)

    @Delete
    suspend fun deleteMilestone(item: TimelineMilestoneEntity)

    @Query("DELETE FROM timeline_milestones")
    suspend fun clearTimeline()

    // Promises
    @Query("SELECT * FROM promises ORDER BY id DESC")
    fun observePromises(): Flow<List<PromiseEntity>>

    @Query("SELECT * FROM promises ORDER BY id DESC")
    suspend fun getAllPromises(): List<PromiseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromise(item: PromiseEntity): Long

    @Update
    suspend fun updatePromise(item: PromiseEntity)

    @Delete
    suspend fun deletePromise(item: PromiseEntity)

    @Query("DELETE FROM promises")
    suspend fun clearPromises()

    // Future Dreams
    @Query("SELECT * FROM future_dreams ORDER BY isAchieved ASC, id DESC")
    fun observeDreams(): Flow<List<FutureDreamEntity>>

    @Query("SELECT * FROM future_dreams ORDER BY id DESC")
    suspend fun getAllDreams(): List<FutureDreamEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDream(item: FutureDreamEntity): Long

    @Update
    suspend fun updateDream(item: FutureDreamEntity)

    @Delete
    suspend fun deleteDream(item: FutureDreamEntity)

    @Query("DELETE FROM future_dreams")
    suspend fun clearDreams()

    // Photo Gallery
    @Query("SELECT * FROM gallery_photos ORDER BY id DESC")
    fun observePhotos(): Flow<List<GalleryPhotoEntity>>

    @Query("SELECT * FROM gallery_photos ORDER BY id DESC")
    suspend fun getAllPhotos(): List<GalleryPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(item: GalleryPhotoEntity): Long

    @Update
    suspend fun updatePhoto(item: GalleryPhotoEntity)

    @Delete
    suspend fun deletePhoto(item: GalleryPhotoEntity)

    @Query("DELETE FROM gallery_photos")
    suspend fun clearPhotos()

    // Personal Love Notes
    @Query("SELECT * FROM personal_notes ORDER BY id DESC")
    fun observeNotes(): Flow<List<PersonalNoteEntity>>

    @Query("SELECT * FROM personal_notes ORDER BY id DESC")
    suspend fun getAllNotes(): List<PersonalNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(item: PersonalNoteEntity): Long

    @Update
    suspend fun updateNote(item: PersonalNoteEntity)

    @Delete
    suspend fun deleteNote(item: PersonalNoteEntity)

    @Query("DELETE FROM personal_notes")
    suspend fun clearNotes()
}
