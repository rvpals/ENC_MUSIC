# Architecture

## Overview

ENC Music follows a layered MVVM architecture with unidirectional data flow.

```
┌─────────────────────────────────────┐
│           UI Layer (Compose)        │
│  Screens → observe → ViewModels    │
├─────────────────────────────────────┤
│           Domain Layer              │
│  Models (Song, Album, Artist)       │
├─────────────────────────────────────┤
│           Data Layer                │
│  MusicRepository ← MediaStore      │
│  PlaylistDao     ← Room Database   │
├─────────────────────────────────────┤
│         Service Layer               │
│  PlaybackService ← Media3 ExoPlayer│
└─────────────────────────────────────┘
```

## Layers

### UI Layer

Each screen has a dedicated ViewModel that exposes a `StateFlow<UiState>`. Composables collect this state and render accordingly. User actions call ViewModel methods which update state or trigger side effects.

- `LibraryScreen` / `LibraryViewModel` — tabbed song/album/artist browsing
- `PlayerScreen` / `PlayerViewModel` — now-playing with transport controls
- `AlbumScreen` / `AlbumViewModel` — album detail with song list
- `ArtistScreen` / `ArtistViewModel` — artist detail with album list

### Navigation

Type-safe navigation using `@Serializable` route objects with Navigation Compose. All routes are defined in `Routes.kt`:

- `LibraryRoute` — start destination
- `AlbumRoute(albumId)` — album detail
- `ArtistRoute(artistId)` — artist detail
- `PlayerRoute` — now-playing

`MusicNavHost` wraps the `NavHost` in a `Column` with a `MiniPlayer` at the bottom. The mini player is visible on all screens except `PlayerRoute` and provides quick access to playback controls without navigating away. A `NavHostViewModel` exposes the shared ExoPlayer instance to the mini player.

### Data Layer

**MusicRepository** queries the Android MediaStore ContentProvider for songs, albums, and artists. All queries run on `Dispatchers.IO`.

**Room Database** (`MusicDatabase`) stores user-created playlists:
- `PlaylistEntity` — playlist metadata
- `PlaylistSongCrossRef` — many-to-many relationship between playlists and songs (by song ID from MediaStore)
- `PlaylistDao` — Flow-based queries for reactive UI updates

### Service Layer

**PlaybackService** extends `MediaSessionService` to handle background audio playback. It is started by `MainActivity.onCreate()` to ensure background playback and notification controls are available immediately. The `ExoPlayer` instance is singleton-scoped via Hilt and shared between the service and ViewModels. The service does not release the ExoPlayer on destroy since it is a Hilt-managed singleton.

### Dependency Injection

Hilt provides all wiring:
- `AppModule` — ContentResolver, Room database, DAOs
- `MediaModule` — AudioAttributes, ExoPlayer (singleton)

The ExoPlayer singleton is shared between `PlaybackService` and screen ViewModels so they operate on the same player state.
