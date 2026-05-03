# Changelog

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
