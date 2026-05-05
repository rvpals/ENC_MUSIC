# Development Guide

## Prerequisites

- JDK 17
- Android SDK with API 35 installed
- Android device or emulator running API 26+

## Environment Setup

Set `JAVA_HOME` to your JDK 17 installation before running Gradle:

```bash
export JAVA_HOME="E:/Prog/Java/jdk-17"  # adjust for your system
```

## Build Commands

```bash
./gradlew assembleDebug              # Debug APK
./gradlew assembleRelease            # Release APK (minified + shrunk)
./gradlew bundleRelease              # Release AAB
./gradlew test                       # Unit tests
./gradlew testDebugUnitTest          # Debug unit tests only
./gradlew connectedAndroidTest       # Instrumented tests (device required)
./gradlew lint                       # Android lint
./gradlew clean                      # Clean build artifacts

# Single test class
./gradlew test --tests "com.enc.music.SomeTest"
```

## Dependency Management

All dependencies are declared in `gradle/libs.versions.toml` using the Gradle version catalog. To add a new library:

1. Add the version under `[versions]`
2. Add the library declaration under `[libraries]`
3. Reference it in `app/build.gradle.kts` as `libs.your.library`

## Key Conventions

- **Single Activity** — `MainActivity` is the only activity; all screens are Compose destinations
- **Hilt everywhere** — annotate ViewModels with `@HiltViewModel`, activities/services with `@AndroidEntryPoint`
- **Room as primary data source** — all music metadata (songs with genre/year/rating, albums, artists, playlists) is stored in Room and read via reactive Flows; MediaStore sync runs in background on launch while the UI shows cached data immediately
- **Folder scanning via SAF** — user-initiated rescans use DocumentFile (Storage Access Framework) and MediaMetadataRetriever to import audio files from selected folders
- **Singleton ExoPlayer** — the player instance is shared between the service and ViewModels via Hilt
- **Type-safe routes** — navigation routes are `@Serializable` data classes/objects in `Routes.kt`
- **StateFlow for UI state** — each ViewModel exposes a single `StateFlow<*UiState>` data class
- **EncMusicList for curated lists** — Enchanted Music Lists use file paths (not song IDs) to reference songs, making them resilient to database rebuilds
- **DataStore for preferences** — user settings stored in Jetpack DataStore (`enc_music_prefs`), exposed as reactive Flows, injected via `PreferencesRepository`
- **Database versioning** — uses `fallbackToDestructiveMigration()` since all music data can be re-synced from MediaStore; EncMusicList data is user-created and will be lost on destructive migration (future: add proper migrations)

## Database Schema (Version 4)

| Table | Purpose |
|-------|---------|
| `songs` | Song metadata with unique filePath index |
| `albums` | Album metadata |
| `artists` | Artist metadata |
| `playlists` | User playlists |
| `playlist_songs` | Playlist-song junction (by song ID) |
| `enc_music_lists` | Enchanted Music Lists (unique name, description) |
| `enc_music_list_songs` | List-song junction (by file path, cascade delete) |

## APK Output

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk` (signed with debug keystore)
