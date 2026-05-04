# Features

## Music Library

- **Songs tab** — scrollable list of all songs in the database, showing album art, title, artist, album, and duration
- **Albums tab** — grid of albums with cover art, artist name, and song count
- **Artists tab** — list of artists with album and song counts
- **Folders tab** — browse music by folder hierarchy with recursive navigation into subdirectories and back to parent
- **Status bar** — shown below the tab row on every tab, displaying item counts (e.g., "245 songs", "32 albums", "18 artists", "3 folders · 12 files")
- All music metadata stored locally in SQLite via Room (songs, albums, artists with file paths)

## Database Management

- Accessible via the app menu (3-dot overflow menu in the top bar)
- **Library statistics** — displays total number of songs, albums, artists, and files in a card grid
- **Rescan Library** — opens a folder picker (SAF), recursively scans the selected folder for audio files, extracts metadata (title, artist, album, duration) using MediaMetadataRetriever, and stores in the database; skips songs already in the database
- **Erase Library** — prompts with a confirmation dialog, then truncates all songs, albums, and artists tables
- Supported audio formats: MP3, M4A, AAC, OGG, Opus, FLAC, WAV, WMA, ALAC, AIFF

## App Menu

- **Database Management** — navigate to database management screen
- **Preferences** — placeholder for future settings
- **Help** — placeholder for future help content
- **About** — placeholder for future about screen

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

## Folder Browsing

- Browse music files organized by their directory structure
- Navigate into subfolders by tapping, navigate back via back button or system back gesture
- Shows subfolder count and total song count per folder
- Songs in the current folder are listed below subfolders
- Top bar updates to show the current folder name when navigating into subfolders

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
