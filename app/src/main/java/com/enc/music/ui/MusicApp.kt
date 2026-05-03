package com.enc.music.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.enc.music.ui.navigation.MusicNavHost
import com.enc.music.ui.theme.ENCMusicTheme

@Composable
fun MusicApp() {
    ENCMusicTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MusicNavHost()
        }
    }
}
