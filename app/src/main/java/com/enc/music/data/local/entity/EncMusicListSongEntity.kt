package com.enc.music.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "enc_music_list_songs",
    primaryKeys = ["listId", "filePath"],
    foreignKeys = [
        ForeignKey(
            entity = EncMusicListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listId"])]
)
data class EncMusicListSongEntity(
    val listId: Long,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
