package com.enc.music.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.enc.music.data.local.dao.AlbumDao
import com.enc.music.data.local.dao.ArtistDao
import com.enc.music.data.local.dao.SongDao
import com.enc.music.data.local.entity.AlbumEntity
import com.enc.music.data.local.entity.ArtistEntity
import com.enc.music.data.local.entity.SongEntity
import com.enc.music.model.Album
import com.enc.music.model.Artist
import com.enc.music.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val songDao: SongDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao
) {

    suspend fun syncFromMediaStore() = withContext(Dispatchers.IO) {
        val songs = scanSongsFromMediaStore()
        val albums = scanAlbumsFromMediaStore()
        val artists = scanArtistsFromMediaStore()

        songDao.deleteAll()
        albumDao.deleteAll()
        artistDao.deleteAll()

        songDao.insertAll(songs.map { it.toEntity() })
        albumDao.insertAll(albums.map { it.toEntity() })
        artistDao.insertAll(artists.map { it.toEntity() })
    }

    fun getAllSongs(): Flow<List<Song>> =
        songDao.getAllSongs().map { entities -> entities.map { it.toSong() } }

    fun getAllAlbums(): Flow<List<Album>> =
        albumDao.getAllAlbums().map { entities -> entities.map { it.toAlbum() } }

    fun getAllArtists(): Flow<List<Artist>> =
        artistDao.getAllArtists().map { entities -> entities.map { it.toArtist() } }

    fun getSongsForAlbum(albumId: Long): Flow<List<Song>> =
        songDao.getSongsForAlbum(albumId).map { entities -> entities.map { it.toSong() } }

    fun getSongsForArtist(artistId: Long): Flow<List<Song>> =
        songDao.getSongsForArtist(artistId).map { entities -> entities.map { it.toSong() } }

    fun getAlbumsForArtist(artistId: Long): Flow<List<Album>> =
        albumDao.getAlbumsForArtist(artistId).map { entities -> entities.map { it.toAlbum() } }

    fun getSongsInFolder(folderPath: String): Flow<List<Song>> =
        songDao.getSongsInFolder(folderPath).map { entities -> entities.map { it.toSong() } }

    fun getDistinctFolders(): Flow<List<String>> =
        songDao.getDistinctFolders()

    suspend fun getSongCount(): Int = songDao.getCount()
    suspend fun getAlbumCount(): Int = albumDao.getCount()
    suspend fun getArtistCount(): Int = artistDao.getCount()
    suspend fun getFileCount(): Int = songDao.getFileCount()

    suspend fun eraseLibrary() = withContext(Dispatchers.IO) {
        songDao.deleteAll()
        albumDao.deleteAll()
        artistDao.deleteAll()
    }

    suspend fun scanFolder(folderUri: Uri, context: android.content.Context) = withContext(Dispatchers.IO) {
        val existingPaths = songDao.getAllFilePaths().toSet()
        val newSongs = mutableListOf<SongEntity>()
        val albumsMap = mutableMapOf<Long, AlbumEntity>()
        val artistsMap = mutableMapOf<Long, ArtistEntity>()
        var nextId = (songDao.getCount() + 1).toLong() * 1000000

        val rootDoc = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext
        scanDocTree(context, rootDoc, existingPaths, newSongs, albumsMap, artistsMap, { nextId++ })

        if (newSongs.isNotEmpty()) {
            songDao.insertAllSkipExisting(newSongs)
        }
        if (albumsMap.isNotEmpty()) {
            albumDao.insertAllSkipExisting(albumsMap.values.toList())
        }
        if (artistsMap.isNotEmpty()) {
            artistDao.insertAllSkipExisting(artistsMap.values.toList())
        }
    }

    private fun scanDocTree(
        context: android.content.Context,
        dir: DocumentFile,
        existingPaths: Set<String>,
        songs: MutableList<SongEntity>,
        albums: MutableMap<Long, AlbumEntity>,
        artists: MutableMap<Long, ArtistEntity>,
        nextId: () -> Long
    ) {
        val files = dir.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                scanDocTree(context, file, existingPaths, songs, albums, artists, nextId)
            } else if (file.isFile && isAudioFile(file.name ?: "")) {
                val fileUri = file.uri
                val filePath = fileUri.toString()
                if (existingPaths.contains(filePath)) continue

                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, fileUri)
                    val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        ?: file.name?.substringBeforeLast(".") ?: "Unknown"
                    val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown"
                    val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown"
                    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

                    val songId = nextId()
                    val albumId = album.hashCode().toLong() and 0x7FFFFFFFL
                    val artistId = artist.hashCode().toLong() and 0x7FFFFFFFL
                    val folderPath = filePath.substringBeforeLast("/", "")

                    songs.add(
                        SongEntity(
                            id = songId,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            artistId = artistId,
                            duration = duration,
                            uri = fileUri.toString(),
                            albumArtUri = null,
                            filePath = filePath,
                            folderPath = folderPath
                        )
                    )

                    if (!albums.containsKey(albumId)) {
                        albums[albumId] = AlbumEntity(
                            id = albumId,
                            title = album,
                            artist = artist,
                            artistId = artistId,
                            songCount = 1,
                            albumArtUri = null
                        )
                    }

                    if (!artists.containsKey(artistId)) {
                        artists[artistId] = ArtistEntity(
                            id = artistId,
                            name = artist,
                            albumCount = 1,
                            songCount = 1
                        )
                    }
                } catch (_: Exception) {
                } finally {
                    retriever.release()
                }
            }
        }
    }

    private fun isAudioFile(name: String): Boolean {
        val ext = name.substringAfterLast(".").lowercase()
        return ext in setOf("mp3", "m4a", "aac", "ogg", "opus", "flac", "wav", "wma", "alac", "aiff")
    }

    @Suppress("DEPRECATION")
    private fun scanSongsFromMediaStore(): List<Song> {
        val songs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                val filePath = cursor.getString(dataCol) ?: ""
                val folderPath = filePath.substringBeforeLast("/", "")
                songs.add(
                    Song(
                        id = id,
                        title = cursor.getString(titleCol),
                        artist = cursor.getString(artistCol),
                        album = cursor.getString(albumCol),
                        albumId = albumId,
                        artistId = cursor.getLong(artistIdCol),
                        duration = cursor.getLong(durationCol),
                        uri = ContentUris.withAppendedId(collection, id),
                        albumArtUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ),
                        filePath = filePath,
                        folderPath = folderPath
                    )
                )
            }
        }
        return songs
    }

    private fun scanAlbumsFromMediaStore(): List<Album> {
        val albums = mutableListOf<Album>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Albums.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.ARTIST_ID,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
        )

        contentResolver.query(collection, projection, null, null, "${MediaStore.Audio.Albums.ALBUM} ASC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST_ID)
            val countCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                albums.add(
                    Album(
                        id = id,
                        title = cursor.getString(albumCol),
                        artist = cursor.getString(artistCol),
                        artistId = cursor.getLong(artistIdCol),
                        songCount = cursor.getInt(countCol),
                        albumArtUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            id
                        )
                    )
                )
            }
        }
        return albums
    }

    private fun scanArtistsFromMediaStore(): List<Artist> {
        val artists = mutableListOf<Artist>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Artists.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
        )

        contentResolver.query(collection, projection, null, null, "${MediaStore.Audio.Artists.ARTIST} ASC")?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val albumCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val songCountCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

            while (cursor.moveToNext()) {
                artists.add(
                    Artist(
                        id = cursor.getLong(idCol),
                        name = cursor.getString(nameCol),
                        albumCount = cursor.getInt(albumCountCol),
                        songCount = cursor.getInt(songCountCol)
                    )
                )
            }
        }
        return artists
    }
}

private fun Song.toEntity() = SongEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    artistId = artistId,
    duration = duration,
    uri = uri.toString(),
    albumArtUri = albumArtUri?.toString(),
    filePath = filePath,
    folderPath = folderPath
)

private fun Album.toEntity() = AlbumEntity(
    id = id,
    title = title,
    artist = artist,
    artistId = artistId,
    songCount = songCount,
    albumArtUri = albumArtUri?.toString()
)

private fun Artist.toEntity() = ArtistEntity(
    id = id,
    name = name,
    albumCount = albumCount,
    songCount = songCount
)

fun SongEntity.toSong() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    artistId = artistId,
    duration = duration,
    uri = Uri.parse(uri),
    albumArtUri = albumArtUri?.let { Uri.parse(it) },
    filePath = filePath,
    folderPath = folderPath
)

fun AlbumEntity.toAlbum() = Album(
    id = id,
    title = title,
    artist = artist,
    artistId = artistId,
    songCount = songCount,
    albumArtUri = albumArtUri?.let { Uri.parse(it) }
)

fun ArtistEntity.toArtist() = Artist(
    id = id,
    name = name,
    albumCount = albumCount,
    songCount = songCount
)
