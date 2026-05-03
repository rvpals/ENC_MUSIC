package com.enc.music.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long,
    val songCount: Int,
    val albumArtUri: Uri?
)
