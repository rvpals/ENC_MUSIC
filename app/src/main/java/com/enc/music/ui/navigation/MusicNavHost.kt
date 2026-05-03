package com.enc.music.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.enc.music.ui.screens.album.AlbumScreen
import com.enc.music.ui.screens.artist.ArtistScreen
import com.enc.music.ui.screens.library.LibraryScreen
import com.enc.music.ui.screens.player.PlayerScreen

@Composable
fun MusicNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = LibraryRoute) {
        composable<LibraryRoute> {
            LibraryScreen(
                onSongClick = { navController.navigate(PlayerRoute) },
                onAlbumClick = { albumId -> navController.navigate(AlbumRoute(albumId)) },
                onArtistClick = { artistId -> navController.navigate(ArtistRoute(artistId)) }
            )
        }
        composable<AlbumRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AlbumRoute>()
            AlbumScreen(
                albumId = route.albumId,
                onBack = { navController.popBackStack() },
                onPlayAll = { navController.navigate(PlayerRoute) }
            )
        }
        composable<ArtistRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ArtistRoute>()
            ArtistScreen(
                artistId = route.artistId,
                onBack = { navController.popBackStack() },
                onAlbumClick = { albumId -> navController.navigate(AlbumRoute(albumId)) }
            )
        }
        composable<PlayerRoute> {
            PlayerScreen(onBack = { navController.popBackStack() })
        }
    }
}
