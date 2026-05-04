# Changelog

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
