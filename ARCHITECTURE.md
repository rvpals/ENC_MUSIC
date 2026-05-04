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
│          FolderItem)               │
├─────────────────────────────────────┤
│           Data Layer                │
│  MusicRepository                   │
│    ← MediaStore (scan/sync)        │
│    ← Room Database (read/write)    │
│    ← DocumentFile (folder scan)    │
│  PlaylistDao ← Room Database       │
├─────────────────────────────────────┤
│         Service Layer               │
│  PlaybackService ← Media3 ExoPlayer│
└─────────────────────────────────────┘
```

## Layers

### UI Layer

Each screen has a dedicated ViewModel that exposes a `StateFlow<UiState>`. Composables collect this state and render accordingly. User actions call ViewModel methods which update state or trigger side effects.

- `LibraryScreen` / `LibraryViewModel` — tabbed song/album/artist/folder browsing with status bar, overflow menu
- `PlayerScreen` / `PlayerViewModel` — now-playing with transport controls
- `AlbumScreen` / `AlbumViewModel` — album detail with song list
- `ArtistScreen` / `ArtistViewModel` — artist detail with album list
- `DatabaseManagementScreen` / `DatabaseManagementViewModel` — library stats, rescan, erase

### Navigation

Type-safe navigation using `@Serializable` route objects with Navigation Compose. All routes are defined in `Routes.kt`:

- `LibraryRoute` — start destination
- `AlbumRoute(albumId)` — album detail
- `ArtistRoute(artistId)` — artist detail
- `PlayerRoute` — now-playing
- `DatabaseManagementRoute` — database management

`MusicNavHost` wraps the `NavHost` in a `Column` with a `MiniPlayer` at the bottom. The mini player is visible on all screens except `PlayerRoute` and provides quick access to playback controls without navigating away. A `NavHostViewModel` exposes the shared ExoPlayer instance to the mini player.

### Data Layer

**MusicRepository** is the central data coordinator:
- **Sync**: Scans MediaStore on app launch and writes all song/album/artist metadata to Room
- **Read**: All UI queries read from Room via reactive `Flow`s (not live MediaStore queries)
- **Folder scan**: Uses DocumentFile (SAF) and MediaMetadataRetriever to scan user-selected folders recursively, skipping already-imported songs
- **Erase**: Truncates all music tables on demand

**Room Database** (`MusicDatabase`, version 2) stores:
- `SongEntity` — song metadata including file path and folder path
- `AlbumEntity` — album metadata
- `ArtistEntity` — artist metadata
- `PlaylistEntity` — playlist metadata
- `PlaylistSongCrossRef` — many-to-many relationship between playlists and songs
- DAOs: `SongDao`, `AlbumDao`, `ArtistDao`, `PlaylistDao` — all with Flow-based queries for reactive UI updates

### Service Layer

**PlaybackService** extends `MediaSessionService` to handle background audio playback. It is started by `MainActivity.onCreate()` to ensure background playback and notification controls are available immediately. The `ExoPlayer` instance is singleton-scoped via Hilt and shared between the service and ViewModels. The service does not release the ExoPlayer on destroy since it is a Hilt-managed singleton.

### Dependency Injection

Hilt provides all wiring:
- `AppModule` — ContentResolver, Room database, all DAOs (SongDao, AlbumDao, ArtistDao, PlaylistDao)
- `MediaModule` — AudioAttributes, ExoPlayer (singleton)

The ExoPlayer singleton is shared between `PlaybackService` and screen ViewModels so they operate on the same player state.
