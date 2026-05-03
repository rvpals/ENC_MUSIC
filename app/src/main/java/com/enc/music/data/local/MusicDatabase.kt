package com.enc.music.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enc.music.data.local.dao.PlaylistDao
import com.enc.music.data.local.entity.PlaylistEntity
import com.enc.music.data.local.entity.PlaylistSongCrossRef

@Database(
    entities = [PlaylistEntity::class, PlaylistSongCrossRef::class],
    version = 1,
    exportSchema = true
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}
