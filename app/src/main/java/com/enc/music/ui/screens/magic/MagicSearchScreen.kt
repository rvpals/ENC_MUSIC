package com.enc.music.ui.screens.magic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.enc.music.model.Song
import com.enc.music.ui.components.formatDuration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MagicSearchScreen(
    onBack: () -> Unit,
    viewModel: MagicSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var filtersExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.addedMessage) {
        uiState.addedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enchanted Music Magic") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.selectedSongIds.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.showAddToListDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Add to Magic List")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { filtersExpanded = !filtersExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Search Filters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (filtersExpanded) "Collapse" else "Expand",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = filtersExpanded) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            FilterSection(uiState, viewModel)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.search()
                                filtersExpanded = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Search")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.updateFilters(MagicSearchFilters())
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }

            if (uiState.hasSearched) {
                ResultsHeader(uiState, viewModel)
                if (uiState.results.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No songs match your filters",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.results, key = { it.id }) { song ->
                            SelectableSongItem(
                                song = song,
                                isSelected = song.id in uiState.selectedSongIds,
                                onClick = { viewModel.toggleSongSelection(song.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showAddToListDialog) {
        AddToListDialog(
            lists = uiState.magicLists,
            selectedCount = uiState.selectedSongIds.size,
            onSelectList = { viewModel.addSelectedToList(it) },
            onCreateNew = { viewModel.showCreateListDialog() },
            onDismiss = { viewModel.dismissAddToListDialog() }
        )
    }

    if (uiState.showCreateListDialog) {
        CreateListDialog(
            onConfirm = { name, desc -> viewModel.createListAndAdd(name, desc) },
            onDismiss = { viewModel.dismissCreateListDialog() }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(uiState: MagicSearchUiState, viewModel: MagicSearchViewModel) {
    val filters = uiState.filters
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.verticalScroll(scrollState)) {
        if (uiState.availableGenres.isNotEmpty()) {
            Text("Genre", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                uiState.availableGenres.forEach { genre ->
                    FilterChip(
                        selected = genre in filters.genres,
                        onClick = {
                            val new = if (genre in filters.genres) filters.genres - genre else filters.genres + genre
                            viewModel.updateFilters(filters.copy(genres = new))
                        },
                        label = { Text(genre, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (uiState.availableArtists.isNotEmpty()) {
            Text("Artist", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            ChipSelector(
                items = uiState.availableArtists,
                selectedItems = filters.artists,
                onToggle = { artist ->
                    val new = if (artist in filters.artists) filters.artists - artist else filters.artists + artist
                    viewModel.updateFilters(filters.copy(artists = new))
                }
            )
            Spacer(Modifier.height(10.dp))
        }

        if (uiState.availableAlbums.isNotEmpty()) {
            Text("Album", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            ChipSelector(
                items = uiState.availableAlbums,
                selectedItems = filters.albums,
                onToggle = { album ->
                    val new = if (album in filters.albums) filters.albums - album else filters.albums + album
                    viewModel.updateFilters(filters.copy(albums = new))
                }
            )
            Spacer(Modifier.height(10.dp))
        }

        if (uiState.availableYears.isNotEmpty()) {
            Text("Year", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                uiState.availableYears.forEach { year ->
                    FilterChip(
                        selected = year in filters.years,
                        onClick = {
                            val new = if (year in filters.years) filters.years - year else filters.years + year
                            viewModel.updateFilters(filters.copy(years = new))
                        },
                        label = { Text(year.toString(), style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Text("Song Duration", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DurationFilter.entries.forEach { df ->
                FilterChip(
                    selected = filters.durationFilter == df,
                    onClick = { viewModel.updateFilters(filters.copy(durationFilter = df)) },
                    label = { Text(df.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        Text("Target Total Duration (minutes)", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var text by remember(filters.targetDurationMinutes) {
                mutableStateOf(filters.targetDurationMinutes?.toString() ?: "")
            }
            OutlinedTextField(
                value = text,
                onValueChange = { value ->
                    text = value
                    val minutes = value.toIntOrNull()
                    viewModel.updateFilters(filters.copy(targetDurationMinutes = minutes))
                },
                placeholder = { Text("e.g. 45") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(120.dp)
            )
            Text(
                "Auto-pick songs to fill this time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSelector(
    items: List<String>,
    selectedItems: Set<String>,
    onToggle: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayItems = if (expanded) items else items.take(8)

    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        displayItems.forEach { item ->
            FilterChip(
                selected = item in selectedItems,
                onClick = { onToggle(item) },
                label = {
                    Text(
                        item,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        if (items.size > 8) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Show less" else "+${items.size - 8} more")
            }
        }
    }
}

@Composable
private fun ResultsHeader(uiState: MagicSearchUiState, viewModel: MagicSearchViewModel) {
    val totalDuration = uiState.results.sumOf { it.duration }
    val selectedDuration = uiState.results
        .filter { it.id in uiState.selectedSongIds }
        .sumOf { it.duration }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "${uiState.results.size} results (${formatDuration(totalDuration)})",
                    style = MaterialTheme.typography.labelMedium
                )
                if (uiState.selectedSongIds.isNotEmpty()) {
                    Text(
                        "${uiState.selectedSongIds.size} selected (${formatDuration(selectedDuration)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row {
                IconButton(onClick = { viewModel.selectAll() }) {
                    Icon(
                        Icons.Default.SelectAll,
                        contentDescription = "Select All",
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (uiState.selectedSongIds.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearSelection() }) {
                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableSongItem(song: Song, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = song.album,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                buildString {
                    append(song.artist)
                    if (song.genre.isNotBlank()) append(" · ${song.genre}")
                    if (song.year > 0) append(" · ${song.year}")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            formatDuration(song.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AddToListDialog(
    lists: List<com.enc.music.data.local.entity.EncMusicListEntity>,
    selectedCount: Int,
    onSelectList: (Long) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add $selectedCount songs to Magic List") },
        text = {
            Column {
                if (lists.isEmpty()) {
                    Text(
                        "No lists yet. Create one to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Choose a list:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    lists.forEach { list ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectList(list.id) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(list.name, style = MaterialTheme.typography.bodyMedium)
                                if (list.description.isNotBlank()) {
                                    Text(
                                        list.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onCreateNew) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("New List")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CreateListDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Magic List") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), description.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Create & Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
