package com.enc.music.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [Index(value = ["filePath"], unique = true)]
)
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val artistId: Long,
    val duration: Long,
    val uri: String,
    val albumArtUri: String?,
    val filePath: String,
    val folderPath: String,
    val genre: String = "",
    val year: Int = 0,
    val rating: Int = 0
)
