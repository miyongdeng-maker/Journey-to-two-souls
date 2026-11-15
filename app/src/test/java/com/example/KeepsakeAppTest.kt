package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.KeepsakeDatabase
import com.example.data.LoveLetterEntity
import com.example.data.LoveQuoteEntity
import com.example.data.PersonalNoteEntity
import com.example.data.PreferencesRepository
import com.example.data.PromiseEntity
import com.example.data.StoryChapterEntity
import com.example.security.AuthManager
import com.example.security.EncryptedPasswordRecord
import com.example.util.DateHelper
import com.example.viewmodel.KeepsakeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KeepsakeAppTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `auth manager hashes with unique salts and verifies offline without plaintext`() {
        val record1 = AuthManager.hashAndEncryptPassword("JuliaYB")
        val record2 = AuthManager.hashAndEncryptPassword("JuliaYB")

        assertFalse(record1.encryptedHashBase64.contains("JuliaYB"))
        assertFalse(record1.saltBase64.contains("JuliaYB"))
        assertTrue(record1.saltBase64 != record2.saltBase64)
        assertTrue(record1.encryptedHashBase64 != record2.encryptedHashBase64)

        assertTrue(AuthManager.verifyPassword("JuliaYB", record1))
        assertTrue(AuthManager.verifyPassword("JuliaYB", record2))
        assertFalse(AuthManager.verifyPassword("wrongpass", record1))
        assertFalse(AuthManager.verifyPassword("", record1))
        assertFalse(AuthManager.verifyPassword("JuliaYB", EncryptedPasswordRecord("", "", "")))
    }

    @Test
    fun `preferences repository initializes JuliaYB and supports changing password`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesRepository(context)
        prefs.ensurePasswordInitialized()

        assertTrue(prefs.verifyPassword("JuliaYB"))
        assertFalse(prefs.verifyPassword("Wrong123"))

        // Reject change when current password is wrong
        assertFalse(prefs.changePassword("Wrong123", "NewSecret99"))
        assertTrue(prefs.verifyPassword("JuliaYB"))

        // Accept change when current password matches
        assertTrue(prefs.changePassword("JuliaYB", "NewSecret99"))
        assertFalse(prefs.verifyPassword("JuliaYB"))
        assertTrue(prefs.verifyPassword("NewSecret99"))

        // Restore default for subsequent isolated tests
        assertTrue(prefs.changePassword("NewSecret99", "JuliaYB"))
    }

    @Test
    fun `offline room database persists CRUD across all keepsake tables`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KeepsakeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.keepsakeDao()

        val chId = dao.insertChapter(
            StoryChapterEntity(chapterNumber = 1, title = "Sunset Walk", dateLabel = "2026-11-14", content = "Walking together.")
        )
        val ltId = dao.insertLetter(
            LoveLetterEntity(title = "My Letter", sender = "Miyong", recipient = "Juliana", dateLabel = "2026-11-14", content = "Forever.")
        )
        val qtId = dao.insertQuote(
            LoveQuoteEntity(quoteText = "Always and forever", author = "Us", isPersonal = true)
        )
        val prId = dao.insertPromise(
            PromiseEntity(title = "Patience", description = "Always listen", category = "Promise", madeBy = "Both")
        )
        val ntId = dao.insertNote(
            PersonalNoteEntity(title = "Morning", body = "Good morning love", author = "Miyong", mood = "Tender", dateLabel = "Today")
        )

        assertTrue(chId > 0 && ltId > 0 && qtId > 0 && prId > 0 && ntId > 0)
        assertEquals(1, dao.observeChapters().first().size)
        assertEquals(1, dao.observeLetters().first().size)
        assertEquals(1, dao.observeQuotes().first().size)
        assertEquals(1, dao.observePromises().first().size)
        assertEquals(1, dao.observeNotes().first().size)

        db.close()
    }

    @Test
    fun `date helper computes days together and anniversary countdown`() {
        val todayIso = DateHelper.todayIso()
        assertEquals(1L, DateHelper.daysSince(todayIso))
        assertEquals(0L, DateHelper.daysUntilNextAnniversary(todayIso))
        assertNotNull(DateHelper.formatDisplayDate(todayIso))
    }

    @Test
    fun `cuj homepage displays photo publicly then password unlocks dashboard and locks again`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = KeepsakeViewModel(app)

        composeTestRule.setContent {
            JourneyOfTwoSoulsApp(viewModel = viewModel, skipSplash = true)
        }
        composeTestRule.waitForIdle()

        // 1. Homepage couple photo & title are visible without password
        composeTestRule.onNodeWithTag("homepage_couple_photo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("homepage_title").assertIsDisplayed()

        // 2. Tapping Enter Our Love Story opens Password screen
        composeTestRule.onNodeWithTag("enter_love_story_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 3. Incorrect password shows friendly error & stays locked
        composeTestRule.onNodeWithTag("password_input").performScrollTo().performTextReplacement("wrongpassword")
        composeTestRule.onNodeWithTag("unlock_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            viewModel.authError.value != null
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("password_error_text").performScrollTo().assertIsDisplayed()
        assertFalse(viewModel.isUnlocked.value)

        // 4. Correct password via UI unlocks Dashboard and shows sections
        composeTestRule.onNodeWithTag("password_input").performScrollTo().performTextReplacement("JuliaYB")
        composeTestRule.onNodeWithTag("unlock_button").performScrollTo().performClick()
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            viewModel.isUnlocked.value
        }
        composeTestRule.waitForIdle()
        assertTrue(viewModel.isUnlocked.value)

        // 5. Navigating to Our Love Story works
        composeTestRule.onNodeWithTag("section_our_love_story").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("section_our_love_story").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Our Love Story").assertIsDisplayed()

        // 6. Locking returns to protected state and redirects to LockScreen
        viewModel.lockKeepsake()
        composeTestRule.waitForIdle()
        assertFalse(viewModel.isUnlocked.value)
        composeTestRule.onNodeWithTag("password_input").assertIsDisplayed()
    }
}
