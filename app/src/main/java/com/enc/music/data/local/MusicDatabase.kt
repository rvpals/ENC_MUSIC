package com.enc.music.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enc.music.data.local.dao.AlbumDao
import com.enc.music.data.local.dao.ArtistDao
import com.enc.music.data.local.dao.PlaylistDao
import com.enc.music.data.local.dao.SongDao
import com.enc.music.data.local.entity.AlbumEntity
import com.enc.music.data.local.entity.ArtistEntity
import com.enc.music.data.local.entity.PlaylistEntity
import com.enc.music.data.local.entity.PlaylistSongCrossRef
import com.enc.music.data.local.entity.SongEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
}
