package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StoryChapterEntity::class,
        LoveLetterEntity::class,
        LoveQuoteEntity::class,
        MemoryEntity::class,
        SpecialMomentEntity::class,
        TimelineMilestoneEntity::class,
        PromiseEntity::class,
        FutureDreamEntity::class,
        GalleryPhotoEntity::class,
        PersonalNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KeepsakeDatabase : RoomDatabase() {
    abstract fun keepsakeDao(): KeepsakeDao

    companion object {
        @Volatile
        private var INSTANCE: KeepsakeDatabase? = null

        fun getInstance(context: Context): KeepsakeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): KeepsakeDatabase {
            return Room.databaseBuilder(
                appContext,
                KeepsakeDatabase::class.java,
                "journey_of_two_souls.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedStarterPlaceholders(getInstance(appContext).keepsakeDao())
                        }
                    }
                })
                .build()
        }

        /**
         * Seeds clearly-marked editable templates and timeless love quotes on first launch
         * without inventing any personal relationship dates or fictional history.
         */
        suspend fun seedStarterPlaceholders(dao: KeepsakeDao) {
            if (dao.getAllChapters().isEmpty()) {
                dao.insertChapter(
                    StoryChapterEntity(
                        chapterNumber = 1,
                        title = "How Our Paths Crossed (Editable Chapter)",
                        dateLabel = "Add your starting date",
                        content = "Every enduring story begins with a quiet moment that changes everything forever.\n\n" +
                            "Use the Edit button above to write the true story of how Miyong Bangot and Juliana first met—the place, the weather, the first conversation, and the moment you both realized something extraordinary had begun.",
                        isFavorite = true
                    )
                )
                dao.insertChapter(
                    StoryChapterEntity(
                        chapterNumber = 2,
                        title = "Growing Closer Together (Editable Chapter)",
                        dateLabel = "Add chapter date",
                        content = "This page waits for your real words—the shared laughter, late-night talks, small acts of care, and quiet reassurance that turned two separate paths into one shared journey.\n\n" +
                            "Tap '+' anytime to add new chapters as your love story continues to unfold."
                    )
                )
            }

            if (dao.getAllLetters().isEmpty()) {
                dao.insertLetter(
                    LoveLetterEntity(
                        title = "A Welcome Letter for Our Keepsake",
                        sender = "Miyong Bangot",
                        recipient = "Juliana",
                        dateLabel = "Editable Date",
                        content = "Dearest Juliana,\n\n" +
                            "I built this quiet space so our words, memories, promises, and dreams have a home that belongs only to us—safe, offline, and timeless.\n\n" +
                            "Edit this letter or write a new one whenever your heart wants to leave something for tomorrow.\n\n" +
                            "Always yours,\nMiyong",
                        isFavorite = true
                    )
                )
            }

            if (dao.getAllQuotes().isEmpty()) {
                dao.insertQuote(
                    LoveQuoteEntity(
                        quoteText = "Two hearts, one journey, a love story without an ending.",
                        author = "Journey of Two Souls",
                        isPersonal = true,
                        isFavorite = true
                    )
                )
                dao.insertQuote(
                    LoveQuoteEntity(
                        quoteText = "Whatever our souls are made of, his and mine are the same.",
                        author = "Emily Brontë",
                        isPersonal = false,
                        isFavorite = true
                    )
                )
                dao.insertQuote(
                    LoveQuoteEntity(
                        quoteText = "In all the world, there is no heart for me like yours. In all the world, there is no love for you like mine.",
                        author = "Maya Angelou",
                        isPersonal = false,
                        isFavorite = false
                    )
                )
                dao.insertQuote(
                    LoveQuoteEntity(
                        quoteText = "I love you not only for what you are, but for what I am when I am with you.",
                        author = "Roy Croft",
                        isPersonal = false,
                        isFavorite = false
                    )
                )
            }

            if (dao.getAllMemories().isEmpty()) {
                dao.insertMemory(
                    MemoryEntity(
                        title = "Your First Saved Memory (Tap to Edit)",
                        description = "Add the memory you never want to fade—where you went, how the light looked, and why it still makes you smile.",
                        dateLabel = "Set memory date",
                        specialNotes = "You can attach a photograph from your phone gallery right inside this memory."
                    )
                )
            }

            if (dao.getAllMoments().isEmpty()) {
                dao.insertMoment(
                    SpecialMomentEntity(
                        title = "Our Anniversary Celebration (Editable)",
                        category = "Anniversary",
                        dateLabel = "Tap to set date",
                        description = "Edit this placeholder with your real anniversary date and how you celebrated together."
                    )
                )
            }

            if (dao.getAllTimeline().isEmpty()) {
                dao.insertMilestone(
                    TimelineMilestoneEntity(
                        title = "The Day Our Journey Began (Editable)",
                        dateLabel = "Set your milestone date",
                        description = "Tap edit to record the real date and story of your first milestone together.",
                        sortOrder = 1,
                        isFavorite = true
                    )
                )
            }

            if (dao.getAllPromises().isEmpty()) {
                dao.insertPromise(
                    PromiseEntity(
                        title = "To Always Listen With Patience",
                        description = "Even on busy or difficult days, we promise to listen gently, speak honestly, and protect each other's peace.",
                        category = "Promise",
                        madeBy = "Miyong & Juliana",
                        isFavorite = true
                    )
                )
            }

            if (dao.getAllDreams().isEmpty()) {
                dao.insertDream(
                    FutureDreamEntity(
                        title = "A Quiet Golden-Hour Trip Together (Editable)",
                        category = "Travel",
                        targetTimeframe = "Add target year or season",
                        notes = "Write down the places you dream of exploring hand in hand."
                    )
                )
            }

            if (dao.getAllNotes().isEmpty()) {
                dao.insertNote(
                    PersonalNoteEntity(
                        title = "For a Smile Today",
                        body = "No matter how ordinary today feels, remember you are deeply cherished. Add your own quick love notes here anytime.",
                        author = "Miyong Bangot",
                        mood = "Tender",
                        dateLabel = "Editable Date",
                        isFavorite = true
                    )
                )
            }
        }
    }
}
