# Architecture

## Overview

ENC Music follows a layered MVVM architecture with unidirectional data flow.

```
┌─────────────────────────────────────┐
│           UI Layer (Compose)        │
│  Screens → observe → ViewModels    │
├─────────────────────────────────────┤
│           Domain Layer              │
│  Models (Song, Album, Artist,      │
│          FolderItem, Playlist)     │
├─────────────────────────────────────┤
│           Data Layer                │
│  MusicRepository                   │
│    ← MediaStore (scan/sync)        │
│    ← Room Database (read/write)    │
│    ← DocumentFile (folder scan)    │
│  PreferencesRepository ← DataStore │
│  EncMusicListDao ← Room Database   │
│  PlaylistDao ← Room Database       │
├─────────────────────────────────────┤
│         Service Layer               │
│  PlaybackService ← Media3 ExoPlayer│
└─────────────────────────────────────┘
```

## Layers

### UI Layer

Each screen has a dedicated ViewModel that exposes a `StateFlow<UiState>`. Composables collect this state and render accordingly. User actions call ViewModel methods which update state or trigger side effects.

- `LibraryScreen` / `LibraryViewModel` — tabbed song/album/artist/folder/playlist browsing with status bar, overflow menu
- `PlayerScreen` / `PlayerViewModel` — now-playing with transport controls
- `AlbumScreen` / `AlbumViewModel` — album detail with song list
- `ArtistScreen` / `ArtistViewModel` — artist detail with album list
- `DatabaseManagementScreen` / `DatabaseManagementViewModel` — library stats, rescan with progress, erase
- `MagicSearchScreen` / `MagicSearchViewModel` — advanced multi-field search with duration targeting, song selection, and "Add to Magic List" workflow
- `PreferencesScreen` / `PreferencesViewModel` — user preferences with DataStore persistence
- `AboutScreen` — app info and version display

### Navigation

Type-safe navigation using `@Serializable` route objects with Navigation Compose. All routes are defined in `Routes.kt`:

- `LibraryRoute` — start destination
- `AlbumRoute(albumId)` — album detail
- `ArtistRoute(artistId)` — artist detail
- `PlayerRoute` — now-playing
- `DatabaseManagementRoute` — database management
- `MagicSearchRoute` — Enchanted Music Magic search screen
- `PreferencesRoute` — user preferences
- `AboutRoute` — about screen

`MusicNavHost` wraps the `NavHost` in a `Column` with a `MiniPlayer` at the bottom. The mini player is visible on all screens except `PlayerRoute` and provides quick access to playback controls without navigating away. A `NavHostViewModel` exposes the shared ExoPlayer instance to the mini player.

### Data Layer

**MusicRepository** is the central data coordinator:
- **Sync**: Scans MediaStore on app launch (in background) and writes all song/album/artist metadata to Room (including genre, year); UI displays cached data immediately without blocking
- **Read**: All UI queries read from Room via reactive `Flow`s (not live MediaStore queries)
- **Playlists**: Full playlist CRUD — create, delete, add/remove songs, query with song counts
- **Folder scan**: Uses DocumentFile (SAF) and MediaMetadataRetriever to scan user-selected folders recursively, extracting metadata including genre and year, skipping already-imported songs; reports progress via callback (file count, current file name)
- **Erase**: Truncates all music tables on demand

**PreferencesRepository** manages user settings via Jetpack DataStore:
- Exposes each preference as a reactive `Flow`
- Currently stores: `autoPlayPlaylist` (Boolean)

**Room Database** (`MusicDatabase`, version 4) stores:
- `SongEntity` — song metadata including file path, folder path, genre, year, and rating; unique index on filePath
- `AlbumEntity` — album metadata
- `ArtistEntity` — artist metadata
- `PlaylistEntity` — playlist metadata
- `PlaylistSongCrossRef` — many-to-many relationship between playlists and songs
- `EncMusicListEntity` — Enchanted Music List metadata (unique name, description)
- `EncMusicListSongEntity` — junction table linking EncMusicLists to songs by file path (cascade delete)
- DAOs: `SongDao`, `AlbumDao`, `ArtistDao`, `PlaylistDao`, `EncMusicListDao` — all with Flow-based queries for reactive UI updates

### Service Layer

**PlaybackService** extends `MediaSessionService` to handle background audio playback. It is started by `MainActivity.onCreate()` to ensure background playback and notification controls are available immediately. The `ExoPlayer` instance is singleton-scoped via Hilt and shared between the service and ViewModels. The service does not release the ExoPlayer on destroy since it is a Hilt-managed singleton.

### Dependency Injection

Hilt provides all wiring:
- `AppModule` — ContentResolver, Room database, all DAOs (SongDao, AlbumDao, ArtistDao, PlaylistDao, EncMusicListDao)
- `MediaModule` — AudioAttributes, ExoPlayer (singleton)
- `PreferencesRepository` — auto-provided by Hilt as `@Singleton` with `@ApplicationContext` injection

The ExoPlayer singleton is shared between `PlaybackService` and screen ViewModels so they operate on the same player state.
