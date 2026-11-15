package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.security.AuthManager
import com.example.security.EncryptedPasswordRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class AppPreferences(
    val darkMode: Boolean = false,
    val readingFontScale: Float = 1.0f,
    val inactivityTimeoutMinutes: Int = 5,
    val musicEnabled: Boolean = false,
    val relationshipStartDateIso: String = "",
    val anniversaryDateIso: String = "",
    val heroPhotoPath: String = ""
)

class PreferencesRepository(private val context: Context) {

    private val dataStore: DataStore<Preferences> = getOrCreateDataStore(context.applicationContext)

    private object Keys {
        val PASSWORD_HASH_ENC = stringPreferencesKey("password_hash_enc")
        val PASSWORD_SALT = stringPreferencesKey("password_salt")
        val PASSWORD_IV = stringPreferencesKey("password_iv")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val READING_FONT_SCALE = floatPreferencesKey("reading_font_scale")
        val INACTIVITY_TIMEOUT_MIN = intPreferencesKey("inactivity_timeout_minutes")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val RELATIONSHIP_START_ISO = stringPreferencesKey("relationship_start_iso")
        val ANNIVERSARY_DATE_ISO = stringPreferencesKey("anniversary_date_iso")
        val HERO_PHOTO_PATH = stringPreferencesKey("hero_photo_path")
    }

    val preferencesFlow: Flow<AppPreferences> = dataStore.data.map { prefs ->
        AppPreferences(
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            readingFontScale = prefs[Keys.READING_FONT_SCALE] ?: 1.0f,
            inactivityTimeoutMinutes = prefs[Keys.INACTIVITY_TIMEOUT_MIN] ?: 5,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: false,
            relationshipStartDateIso = prefs[Keys.RELATIONSHIP_START_ISO] ?: "",
            anniversaryDateIso = prefs[Keys.ANNIVERSARY_DATE_ISO] ?: "",
            heroPhotoPath = prefs[Keys.HERO_PHOTO_PATH] ?: ""
        )
    }

    /**
     * Ensures the initial password ("JuliaYB") is stored strictly as an encrypted
     * salted PBKDF2 hash on first launch and never in plain text.
     */
    suspend fun ensurePasswordInitialized() {
        val prefs = dataStore.data.first()
        if (prefs[Keys.PASSWORD_HASH_ENC].isNullOrEmpty()) {
            val record = withContext(Dispatchers.Default) {
                AuthManager.hashAndEncryptPassword(INITIAL_PASSWORD)
            }
            savePasswordRecord(record)
        }
    }

    suspend fun verifyPassword(candidate: String): Boolean {
        ensurePasswordInitialized()
        val prefs = dataStore.data.first()
        val enc = prefs[Keys.PASSWORD_HASH_ENC].orEmpty()
        val salt = prefs[Keys.PASSWORD_SALT].orEmpty()
        val iv = prefs[Keys.PASSWORD_IV].orEmpty()
        if (enc.isEmpty() || salt.isEmpty()) return false
        return withContext(Dispatchers.Default) {
            AuthManager.verifyPassword(
                plainPassword = candidate,
                record = EncryptedPasswordRecord(enc, salt, iv)
            )
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Boolean {
        if (!AuthManager.isPasswordPolicyValid(newPassword)) return false
        if (!verifyPassword(currentPassword)) return false
        val newRecord = withContext(Dispatchers.Default) {
            AuthManager.hashAndEncryptPassword(newPassword)
        }
        savePasswordRecord(newRecord)
        return true
    }

    private suspend fun savePasswordRecord(record: EncryptedPasswordRecord) {
        dataStore.edit { prefs ->
            prefs[Keys.PASSWORD_HASH_ENC] = record.encryptedHashBase64
            prefs[Keys.PASSWORD_SALT] = record.saltBase64
            prefs[Keys.PASSWORD_IV] = record.ivBase64
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setReadingFontScale(scale: Float) {
        dataStore.edit {
            it[Keys.READING_FONT_SCALE] = scale.coerceIn(0.85f, 1.45f)
        }
    }

    suspend fun setInactivityTimeoutMinutes(minutes: Int) {
        dataStore.edit { it[Keys.INACTIVITY_TIMEOUT_MIN] = minutes }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setRelationshipStartDateIso(isoDate: String) {
        dataStore.edit { it[Keys.RELATIONSHIP_START_ISO] = isoDate }
    }

    suspend fun setAnniversaryDateIso(isoDate: String) {
        dataStore.edit { it[Keys.ANNIVERSARY_DATE_ISO] = isoDate }
    }

    suspend fun setHeroPhotoPath(path: String) {
        dataStore.edit { it[Keys.HERO_PHOTO_PATH] = path }
    }

    companion object {
        private const val INITIAL_PASSWORD = "JuliaYB"
        private const val DATASTORE_NAME = "journey_two_souls_prefs"
        private val stores = ConcurrentHashMap<String, DataStore<Preferences>>()

        private fun getOrCreateDataStore(appContext: Context): DataStore<Preferences> {
            val file = appContext.preferencesDataStoreFile(DATASTORE_NAME)
            val key = file.canonicalPath
            return stores.getOrPut(key) {
                PreferenceDataStoreFactory.create(produceFile = { file })
            }
        }
    }
}
