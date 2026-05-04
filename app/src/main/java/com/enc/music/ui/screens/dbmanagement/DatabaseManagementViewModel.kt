package com.enc.music.ui.screens.dbmanagement

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enc.music.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DatabaseManagementUiState(
    val songCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val fileCount: Int = 0,
    val isLoading: Boolean = true,
    val isScanning: Boolean = false,
    val scanMessage: String? = null
)

@HiltViewModel
class DatabaseManagementViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatabaseManagementUiState())
    val uiState: StateFlow<DatabaseManagementUiState> = _uiState

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val songCount = musicRepository.getSongCount()
            val albumCount = musicRepository.getAlbumCount()
            val artistCount = musicRepository.getArtistCount()
            val fileCount = musicRepository.getFileCount()
            _uiState.value = DatabaseManagementUiState(
                songCount = songCount,
                albumCount = albumCount,
                artistCount = artistCount,
                fileCount = fileCount,
                isLoading = false
            )
        }
    }

    fun scanFolder(folderUri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, scanMessage = "Scanning...")
            try {
                musicRepository.scanFolder(folderUri, context)
                _uiState.value = _uiState.value.copy(isScanning = false, scanMessage = "Scan complete")
                loadStats()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanMessage = "Scan failed: ${e.message}"
                )
            }
        }
    }

    fun eraseLibrary() {
        viewModelScope.launch {
            musicRepository.eraseLibrary()
            loadStats()
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(scanMessage = null)
    }
}
