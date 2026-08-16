package com.amairatech.zubeenfm.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amairatech.zubeenfm.data.model.Album
import com.amairatech.zubeenfm.data.model.Artist
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import com.amairatech.zubeenfm.ui.theme.DeepAmber
import com.amairatech.zubeenfm.ui.theme.ObsidianBackground
import com.amairatech.zubeenfm.ui.theme.ObsidianBorder
import com.amairatech.zubeenfm.ui.theme.ObsidianCard
import com.amairatech.zubeenfm.ui.theme.ObsidianCardElevated
import com.amairatech.zubeenfm.ui.theme.RoyalGold
import com.amairatech.zubeenfm.ui.theme.SoftGold
import com.amairatech.zubeenfm.ui.theme.SuccessGreen
import com.amairatech.zubeenfm.ui.theme.TextMuted
import com.amairatech.zubeenfm.ui.theme.TextPrimary
import com.amairatech.zubeenfm.ui.theme.TextSecondary
import com.amairatech.zubeenfm.ui.theme.TextPureWhite
import com.amairatech.zubeenfm.ui.theme.GlassWhite
import com.amairatech.zubeenfm.ui.theme.GlassWhiteBorder
import com.amairatech.zubeenfm.ui.theme.GlassPurple
import com.amairatech.zubeenfm.ui.theme.GlassPurpleBorder
import com.amairatech.zubeenfm.ui.theme.GradientPurpleStart
import com.amairatech.zubeenfm.ui.theme.GradientPurpleEnd
import com.amairatech.zubeenfm.ui.theme.GradientIndigoEnd
import com.amairatech.zubeenfm.ui.components.AsyncArtwork
import com.amairatech.zubeenfm.ui.components.GlassCard

enum class LibraryTab(val title: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    GENRES("Genres"),
    LANGUAGES("Languages")
}

/**
 * Responsive Assamese Music Library screen for Normal Mode.
 * Adaptive sub-navigation tabs, search, cards with weight(1f), and adaptive grids.
 */
@Composable
fun SongsScreen(
    currentPlayingSongId: String,
    isPlaying: Boolean,
    favoriteSongIds: Set<String>,
    onSelectSong: (Song) -> Unit,
    onOpenFullPlayer: (Song) -> Unit,
    onPlayAlbum: (Album, Boolean) -> Unit = { _, _ -> },
    onPlayArtist: (Artist) -> Unit = {}
) {
    var selectedLibraryTab by rememberSaveable { mutableStateOf(LibraryTab.SONGS) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var debouncedSearchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(300) // Debounce 300ms
        debouncedSearchQuery = searchQuery
    }

    var selectedGenre by rememberSaveable { mutableStateOf("All Genres") }
    var selectedLanguage by rememberSaveable { mutableStateOf("All Languages") }
    var showOnlyFavorites by rememberSaveable { mutableStateOf(false) }
    var isAzSort by rememberSaveable { mutableStateOf(false) }

    var selectedAlbumForDetails by remember { mutableStateOf<Album?>(null) }
    var selectedArtistForDetails by remember { mutableStateOf<Artist?>(null) }

    val allSongs by NormalCatalogueRepository.songsFlow.collectAsState()
    val allAlbums by NormalCatalogueRepository.albumsFlow.collectAsState()
    val allArtists by NormalCatalogueRepository.artistsFlow.collectAsState()
    val isLoading by NormalCatalogueRepository.providerManager.isNormalLoading.collectAsState()
    val loadingStatus by NormalCatalogueRepository.providerManager.normalLoadingStatus.collectAsState()

    val filteredSongs = remember(debouncedSearchQuery, selectedGenre, selectedLanguage, showOnlyFavorites, favoriteSongIds, allSongs, isAzSort) {
        if (showOnlyFavorites) {
            val favs = allSongs.filter { it.id in favoriteSongIds }
            if (isAzSort && debouncedSearchQuery.isEmpty()) favs.sortedBy { it.titleEnglish.lowercase() } else favs
        } else {
            NormalCatalogueRepository.searchSongs(
                query = debouncedSearchQuery,
                selectedGenre = selectedGenre,
                selectedLanguage = selectedLanguage,
                isAzSort = isAzSort
            )
        }
    }

    val filteredAlbums = remember(debouncedSearchQuery, allAlbums) {
        val q = debouncedSearchQuery.trim().lowercase()
        if (q.isEmpty()) allAlbums else allAlbums.filter {
            it.nameEnglish.lowercase().contains(q) ||
            it.nameAssamese.lowercase().contains(q) ||
            it.artistEnglish.lowercase().contains(q) ||
            it.artistAssamese.lowercase().contains(q)
        }
    }

    val filteredArtists = remember(debouncedSearchQuery, allArtists) {
        val q = debouncedSearchQuery.trim().lowercase()
        if (q.isEmpty()) allArtists else allArtists.filter {
            it.nameEnglish.lowercase().contains(q) ||
            it.nameAssamese.lowercase().contains(q)
        }
    }

    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        GradientPurpleEnd,
                        ObsidianBackground,
                        GradientIndigoEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assamese Library",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalGold
                )

                Text(
                    text = if (isLoading) "Loading Library..." else "${allSongs.size} Songs",
                    fontSize = 12.sp,
                    color = SoftGold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = RoyalGold,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                    Text(text = loadingStatus, fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Navigation Tabs: Horizontal Scrollable Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LibraryTab.values()) { tab ->
                    val isSelected = tab == selectedLibraryTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) GlassPurple else GlassWhite)
                            .border(1.dp, if (isSelected) GlassPurpleBorder else GlassWhiteBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedLibraryTab = tab }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) TextPureWhite else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Global Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search songs, albums, or artists...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassPurpleBorder,
                    unfocusedBorderColor = GlassWhiteBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = RoyalGold,
                    focusedContainerColor = GlassWhite,
                    unfocusedContainerColor = GlassWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content Area based on Tab
            when (selectedLibraryTab) {
                LibraryTab.SONGS -> {
                    // Quick Filter Chips: Favorites, A-Z, Genres
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            val isSelected = showOnlyFavorites
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) GlassPurple else GlassWhite)
                                    .border(1.dp, if (isSelected) GlassPurpleBorder else GlassWhiteBorder, RoundedCornerShape(12.dp))
                                    .clickable { showOnlyFavorites = !showOnlyFavorites }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "❤️ Favorites",
                                    fontSize = 11.sp,
                                    color = if (isSelected) TextPureWhite else TextMuted
                                )
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isAzSort) GlassPurple else GlassWhite)
                                    .border(1.dp, if (isAzSort) GlassPurpleBorder else GlassWhiteBorder, RoundedCornerShape(12.dp))
                                    .clickable { isAzSort = !isAzSort }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Sort A–Z",
                                    fontSize = 11.sp,
                                    color = if (isAzSort) TextPureWhite else TextMuted
                                )
                            }
                        }

                        items(NormalCatalogueRepository.genres.filter { it != "All Genres" }) { genre ->
                            val isSelected = genre == selectedGenre && !showOnlyFavorites
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) GlassPurple else GlassWhite)
                                    .border(1.dp, if (isSelected) GlassPurpleBorder else GlassWhiteBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        showOnlyFavorites = false
                                        selectedGenre = if (selectedGenre == genre) "All Genres" else genre
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = genre,
                                    fontSize = 11.sp,
                                    color = if (isSelected) TextPureWhite else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredSongs.isEmpty() && !isLoading) {
                        EmptyCatalogState("No songs found", onReset = {
                            searchQuery = ""
                            selectedGenre = "All Genres"
                            selectedLanguage = "All Languages"
                            showOnlyFavorites = false
                            isAzSort = false
                        })
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isAzSort) {
                                val grouped = filteredSongs.groupBy { song ->
                                    val firstChar = song.titleEnglish.trim().firstOrNull()?.uppercaseChar() ?: '?'
                                    if (firstChar in 'A'..'Z') {
                                        firstChar.toString()
                                    } else if (song.titleAssamese.any { it in '\u0980'..'\u09FF' }) {
                                        "Assamese"
                                    } else {
                                        "#"
                                    }
                                }
                                
                                grouped.forEach { (initial, songs) ->
                                    item(key = "header_$initial") {
                                        Text(
                                            text = initial,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                            color = RoyalGold,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        )
                                    }
                                    items(songs, key = { it.id }) { song ->
                                        val isCurrent = song.id == currentPlayingSongId
                                        val isFav = song.id in favoriteSongIds
                                        ResponsiveSongRow(
                                            song = song,
                                            isCurrentlyPlaying = isCurrent && isPlaying,
                                            isFavorite = isFav,
                                            onClick = {
                                                onSelectSong(song)
                                                onOpenFullPlayer(song)
                                            }
                                        )
                                    }
                                }
                            } else {
                                items(filteredSongs, key = { it.id }) { song ->
                                    val isCurrent = song.id == currentPlayingSongId
                                    val isFav = song.id in favoriteSongIds
                                    ResponsiveSongRow(
                                        song = song,
                                        isCurrentlyPlaying = isCurrent && isPlaying,
                                        isFavorite = isFav,
                                        onClick = {
                                            onSelectSong(song)
                                            onOpenFullPlayer(song)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                LibraryTab.ALBUMS -> {
                    if (filteredAlbums.isEmpty() && !isLoading) {
                        EmptyCatalogState("No albums found", onReset = { searchQuery = "" })
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredAlbums, key = { it.id }) { album ->
                                ResponsiveAlbumCard(
                                    album = album,
                                    onClick = { selectedAlbumForDetails = album }
                                )
                            }
                        }
                    }
                }

                LibraryTab.ARTISTS -> {
                    if (filteredArtists.isEmpty() && !isLoading) {
                        EmptyCatalogState("No artists found", onReset = { searchQuery = "" })
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredArtists, key = { it.id }) { artist ->
                                ResponsiveArtistCard(
                                    artist = artist,
                                    onClick = { selectedArtistForDetails = artist }
                                )
                            }
                        }
                    }
                }

                LibraryTab.GENRES -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(NormalCatalogueRepository.genres.filter { it != "All Genres" }) { genre ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedGenre = genre
                                        selectedLibraryTab = LibraryTab.SONGS
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = GlassWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = genre,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(text = "›", fontSize = 18.sp, color = RoyalGold)
                                }
                            }
                        }
                    }
                }

                LibraryTab.LANGUAGES -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(NormalCatalogueRepository.languages.filter { it != "All Languages" }) { lang ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage = lang
                                        selectedLibraryTab = LibraryTab.SONGS
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = GlassWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lang,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(text = "›", fontSize = 18.sp, color = RoyalGold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Album Details Bottom Sheet
        selectedAlbumForDetails?.let { album ->
            AlbumDetailBottomSheet(
                album = album,
                currentPlayingSongId = currentPlayingSongId,
                isPlaying = isPlaying,
                onPlaySong = { song ->
                    onSelectSong(song)
                    onOpenFullPlayer(song)
                },
                onPlayAll = { alb -> onPlayAlbum(alb, false) },
                onShuffleAll = { alb -> onPlayAlbum(alb, true) },
                onDismiss = { selectedAlbumForDetails = null }
            )
        }

        // Artist Details Bottom Sheet
        selectedArtistForDetails?.let { artist ->
            ArtistDetailBottomSheet(
                artist = artist,
                currentPlayingSongId = currentPlayingSongId,
                isPlaying = isPlaying,
                onPlaySong = { song ->
                    onSelectSong(song)
                    onOpenFullPlayer(song)
                },
                onPlayAll = { art -> onPlayArtist(art) },
                onSelectAlbum = { alb ->
                    selectedArtistForDetails = null
                    selectedAlbumForDetails = alb
                },
                onDismiss = { selectedArtistForDetails = null }
            )
        }
    }
}

@Composable
private fun ResponsiveSongRow(
    song: Song,
    isCurrentlyPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = 16.dp,
        backgroundColor = if (isCurrentlyPlaying) GlassPurple.copy(alpha = 0.8f) else GlassWhite,
        borderColor = if (isCurrentlyPlaying) RoyalGold.copy(alpha = 0.5f) else GlassWhiteBorder.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Square Artwork
                AsyncArtwork(
                    url = song.artworkUrl,
                    modifier = Modifier.size(52.dp),
                    cornerRadius = 10.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title + Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.titleAssamese,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentlyPlaying) RoyalGold else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${song.artistAssamese} • ${song.albumAssamese}",
                        fontSize = 11.sp,
                        color = TextSecondary, // using Secondary instead of Muted for readability
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Duration / Favorite
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isFavorite) {
                    Text(text = "❤️", fontSize = 11.sp)
                }
                Text(
                    text = String.format("%02d:%02d", song.durationSeconds / 60, song.durationSeconds % 60),
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun ResponsiveAlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AsyncArtwork(
                url = album.artworkUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                cornerRadius = 16.dp,
                placeholderIcon = "💿"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = album.nameAssamese,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = album.artistAssamese,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${album.songs.size} tracks",
                fontSize = 10.sp,
                color = SoftGold
            )
        }
    }
}

@Composable
private fun ResponsiveArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncArtwork(
                url = artist.artworkUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                cornerRadius = 16.dp,
                placeholderIcon = "🎤"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = artist.nameAssamese,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = artist.nameEnglish,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${artist.allSongs.size} songs • ${artist.albums.size} albums",
                fontSize = 10.sp,
                color = SoftGold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyCatalogState(message: String, onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎶", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = GlassPurple),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassPurpleBorder)
        ) {
            Text("Reset", color = TextPureWhite, fontSize = 12.sp)
        }
    }
}
