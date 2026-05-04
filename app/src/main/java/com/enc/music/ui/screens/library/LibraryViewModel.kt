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
import com.enc.music.model.FolderItem
import com.enc.music.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab { Songs, Albums, Artists, Folders }

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val folderSongs: List<Song> = emptyList(),
    val currentFolderPath: String? = null,
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

    fun selectTab(tab: LibraryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun playSong(song: Song, songsContext: List<Song> = _uiState.value.songs) {
        val index = songsContext.indexOf(song)
        val mediaItems = songsContext.map { it.toMediaItem() }
        player.setMediaItems(mediaItems, index.coerceAtLeast(0), 0L)
        player.prepare()
        player.play()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            musicRepository.syncFromMediaStore()

            launch {
                combine(
                    musicRepository.getAllSongs(),
                    musicRepository.getAllAlbums(),
                    musicRepository.getAllArtists(),
                    musicRepository.getDistinctFolders()
                ) { songs, albums, artists, allFolders ->
                    val current = _uiState.value
                    val folderPath = current.currentFolderPath
                    val folderItems = buildFolderItems(folderPath, allFolders, songs)
                    val folderSongs = if (folderPath != null) {
                        songs.filter { it.folderPath == folderPath }
                    } else {
                        emptyList()
                    }
                    current.copy(
                        songs = songs,
                        albums = albums,
                        artists = artists,
                        folders = folderItems,
                        folderSongs = folderSongs,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }
    }

    fun openFolder(folderPath: String) {
        viewModelScope.launch {
            val allFolders = musicRepository.getDistinctFolders().first()
            val allSongs = musicRepository.getAllSongs().first()
            val songsInFolder = allSongs.filter { it.folderPath == folderPath }
            val subfolders = buildFolderItems(folderPath, allFolders, allSongs)
            _uiState.value = _uiState.value.copy(
                currentFolderPath = folderPath,
                folders = subfolders,
                folderSongs = songsInFolder
            )
        }
    }

    fun navigateUp(): Boolean {
        val current = _uiState.value.currentFolderPath ?: return false
        val parent = current.substringBeforeLast("/", "")
        if (parent.isEmpty() || !parent.contains("/")) {
            viewModelScope.launch {
                val allFolders = musicRepository.getDistinctFolders().first()
                val allSongs = musicRepository.getAllSongs().first()
                _uiState.value = _uiState.value.copy(
                    currentFolderPath = null,
                    folders = buildFolderItems(null, allFolders, allSongs),
                    folderSongs = emptyList()
                )
            }
            return true
        }
        openFolder(parent)
        return true
    }

    private fun buildFolderItems(
        parentPath: String?,
        allFolders: List<String>,
        allSongs: List<Song>
    ): List<FolderItem> {
        val childPaths = if (parentPath == null) {
            allFolders.map { findRootAncestor(it, allFolders) }.distinct().sorted()
        } else {
            allFolders
                .filter { it.startsWith("$parentPath/") }
                .map { "$parentPath/" + it.removePrefix("$parentPath/").substringBefore("/") }
                .distinct()
                .sorted()
        }

        return childPaths.map { folderPath ->
            val totalSongs = allSongs.count { it.folderPath.startsWith(folderPath) }
            val subfolderCount = allFolders.count { it != folderPath && it.startsWith("$folderPath/") }
            FolderItem(
                name = folderPath.substringAfterLast("/"),
                path = folderPath,
                songCount = totalSongs,
                subfolderCount = subfolderCount
            )
        }
    }

    private fun findRootAncestor(path: String, allFolders: List<String>): String {
        val segments = path.split("/").filter { it.isNotEmpty() }
        var current = ""
        for (segment in segments) {
            current = "$current/$segment"
            val hasDirectSongs = allFolders.contains(current)
            val nextSegments = allFolders
                .filter { it.startsWith("$current/") }
                .map { it.removePrefix("$current/").substringBefore("/") }
                .distinct()
            if (hasDirectSongs || nextSegments.size > 1 || nextSegments.isEmpty()) {
                return current
            }
        }
        return path
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
