package com.enc.music.ui.screens.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.enc.music.data.repository.MusicRepository
import com.enc.music.model.Song
import com.enc.music.ui.screens.library.toMediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiState(
    val albumTitle: String = "",
    val artist: String = "",
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val player: ExoPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState

    fun loadAlbum(albumId: Long) {
        viewModelScope.launch {
            val songs = musicRepository.getSongsForAlbum(albumId)
            _uiState.value = AlbumUiState(
                albumTitle = songs.firstOrNull()?.album ?: "",
                artist = songs.firstOrNull()?.artist ?: "",
                songs = songs,
                isLoading = false
            )
        }
    }

    fun playSong(song: Song) {
        val songs = _uiState.value.songs
        val index = songs.indexOf(song)
        val mediaItems = songs.map { it.toMediaItem() }
        player.setMediaItems(mediaItems, index, 0L)
        player.prepare()
        player.play()
    }

    fun playAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) {
            val mediaItems = songs.map { it.toMediaItem() }
            player.setMediaItems(mediaItems)
            player.prepare()
            player.play()
        }
    }
}
