package com.enc.music.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.enc.music.ui.components.MiniPlayer
import com.enc.music.ui.screens.album.AlbumScreen
import com.enc.music.ui.screens.artist.ArtistScreen
import com.enc.music.ui.screens.dbmanagement.DatabaseManagementScreen
import com.enc.music.ui.screens.library.LibraryScreen
import com.enc.music.ui.screens.magic.MagicSearchScreen
import com.enc.music.ui.screens.player.PlayerScreen

@Composable
fun MusicNavHost(
    navHostViewModel: NavHostViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showMiniPlayer = currentRoute != null &&
            !currentRoute.contains(PlayerRoute::class.qualifiedName.orEmpty())

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = LibraryRoute) {
                composable<LibraryRoute> {
                    LibraryScreen(
                        onSongClick = { },
                        onAlbumClick = { albumId -> navController.navigate(AlbumRoute(albumId)) },
                        onArtistClick = { artistId -> navController.navigate(ArtistRoute(artistId)) },
                        onNavigateTo = { route -> navController.navigate(route) }
                    )
                }
                composable<AlbumRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<AlbumRoute>()
                    AlbumScreen(
                        albumId = route.albumId,
                        onBack = { navController.popBackStack() },
                        onPlayAll = { }
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
                composable<DatabaseManagementRoute> {
                    DatabaseManagementScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<MagicSearchRoute> {
                    MagicSearchScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        if (showMiniPlayer) {
            MiniPlayer(
                player = navHostViewModel.player,
                onClick = { navController.navigate(PlayerRoute) },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }
}
