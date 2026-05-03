# Features

## Music Library

- **Songs tab** — scrollable list of all songs on the device, showing album art, title, artist, album, and duration
- **Albums tab** — grid of albums with cover art, artist name, and song count
- **Artists tab** — list of artists with album and song counts

## Mini Player

- Persistent bar at the bottom of library, album, and artist screens
- Shows album art, song title, and artist
- Play/pause and skip-next controls
- Progress indicator bar
- Tap to expand into full now-playing screen
- Animated slide-in/out when playback starts/stops

## Playback

- Full background audio playback via Media3 ExoPlayer
- PlaybackService started on app launch for reliable background playback
- MediaSession integration for system media controls and notification
- Audio focus handling — pauses when other apps take focus
- Becoming-noisy handling — pauses when headphones are unplugged
- Play/pause, skip next/previous, seek via slider
- Shuffle mode toggle
- Repeat modes: off, repeat all, repeat one

## Now Playing

- Large album art display
- Song title and artist
- Seek slider with current position and total duration
- Full transport controls

## Album Detail

- Song list for the selected album
- Play all songs in the album
- Play individual songs

## Artist Detail

- Album list for the selected artist
- Navigate to album detail

## Playlists (Data Layer)

- Create and delete playlists
- Add and remove songs from playlists
- Persistent storage via Room database

## Permissions

- Runtime audio permission request (READ_MEDIA_AUDIO on Android 13+, READ_EXTERNAL_STORAGE on older)
- Notification permission request (POST_NOTIFICATIONS on Android 13+) for media controls
- Graceful handling when permission is already granted (no redundant dialogs)

## Visual Design

- Material 3 with Material You dynamic colors (Android 12+)
- Dark and light theme support (follows system)
- Splash screen with app icon on dark background
- Edge-to-edge display
- Custom adaptive launcher icon
