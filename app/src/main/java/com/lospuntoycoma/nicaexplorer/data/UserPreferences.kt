package com.lospuntoycoma.nicaexplorer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nicaexplorer_prefs")

/**
 * Preferencias y datos locales del usuario persistidos con DataStore.
 * Los lugares guardados y el historial se almacenan por usuario (uid)
 * para no mezclar información entre cuentas en el mismo dispositivo.
 */
object UserPreferences {

    private lateinit var appContext: Context

    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun savedPlacesKey(uid: String) = stringSetPreferencesKey("lugares_guardados_$uid")
    private fun historyKey(uid: String) = stringSetPreferencesKey("historial_$uid")

    fun darkThemeFlow(): Flow<Boolean?> =
        appContext.dataStore.data.map { it[DARK_THEME_KEY] }

    fun notificationsFlow(): Flow<Boolean> =
        appContext.dataStore.data.map { it[NOTIFICATIONS_KEY] ?: true }

    suspend fun setDarkTheme(enabled: Boolean?) {
        appContext.dataStore.edit { prefs ->
            if (enabled == null) {
                prefs.remove(DARK_THEME_KEY)
            } else {
                prefs[DARK_THEME_KEY] = enabled
            }
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_KEY] = enabled
        }
    }

    fun savedPlacesFlow(uid: String): Flow<Set<String>> =
        appContext.dataStore.data.map { it[savedPlacesKey(uid)] ?: emptySet() }

    suspend fun toggleSavedPlace(uid: String, monumentId: String) {
        appContext.dataStore.edit { prefs ->
            val key = savedPlacesKey(uid)
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (monumentId in current) current - monumentId else current + monumentId
        }
    }

    fun historyFlow(uid: String): Flow<Map<String, Long>> =
        appContext.dataStore.data.map { prefs ->
            (prefs[historyKey(uid)] ?: emptySet())
                .mapNotNull { entry ->
                    val parts = entry.split("|")
                    if (parts.size == 2) {
                        parts[0] to (parts[1].toLongOrNull() ?: 0L)
                    } else {
                        null
                    }
                }
                .toMap()
        }

    suspend fun recordExploration(uid: String, monumentId: String) {
        val timestamp = System.currentTimeMillis()
        appContext.dataStore.edit { prefs ->
            val key = historyKey(uid)
            val current = prefs[key] ?: emptySet()
            val updated = (current.filterNot { it.startsWith("$monumentId|") } + "$monumentId|$timestamp").toSet()
            prefs[key] = updated
        }
    }
}
