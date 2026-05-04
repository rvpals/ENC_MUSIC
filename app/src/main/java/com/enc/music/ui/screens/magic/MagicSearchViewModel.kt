package com.enc.music.ui.screens.magic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enc.music.data.local.dao.EncMusicListDao
import com.enc.music.data.local.entity.EncMusicListEntity
import com.enc.music.data.local.entity.EncMusicListSongEntity
import com.enc.music.data.repository.MusicRepository
import com.enc.music.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DurationFilter(val label: String) {
    ANY("Any"),
    UNDER_1("Under 1 min"),
    UNDER_3("Under 3 min"),
    OVER_5("Over 5 min"),
    OVER_10("Over 10 min")
}

data class MagicSearchFilters(
    val genres: Set<String> = emptySet(),
    val artists: Set<String> = emptySet(),
    val albums: Set<String> = emptySet(),
    val years: Set<Int> = emptySet(),
    val durationFilter: DurationFilter = DurationFilter.ANY,
    val targetDurationMinutes: Int? = null
)

data class MagicSearchUiState(
    val allSongs: List<Song> = emptyList(),
    val results: List<Song> = emptyList(),
    val selectedSongIds: Set<Long> = emptySet(),
    val filters: MagicSearchFilters = MagicSearchFilters(),
    val availableGenres: List<String> = emptyList(),
    val availableArtists: List<String> = emptyList(),
    val availableAlbums: List<String> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val magicLists: List<EncMusicListEntity> = emptyList(),
    val showAddToListDialog: Boolean = false,
    val showCreateListDialog: Boolean = false,
    val hasSearched: Boolean = false,
    val addedMessage: String? = null
)

@HiltViewModel
class MagicSearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val encMusicListDao: EncMusicListDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MagicSearchUiState())
    val uiState: StateFlow<MagicSearchUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val songs = musicRepository.getAllSongs().first()
            val genres = songs.map { it.genre }.filter { it.isNotBlank() }.distinct().sorted()
            val artists = songs.map { it.artist }.filter { it.isNotBlank() }.distinct().sorted()
            val albums = songs.map { it.album }.filter { it.isNotBlank() }.distinct().sorted()
            val years = songs.map { it.year }.filter { it > 0 }.distinct().sorted()
            val lists = encMusicListDao.getAllLists().first()
            _uiState.value = _uiState.value.copy(
                allSongs = songs,
                availableGenres = genres,
                availableArtists = artists,
                availableAlbums = albums,
                availableYears = years,
                magicLists = lists
            )
        }
    }

    fun updateFilters(filters: MagicSearchFilters) {
        _uiState.value = _uiState.value.copy(filters = filters)
    }

    fun search() {
        val state = _uiState.value
        val filters = state.filters
        var filtered = state.allSongs

        if (filters.genres.isNotEmpty()) {
            filtered = filtered.filter { song ->
                filters.genres.any { song.genre.contains(it, ignoreCase = true) }
            }
        }
        if (filters.artists.isNotEmpty()) {
            filtered = filtered.filter { it.artist in filters.artists }
        }
        if (filters.albums.isNotEmpty()) {
            filtered = filtered.filter { it.album in filters.albums }
        }
        if (filters.years.isNotEmpty()) {
            filtered = filtered.filter { it.year in filters.years }
        }

        filtered = when (filters.durationFilter) {
            DurationFilter.ANY -> filtered
            DurationFilter.UNDER_1 -> filtered.filter { it.duration < 60_000 }
            DurationFilter.UNDER_3 -> filtered.filter { it.duration < 180_000 }
            DurationFilter.OVER_5 -> filtered.filter { it.duration > 300_000 }
            DurationFilter.OVER_10 -> filtered.filter { it.duration > 600_000 }
        }

        val results = if (filters.targetDurationMinutes != null && filters.targetDurationMinutes > 0) {
            selectSongsForTargetDuration(filtered, filters.targetDurationMinutes.toLong() * 60_000)
        } else {
            filtered
        }

        _uiState.value = state.copy(
            results = results,
            selectedSongIds = emptySet(),
            hasSearched = true
        )
    }

    private fun selectSongsForTargetDuration(songs: List<Song>, targetMs: Long): List<Song> {
        if (songs.isEmpty()) return emptyList()
        val shuffled = songs.shuffled()
        val selected = mutableListOf<Song>()
        var totalDuration = 0L
        val tolerance = targetMs / 10

        for (song in shuffled) {
            if (totalDuration >= targetMs) break
            if (totalDuration + song.duration <= targetMs + tolerance) {
                selected.add(song)
                totalDuration += song.duration
            }
        }

        if (selected.isEmpty() && shuffled.isNotEmpty()) {
            selected.add(shuffled.first())
        }

        return selected.sortedBy { it.title }
    }

    fun toggleSongSelection(songId: Long) {
        val current = _uiState.value.selectedSongIds
        _uiState.value = _uiState.value.copy(
            selectedSongIds = if (songId in current) current - songId else current + songId
        )
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedSongIds = _uiState.value.results.map { it.id }.toSet()
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedSongIds = emptySet())
    }

    fun showAddToListDialog() {
        viewModelScope.launch {
            val lists = encMusicListDao.getAllLists().first()
            _uiState.value = _uiState.value.copy(
                magicLists = lists,
                showAddToListDialog = true
            )
        }
    }

    fun dismissAddToListDialog() {
        _uiState.value = _uiState.value.copy(showAddToListDialog = false)
    }

    fun showCreateListDialog() {
        _uiState.value = _uiState.value.copy(showCreateListDialog = true)
    }

    fun dismissCreateListDialog() {
        _uiState.value = _uiState.value.copy(showCreateListDialog = false)
    }

    fun createListAndAdd(name: String, description: String) {
        viewModelScope.launch {
            val listId = encMusicListDao.createList(
                EncMusicListEntity(name = name, description = description)
            )
            addSelectedToList(listId)
            _uiState.value = _uiState.value.copy(showCreateListDialog = false)
        }
    }

    fun addSelectedToList(listId: Long) {
        viewModelScope.launch {
            val selectedSongs = _uiState.value.results
                .filter { it.id in _uiState.value.selectedSongIds }
            val existingCount = encMusicListDao.getSongCount(listId)
            selectedSongs.forEachIndexed { index, song ->
                encMusicListDao.addSong(
                    EncMusicListSongEntity(
                        listId = listId,
                        filePath = song.filePath,
                        sortOrder = existingCount + index
                    )
                )
            }
            val list = encMusicListDao.getListById(listId)
            _uiState.value = _uiState.value.copy(
                showAddToListDialog = false,
                selectedSongIds = emptySet(),
                addedMessage = "Added ${selectedSongs.size} songs to \"${list?.name ?: "list"}\""
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(addedMessage = null)
    }
}
