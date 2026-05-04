package com.enc.music.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object LibraryRoute

@Serializable
data class AlbumRoute(val albumId: Long)

@Serializable
data class ArtistRoute(val artistId: Long)

@Serializable
object PlayerRoute

@Serializable
object DatabaseManagementRoute
