# Changelog

## [1.4.0] - 2026-05-04

### Added
- **Playlists tab** — new tab in the library for browsing and playing user-created playlists; shows playlist name and song count, tapping a playlist loads its songs into the player
- **Scan progress bar** — Database Management "Rescan Library" now shows a linear progress bar with percentage, "X of Y files imported" counter, and current file name during folder scanning
- **Preferences screen** — accessible from the app menu with a "Playback" section containing the "Auto-play after playlist select" toggle (persisted via DataStore)
- **About screen** — shows app name, version, and tagline
- `Playlist` domain model with song count
- `PreferencesRepository` backed by Jetpack DataStore for persistent user preferences
- `PlaylistDao.getSongCountForPlaylist()` query
- Playlist methods in `MusicRepository`: `getAllPlaylists`, `getSongIdsForPlaylist`, `createPlaylist`, `deletePlaylist`, `addSongToPlaylist`, `removeSongFromPlaylist`
- `PreferencesRoute` and `AboutRoute` added to type-safe navigation
- DataStore Preferences dependency (`androidx.datastore:datastore-preferences:1.1.1`)

### Changed
- **Instant library loading** — library tabs now display cached data from Room immediately on open; MediaStore sync runs in the background and the UI auto-updates via reactive Flows (eliminates the loading spinner delay for large libraries)
- `MusicRepository.scanFolder` now accepts a progress callback and pre-counts audio files before scanning to report accurate percentage progress
- `DatabaseManagementUiState` extended with scan progress fields (`scanProgress`, `scanFilesProcessed`, `scanTotalFiles`, `scanCurrentFile`)
- `LibraryUiState` extended with `playlists` field
- `LibraryTab` enum extended with `Playlists`
- `LibraryViewModel` collects playlists via `combine` and exposes `playPlaylist()` method
- App menu "Preferences" and "About" items now navigate to their respective screens
- `versionCode` bumped to 5, `versionName` to 1.4.0

## [1.3.0] - 2026-05-04

### Added
- **Enchanted Music Magic** — new screen accessible from the app menu for advanced music search and smart playlist building
  - Multi-field search filters: genre, artist, album, year (all as multi-select chips), and song duration (under 1 min, under 3 min, over 5 min, over 10 min)
  - **Target total duration** — enter a target time (e.g., 45 minutes) and the app auto-picks a shuffled subset of matching songs whose combined duration approximates the target (within 10% tolerance)
  - Results grid with song count, total duration, select all/clear, and individual song toggle
  - Each result row shows title, artist, genre, year, and duration
  - **Add to Magic List** — FAB appears when songs are selected; opens dialog to choose an existing EncMusicList or create a new one (name + description)
  - Snackbar confirmation after adding songs
  - Collapsible filter panel for more screen space when reviewing results
- **EncMusicList** (Enchanted Music List) — new database entity for curated song lists
  - `EncMusicListEntity` with auto-generated ID, unique name, description, and creation timestamp
  - `EncMusicListSongEntity` junction table linking lists to songs by full file path, with cascade delete, sort order, and added-at timestamp
  - `EncMusicListDao` with full CRUD: create/update/delete lists, add/remove songs by file path, query song paths per list, get by name or ID, song count
  - Registered in `MusicDatabase` (version 4) and wired via Hilt in `AppModule`
- **Song metadata fields** — `genre` (String), `year` (Int), `rating` (Int) added to `SongEntity` and `Song` domain model
  - Genre extracted from MediaStore (API 30+ via `MediaStore.Audio.Media.GENRE`) and from MediaMetadataRetriever for SAF folder scans (all API levels)
  - Year extracted from both MediaStore and MediaMetadataRetriever
  - Rating defaults to 0, ready for future user-driven rating UI
- **Unique file path index** on `SongEntity.filePath` — enforces uniqueness of songs by their file path as the business key
- `MagicSearchRoute` added to type-safe navigation

### Fixed
- **Folder browsing** — clicking a folder in the Folders tab now correctly navigates into it
  - Fixed `buildFolderItems` to discover child folders by extracting the next path segment from all descendant paths, instead of requiring exact matches in the distinct folders list
  - Fixed `findRootAncestor` to skip single-child intermediate directories (e.g., `/storage/emulated/0`) and land on meaningful folders where songs exist or paths branch
  - Fixed `loadLibrary` combine collector to respect `currentFolderPath` instead of always resetting to root folders

### Changed
- `MusicDatabase` bumped to version 4 (destructive migration via `fallbackToDestructiveMigration`)
- `MusicRepository.scanSongsFromMediaStore` now reads genre (conditionally on API 30+) and year columns
- `MusicRepository.scanDocTree` extracts genre and year via MediaMetadataRetriever
- Song-to-entity and entity-to-song mappers updated for new fields
- App menu now shows "Enchanted Music Magic" as the first item, above "Database Management"
- `versionCode` bumped to 4, `versionName` to 1.3.0

## [1.2.0] - 2026-05-03

### Added
- **Local SQLite database** for all music metadata — songs, albums, and artists are now stored in Room with full file paths, replacing live MediaStore queries
- **Folders tab** — new tab in the library for browsing music by folder hierarchy with recursive navigation into subdirectories and back
- **Status bar** on every library tab showing item counts (songs, albums, artists, or folders/files)
- **App menu** — overflow menu (3-dot) in the top bar with Database Management, Preferences, Help, and About items
- **Database Management screen** — shows library statistics (total songs, albums, artists, files), "Rescan Library" button with SAF folder picker for recursive scanning, and "Erase Library" button with confirmation dialog
- **Folder-based scanning** — uses DocumentFile (SAF) and MediaMetadataRetriever to scan user-selected folders recursively, extracting metadata and skipping already-imported songs
- Room entities: `SongEntity`, `AlbumEntity`, `ArtistEntity` with corresponding DAOs (`SongDao`, `AlbumDao`, `ArtistDao`)
- `FolderItem` domain model for folder browsing
- `DocumentFile` dependency for Storage Access Framework folder browsing
- Scrollable tab row to accommodate 4 tabs (Songs, Albums, Artists, Folders)

### Changed
- `MusicRepository` refactored: MediaStore is scanned once on load and synced to Room; all reads now come from Room as reactive Flows instead of repeated MediaStore queries
- `Song` model extended with `filePath` and `folderPath` fields
- `AlbumViewModel` and `ArtistViewModel` now collect Room Flows instead of one-shot suspend calls
- `LibraryViewModel` manages folder navigation state (open, navigate up) and combines multiple Flows for reactive updates
- `MusicDatabase` bumped to version 2 with destructive migration (adds songs, albums, artists tables)
- `AppModule` now provides `SongDao`, `AlbumDao`, `ArtistDao` via Hilt
- `LibraryScreen` uses `ScrollableTabRow` instead of `TabRow` for 4 tabs

## [1.1.0] - 2026-05-03

### Added
- Mini player bar visible across library, album, and artist screens with album art, title, play/pause, skip, and progress indicator
- Tapping mini player navigates to the full now-playing screen
- Notification permission request (POST_NOTIFICATIONS) on Android 13+
- NavHostViewModel to provide shared ExoPlayer instance to mini player

### Fixed
- Runtime permission flow: library now loads correctly after granting audio permission (was loading before permission dialog appeared)
- Library loads immediately on app re-open when permission is already granted
- PlaybackService is now started on app launch for proper background playback and notification controls
- PlaybackService no longer releases the shared ExoPlayer singleton on destroy, which previously broke playback
- Release APK is now signed with the debug keystore so it can be installed on devices

## [1.0.0] - 2026-05-02

### Added
- Initial project setup with Gradle Kotlin DSL and version catalog
- Library screen with tabbed browsing (Songs, Albums, Artists)
- Now-playing screen with playback controls (play/pause, skip, seek, shuffle, repeat)
- Album detail screen with song list and play-all
- Artist detail screen with album list
- Media3 ExoPlayer integration with MediaSessionService for background playback
- Room database for playlist persistence (playlists and playlist-song cross-references)
- MediaStore integration for reading device audio files
- Hilt dependency injection throughout
- Material 3 theming with dynamic color support
- Android 12+ splash screen with backward compatibility (core-splashscreen)
- Custom launcher icon across all density buckets
- Adaptive icon with dark background
- Type-safe navigation using Kotlin Serialization
- Edge-to-edge display support
- ProGuard/R8 configuration for release builds
