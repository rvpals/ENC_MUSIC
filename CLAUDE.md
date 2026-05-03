# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ENC Music is an Android music player app built with Kotlin, Jetpack Compose, and Media3 (ExoPlayer). It reads audio from the device via MediaStore and plays it with background playback support via MediaSessionService.

**Package:** `com.enc.music`
**Min SDK:** 26 (Android 8.0) | **Target/Compile SDK:** 35 (Android 15)

## Build Commands

Requires `JAVA_HOME` set to JDK 17 (on this machine: `E:/Prog/Java/jdk-17`).

```bash
./gradlew assembleDebug              # Build debug APK
./gradlew assembleRelease            # Build release APK (minified + shrunk)
./gradlew bundleRelease              # Build release AAB
./gradlew test                       # Run unit tests
./gradlew testDebugUnitTest          # Run debug unit tests only
./gradlew connectedAndroidTest       # Run instrumented tests (requires device/emulator)
./gradlew lint                       # Run Android lint
./gradlew clean                      # Clean build artifacts

# Run a single test class
./gradlew test --tests "com.enc.music.SomeTest"
```

## Technology Stack

- **UI:** Jetpack Compose with Material 3, Coil for images
- **Media:** Media3 (ExoPlayer + MediaSession) for audio playback
- **Database:** Room with KSP annotation processing
- **DI:** Hilt with KSP
- **Navigation:** Navigation Compose (single-activity architecture)
- **Async:** Kotlin Coroutines + Flows
- **Build:** Gradle Kotlin DSL with version catalog (`gradle/libs.versions.toml`)

## Architecture

The dependency setup targets a layered MVVM architecture:

```
Compose UI (screens/components)
    ↓ observes state
ViewModels (lifecycle-viewmodel-compose)
    ↓ calls
Repositories
    ↓ reads/writes
Room Database + Media3 Service
```

- **Single Activity** hosting Compose navigation graph
- **Hilt** provides all dependency wiring (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- **Room** handles local persistence; schemas export to `app/schemas/`
- **Media3 MediaSessionService** handles background audio playback
- **Java 17** source/target compatibility

## Key Packages

- `model/` — domain models (Song, Album, Artist) used across layers
- `data/local/` — Room database, entities (PlaylistEntity, PlaylistSongCrossRef), DAOs
- `data/repository/MusicRepository` — queries MediaStore for songs/albums/artists
- `service/PlaybackService` — Media3 MediaSessionService for background audio
- `di/` — Hilt modules (AppModule for DB/ContentResolver, MediaModule for ExoPlayer)
- `ui/screens/` — library (tabbed songs/albums/artists), player, album detail, artist detail
- `ui/navigation/` — type-safe routes via `@Serializable` data objects + Navigation Compose + NavHostViewModel
- `ui/components/` — shared composables (SongListItem, MiniPlayer)

## Build Configuration Notes

- Dependencies are managed via version catalog at `gradle/libs.versions.toml` — add new libraries there, not as hardcoded strings
- Release builds enable `isMinifyEnabled` and `isShrinkResources` with ProGuard rules in `app/proguard-rules.pro`
- `android.nonTransitiveRClass=true` is set — use fully qualified R references
- Room plugin configured with schema directory at `$projectDir/schemas`
- Navigation uses Kotlin Serialization for type-safe routes — route classes must be `@Serializable`
