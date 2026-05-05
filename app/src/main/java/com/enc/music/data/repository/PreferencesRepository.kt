package com.enc.music.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "enc_music_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val AUTO_PLAY_PLAYLIST = booleanPreferencesKey("auto_play_playlist")
    }

    val autoPlayPlaylist: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.AUTO_PLAY_PLAYLIST] ?: false }

    suspend fun setAutoPlayPlaylist(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.AUTO_PLAY_PLAYLIST] = enabled }
    }
}
