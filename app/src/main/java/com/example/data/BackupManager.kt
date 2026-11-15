package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Exports and restores all keepsake sections and relationship dates to/from a local
 * JSON document chosen by the user. Never automatically shares or uploads backups.
 */
object BackupManager {

    suspend fun exportToUri(
        context: Context,
        uri: Uri,
        dao: KeepsakeDao,
        prefs: AppPreferences
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("app", "Journey of Two Souls")
            root.put("version", 1)
            root.put("exportedAt", System.currentTimeMillis())

            val settingsObj = JSONObject().apply {
                put("relationshipStartDateIso", prefs.relationshipStartDateIso)
                put("anniversaryDateIso", prefs.anniversaryDateIso)
                put("readingFontScale", prefs.readingFontScale.toDouble())
                put("darkMode", prefs.darkMode)
            }
            root.put("settings", settingsObj)

            val chapters = dao.getAllChapters()
            val letters = dao.getAllLetters()
            val quotes = dao.getAllQuotes()
            val memories = dao.getAllMemories()
            val moments = dao.getAllMoments()
            val timeline = dao.getAllTimeline()
            val promises = dao.getAllPromises()
            val dreams = dao.getAllDreams()
            val photos = dao.getAllPhotos()
            val notes = dao.getAllNotes()

            root.put("chapters", JSONArray().apply {
                chapters.forEach { c ->
                    put(JSONObject().apply {
                        put("chapterNumber", c.chapterNumber)
                        put("title", c.title)
                        put("dateLabel", c.dateLabel)
                        put("content", c.content)
                        put("isFavorite", c.isFavorite)
                    })
                }
            })

            root.put("letters", JSONArray().apply {
                letters.forEach { l ->
                    put(JSONObject().apply {
                        put("title", l.title)
                        put("sender", l.sender)
                        put("recipient", l.recipient)
                        put("dateLabel", l.dateLabel)
                        put("content", l.content)
                        put("isFavorite", l.isFavorite)
                    })
                }
            })

            root.put("quotes", JSONArray().apply {
                quotes.forEach { q ->
                    put(JSONObject().apply {
                        put("quoteText", q.quoteText)
                        put("author", q.author)
                        put("isPersonal", q.isPersonal)
                        put("isFavorite", q.isFavorite)
                    })
                }
            })

            root.put("memories", JSONArray().apply {
                memories.forEach { m ->
                    put(JSONObject().apply {
                        put("title", m.title)
                        put("description", m.description)
                        put("dateLabel", m.dateLabel)
                        put("photoPath", m.photoPath)
                        put("specialNotes", m.specialNotes)
                        put("isFavorite", m.isFavorite)
                    })
                }
            })

            root.put("moments", JSONArray().apply {
                moments.forEach { m ->
                    put(JSONObject().apply {
                        put("title", m.title)
                        put("category", m.category)
                        put("dateLabel", m.dateLabel)
                        put("description", m.description)
                        put("isFavorite", m.isFavorite)
                    })
                }
            })

            root.put("timeline", JSONArray().apply {
                timeline.forEach { t ->
                    put(JSONObject().apply {
                        put("title", t.title)
                        put("dateLabel", t.dateLabel)
                        put("description", t.description)
                        put("sortOrder", t.sortOrder)
                        put("isFavorite", t.isFavorite)
                    })
                }
            })

            root.put("promises", JSONArray().apply {
                promises.forEach { p ->
                    put(JSONObject().apply {
                        put("title", p.title)
                        put("description", p.description)
                        put("category", p.category)
                        put("madeBy", p.madeBy)
                        put("isFavorite", p.isFavorite)
                    })
                }
            })

            root.put("dreams", JSONArray().apply {
                dreams.forEach { d ->
                    put(JSONObject().apply {
                        put("title", d.title)
                        put("category", d.category)
                        put("targetTimeframe", d.targetTimeframe)
                        put("notes", d.notes)
                        put("isAchieved", d.isAchieved)
                        put("isFavorite", d.isFavorite)
                    })
                }
            })

            root.put("photos", JSONArray().apply {
                photos.forEach { p ->
                    put(JSONObject().apply {
                        put("title", p.title)
                        put("caption", p.caption)
                        put("dateLabel", p.dateLabel)
                        put("photoPath", p.photoPath)
                        put("isFavorite", p.isFavorite)
                    })
                }
            })

            root.put("notes", JSONArray().apply {
                notes.forEach { n ->
                    put(JSONObject().apply {
                        put("title", n.title)
                        put("body", n.body)
                        put("author", n.author)
                        put("mood", n.mood)
                        put("dateLabel", n.dateLabel)
                        put("isFavorite", n.isFavorite)
                    })
                }
            })

            val totalItems = chapters.size + letters.size + quotes.size + memories.size +
                moments.size + timeline.size + promises.size + dreams.size + photos.size + notes.size

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(root.toString(2).toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(IllegalStateException("Unable to open backup destination"))

            Result.success(totalItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        dao: KeepsakeDao,
        prefsRepo: PreferencesRepository
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val raw = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext Result.failure(IllegalStateException("Unable to read backup file"))

            val root = JSONObject(raw)
            if (root.optString("app") != "Journey of Two Souls") {
                return@withContext Result.failure(IllegalArgumentException("Not a Journey of Two Souls backup file"))
            }

            var count = 0

            root.optJSONObject("settings")?.let { s ->
                prefsRepo.setRelationshipStartDateIso(s.optString("relationshipStartDateIso", ""))
                prefsRepo.setAnniversaryDateIso(s.optString("anniversaryDateIso", ""))
                if (s.has("readingFontScale")) {
                    prefsRepo.setReadingFontScale(s.optDouble("readingFontScale", 1.0).toFloat())
                }
                if (s.has("darkMode")) {
                    prefsRepo.setDarkMode(s.optBoolean("darkMode", false))
                }
            }

            dao.clearChapters()
            root.optJSONArray("chapters")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertChapter(
                        StoryChapterEntity(
                            chapterNumber = o.optInt("chapterNumber", i + 1),
                            title = o.optString("title"),
                            dateLabel = o.optString("dateLabel"),
                            content = o.optString("content"),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearLetters()
            root.optJSONArray("letters")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertLetter(
                        LoveLetterEntity(
                            title = o.optString("title"),
                            sender = o.optString("sender"),
                            recipient = o.optString("recipient"),
                            dateLabel = o.optString("dateLabel"),
                            content = o.optString("content"),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearQuotes()
            root.optJSONArray("quotes")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertQuote(
                        LoveQuoteEntity(
                            quoteText = o.optString("quoteText"),
                            author = o.optString("author"),
                            isPersonal = o.optBoolean("isPersonal", false),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearMemories()
            root.optJSONArray("memories")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertMemory(
                        MemoryEntity(
                            title = o.optString("title"),
                            description = o.optString("description"),
                            dateLabel = o.optString("dateLabel"),
                            photoPath = o.optString("photoPath"),
                            specialNotes = o.optString("specialNotes"),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearMoments()
            root.optJSONArray("moments")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertMoment(
                        SpecialMomentEntity(
                            title = o.optString("title"),
                            category = o.optString("category"),
                            dateLabel = o.optString("dateLabel"),
                            description = o.optString("description"),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearTimeline()
            root.optJSONArray("timeline")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertMilestone(
                        TimelineMilestoneEntity(
                            title = o.optString("title"),
                            dateLabel = o.optString("dateLabel"),
                            description = o.optString("description"),
                            sortOrder = o.optInt("sortOrder", i + 1),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearPromises()
            root.optJSONArray("promises")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertPromise(
                        PromiseEntity(
                            title = o.optString("title"),
                            description = o.optString("description"),
                            category = o.optString("category"),
                            madeBy = o.optString("madeBy"),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearDreams()
            root.optJSONArray("dreams")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertDream(
                        FutureDreamEntity(
                            title = o.optString("title"),
                            category = o.optString("category"),
                            targetTimeframe = o.optString("targetTimeframe"),
                            notes = o.optString("notes"),
                            isAchieved = o.optBoolean("isAchieved", false),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearPhotos()
            root.optJSONArray("photos")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertPhoto(
                        GalleryPhotoEntity(
                            title = o.optString("title"),
                            caption = o.optString("caption"),
                            dateLabel = o.optString("dateLabel"),
                            photoPath = o.optString("photoPath"),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            dao.clearNotes()
            root.optJSONArray("notes")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertNote(
                        PersonalNoteEntity(
                            title = o.optString("title"),
                            body = o.optString("body"),
                            author = o.optString("author"),
                            mood = o.optString("mood"),
                            dateLabel = o.optString("dateLabel"),
                            isFavorite = o.optBoolean("isFavorite", false)
                        )
                    )
                    count++
                }
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
