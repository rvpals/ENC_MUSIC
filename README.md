# ENC Music

A modern Android music player built with Kotlin, Jetpack Compose, and Media3.

## Features

- Browse your music library by songs, albums, and artists
- Mini player bar with playback controls visible across all screens
- Full now-playing screen with play/pause, skip, seek, shuffle, and repeat
- Background audio playback with media notification controls
- Album and artist detail views
- Playlist support with Room database persistence
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
| Database | Room |
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
├── model/              Domain models (Song, Album, Artist)
├── data/
│   ├── local/          Room database, entities, DAOs
│   └── repository/     MediaStore queries
├── service/            Media3 playback service
├── di/                 Hilt dependency injection modules
└── ui/
    ├── theme/          Material 3 theming
    ├── navigation/     Type-safe route definitions and NavHost
    ├── components/     Shared composables (SongListItem, MiniPlayer)
    └── screens/        Library, Player, Album, Artist screens
```

## License

All rights reserved.
