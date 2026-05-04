package com.enc.music.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enc.music.data.local.entity.EncMusicListEntity
import com.enc.music.data.local.entity.EncMusicListSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EncMusicListDao {

    @Query("SELECT * FROM enc_music_lists ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<EncMusicListEntity>>

    @Query("SELECT * FROM enc_music_lists WHERE id = :id")
    suspend fun getListById(id: Long): EncMusicListEntity?

    @Query("SELECT * FROM enc_music_lists WHERE name = :name")
    suspend fun getListByName(name: String): EncMusicListEntity?

    @Insert
    suspend fun createList(list: EncMusicListEntity): Long

    @Update
    suspend fun updateList(list: EncMusicListEntity)

    @Delete
    suspend fun deleteList(list: EncMusicListEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSong(entry: EncMusicListSongEntity)

    @Query("DELETE FROM enc_music_list_songs WHERE listId = :listId AND filePath = :filePath")
    suspend fun removeSong(listId: Long, filePath: String)

    @Query("SELECT filePath FROM enc_music_list_songs WHERE listId = :listId ORDER BY sortOrder")
    fun getSongPathsForList(listId: Long): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM enc_music_list_songs WHERE listId = :listId")
    suspend fun getSongCount(listId: Long): Int
}
