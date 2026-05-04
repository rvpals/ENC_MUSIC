package com.enc.music.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.enc.music.data.local.MusicDatabase
import com.enc.music.data.local.dao.AlbumDao
import com.enc.music.data.local.dao.ArtistDao
import com.enc.music.data.local.dao.EncMusicListDao
import com.enc.music.data.local.dao.PlaylistDao
import com.enc.music.data.local.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            "enc_music.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePlaylistDao(database: MusicDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideSongDao(database: MusicDatabase): SongDao {
        return database.songDao()
    }

    @Provides
    fun provideAlbumDao(database: MusicDatabase): AlbumDao {
        return database.albumDao()
    }

    @Provides
    fun provideArtistDao(database: MusicDatabase): ArtistDao {
        return database.artistDao()
    }

    @Provides
    fun provideEncMusicListDao(database: MusicDatabase): EncMusicListDao {
        return database.encMusicListDao()
    }
}
