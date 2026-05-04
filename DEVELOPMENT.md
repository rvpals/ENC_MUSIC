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
- **Room as primary data source** — all music metadata (songs, albums, artists) is stored in Room and read via reactive Flows; MediaStore is used only for the initial sync on app launch
- **Folder scanning via SAF** — user-initiated rescans use DocumentFile (Storage Access Framework) and MediaMetadataRetriever to import audio files from selected folders
- **Singleton ExoPlayer** — the player instance is shared between the service and ViewModels via Hilt
- **Type-safe routes** — navigation routes are `@Serializable` data classes/objects in `Routes.kt`
- **StateFlow for UI state** — each ViewModel exposes a single `StateFlow<*UiState>` data class

## APK Output

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk` (signed with debug keystore)
