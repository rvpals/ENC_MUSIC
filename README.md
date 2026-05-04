# ENC Music

A modern Android music player built with Kotlin, Jetpack Compose, and Media3.

## Features

- Browse your music library by songs, albums, artists, and folders
- Full local SQLite database storing all song, album, and artist metadata with file paths, genres, years, and ratings
- Folder browser with recursive navigation into subdirectories
- **Enchanted Music Magic** — advanced search screen with multi-field filtering (genre, artist, album, year, duration), target total duration auto-picker, and "Add to Magic List" for curated playlists
- **EncMusicList** (Enchanted Music Lists) — named playlists with descriptions, tracking songs by file path
- Status bar showing item counts on every tab (songs, albums, artists, files)
- Database management screen with library statistics, folder-based rescan, and erase
- Mini player bar with playback controls visible across all screens
- Full now-playing screen with play/pause, skip, seek, shuffle, and repeat
- Background audio playback with media notification controls
- Album and artist detail views
- Playlist support with Room database persistence
- App menu with Enchanted Music Magic, Database Management, Preferences, Help, and About
- Runtime permission handling for audio access and notifications
- Material 3 dynamic color theming
- Android 12+ splash screen with backward compatibility

## Screenshots

*Coming soon*

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose, Material 3, Coil |
| Media | Media3 (ExoPlayer + MediaSession) |
| Database | Room (SQLite) — songs, albums, artists, playlists, EncMusicLists |
| File Access | DocumentFile (SAF) for folder scanning |
| DI | Hilt |
| Navigation | Navigation Compose (type-safe routes) |
| Async | Kotlin Coroutines + Flows |
| Build | Gradle Kotlin DSL, Version Catalog |

## Requirements

- Android 8.0 (API 26) or higher
- JDK 17 for building

## Building

```bash
./gradlew assembleDebug       # Debug APK
./gradlew assembleRelease     # Release APK (minified)
```

The release APK is signed with the debug keystore and output to `app/build/outputs/apk/release/`.

## Project Structure

```
app/src/main/java/com/enc/music/
├── model/              Domain models (Song, Album, Artist, FolderItem)
├── data/
│   ├── local/          Room database, entities, DAOs (songs, albums, artists, playlists, enc_music_lists)
│   └── repository/     MediaStore sync + Room queries + folder scanning
├── service/            Media3 playback service
├── di/                 Hilt dependency injection modules
└── ui/
    ├── theme/          Material 3 theming
    ├── navigation/     Type-safe route definitions and NavHost
    ├── components/     Shared composables (SongListItem, MiniPlayer)
    └── screens/        Library, Player, Album, Artist, Database Management, Magic Search screens
```

## License

All rights reserved.
