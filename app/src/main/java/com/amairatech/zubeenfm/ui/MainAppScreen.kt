package com.amairatech.zubeenfm.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amairatech.zubeenfm.ui.components.GlassBottomNavBar
import com.amairatech.zubeenfm.ui.components.GlassNavItem
import com.amairatech.zubeenfm.ui.theme.ObsidianBackground
import com.amairatech.zubeenfm.ui.tribute.TributeScreen

enum class AppNavTab(val labelEnglish: String, val icon: String) {
    HOME("Home", "🏠"),
    RADIO("Radio", "📻"),
    SONGS("Songs", "🎶"),
    TRIBUTE("Tribute", "🪔"),
    ABOUT("About", "ℹ️")
}

/**
 * Authoritative global container for ZUBEEN FM.
 * Glassmorphism redesign with floating glass bottom navigation.
 * - Floating Glass Mini Player
 * - Clean 4-tab glass bottom navigation
 * - Normal Mode full player modal bottom sheet
 */
@Composable
fun MainAppScreen(
    radioViewModel: RadioViewModel = viewModel()
) {
    var selectedTab by rememberSaveable { mutableStateOf(AppNavTab.RADIO) }
    val radioUiState by radioViewModel.uiState.collectAsState()

    val navItems = AppNavTab.values().map { GlassNavItem(it.labelEnglish, it.icon) }
    val selectedNavIndex = AppNavTab.values().indexOf(selectedTab)

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent MiniPlayer when outside the main Radio screen
                AnimatedVisibility(
                    visible = selectedTab != AppNavTab.RADIO,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    MiniPlayer(
                        uiState = radioUiState,
                        onTogglePlayPause = { radioViewModel.togglePlayPause() },
                        onClickExpand = {
                            if (radioUiState.isRadioPlaying || radioUiState.activePlaybackMode == PlaybackMode.RADIO) {
                                selectedTab = AppNavTab.RADIO
                            } else {
                                radioViewModel.setNormalModeFullPlayerVisible(true)
                            }
                        }
                    )
                }

                // Floating Glass Bottom Navigation
                GlassBottomNavBar(
                    items = navItems,
                    selectedIndex = selectedNavIndex,
                    onItemSelected = { index ->
                        selectedTab = AppNavTab.values()[index]
                    }
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0520),
                            Color(0xFF060512),
                            ObsidianBackground
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppNavTab.HOME -> HomeScreen(
                    onNavigateToRadio = { selectedTab = AppNavTab.RADIO },
                    onNavigateToSongs = { selectedTab = AppNavTab.SONGS },
                    onNavigateToTribute = { selectedTab = AppNavTab.TRIBUTE },
                    onPlaySong = { song ->
                        radioViewModel.selectAndPlayNormalSong(song)
                    }
                )

                AppNavTab.RADIO -> RadioScreen(viewModel = radioViewModel)

                AppNavTab.SONGS -> SongsScreen(
                    currentPlayingSongId = radioUiState.currentSong.id,
                    isPlaying = radioUiState.isPlaying,
                    favoriteSongIds = radioUiState.favoriteSongIds,
                    onSelectSong = { song ->
                        radioViewModel.selectAndPlayNormalSong(song)
                    },
                    onOpenFullPlayer = { song ->
                        radioViewModel.setNormalModeFullPlayerVisible(true)
                    },
                    onPlayAlbum = { album, shuffle ->
                        radioViewModel.playAlbum(album, shuffle)
                    },
                    onPlayArtist = { artist ->
                        radioViewModel.playArtist(artist)
                    }
                )

                AppNavTab.TRIBUTE -> TributeScreen()

                AppNavTab.ABOUT -> AboutCreditsScreen()
            }

            // Normal Mode Full Player Bottom Sheet
            if (radioUiState.isNormalModeFullPlayerVisible) {
                NormalPlayerBottomSheet(
                    song = radioUiState.normalCurrentSong,
                    isPlaying = radioUiState.isNormalPlaying,
                    elapsedSeconds = radioUiState.normalElapsedSeconds,
                    songProgress = radioUiState.normalSongProgress,
                    isFavorite = radioUiState.normalCurrentSong.id in radioUiState.favoriteSongIds,
                    isShuffleEnabled = radioUiState.isShuffleEnabled,
                    repeatMode = radioUiState.repeatMode,
                    isRepeatEnabled = radioUiState.isRepeatEnabled,
                    onTogglePlayPause = { radioViewModel.togglePlayPauseNormal() },
                    onPrevious = { radioViewModel.playPreviousSong() },
                    onNext = { radioViewModel.playNextSong() },
                    onSeek = { radioViewModel.seekTo(it) },
                    onToggleShuffle = { radioViewModel.toggleShuffle() },
                    onToggleRepeat = { radioViewModel.toggleRepeat() },
                    onToggleFavorite = { radioViewModel.toggleFavorite(radioUiState.normalCurrentSong.id) },
                    onDismiss = { radioViewModel.setNormalModeFullPlayerVisible(false) }
                )
            }
        }
    }
}
