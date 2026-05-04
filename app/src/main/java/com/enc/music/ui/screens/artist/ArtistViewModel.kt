package com.enc.music.ui.screens.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enc.music.data.repository.MusicRepository
import com.enc.music.model.Album
import com.enc.music.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistUiState(
    val artistName: String = "",
    val albums: List<Album> = emptyList(),
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState: StateFlow<ArtistUiState> = _uiState

    fun loadArtist(artistId: Long) {
        viewModelScope.launch {
            combine(
                musicRepository.getAlbumsForArtist(artistId),
                musicRepository.getSongsForArtist(artistId)
            ) { albums, songs ->
                ArtistUiState(
                    artistName = songs.firstOrNull()?.artist ?: "",
                    albums = albums,
                    songs = songs,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
