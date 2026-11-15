package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_chapters")
data class StoryChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterNumber: Int,
    val title: String,
    val dateLabel: String,
    val content: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "love_letters")
data class LoveLetterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sender: String,
    val recipient: String,
    val dateLabel: String,
    val content: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "love_quotes")
data class LoveQuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteText: String,
    val author: String,
    val isPersonal: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val dateLabel: String,
    val photoPath: String = "",
    val specialNotes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "special_moments")
data class SpecialMomentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val dateLabel: String,
    val description: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "timeline_milestones")
data class TimelineMilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateLabel: String,
    val description: String,
    val sortOrder: Int = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "promises")
data class PromiseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val madeBy: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "future_dreams")
data class FutureDreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val targetTimeframe: String,
    val notes: String,
    val isAchieved: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gallery_photos")
data class GalleryPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val caption: String,
    val dateLabel: String,
    val photoPath: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_notes")
data class PersonalNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val author: String,
    val mood: String,
    val dateLabel: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
