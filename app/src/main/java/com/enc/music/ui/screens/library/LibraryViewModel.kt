package com.enc.music.ui.screens.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.enc.music.data.repository.MusicRepository
import com.enc.music.model.Album
import com.enc.music.model.Artist
import com.enc.music.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab { Songs, Albums, Artists }

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.Songs,
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val player: ExoPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState

    init {
        loadLibrary()
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun playSong(song: Song) {
        val songs = _uiState.value.songs
        val index = songs.indexOf(song)
        val mediaItems = songs.map { it.toMediaItem() }
        player.setMediaItems(mediaItems, index, 0L)
        player.prepare()
        player.play()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            val songs = musicRepository.querySongs()
            val albums = musicRepository.queryAlbums()
            val artists = musicRepository.queryArtists()
            _uiState.value = LibraryUiState(
                songs = songs,
                albums = albums,
                artists = artists,
                selectedTab = _uiState.value.selectedTab,
                isLoading = false
            )
        }
    }
}

fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArtUri)
                .build()
        )
        .build()
}
