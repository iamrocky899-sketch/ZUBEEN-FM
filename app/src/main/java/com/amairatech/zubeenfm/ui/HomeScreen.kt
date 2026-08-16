package com.amairatech.zubeenfm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.data.provider.AssameseMusicFilter
import com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository
import com.amairatech.zubeenfm.ui.theme.*
import com.amairatech.zubeenfm.ui.components.AsyncArtwork
import com.amairatech.zubeenfm.ui.components.GlassCard

/**
 * Redesigned Home Screen for ZUBEEN FM.
 * 1. Radio / Normal Mode Cards
 * 2. TOP CHARTS (exactly 10, Assamese only)
 * 3. LISTEN AGAIN (top 5 history)
 * 4. RECENTLY PLAYED (rest of history)
 */
@Composable
fun HomeScreen(
    onNavigateToRadio: () -> Unit,
    onNavigateToSongs: () -> Unit,
    onNavigateToTribute: () -> Unit,
    onPlaySong: (Song) -> Unit
) {
    val allSongs by NormalCatalogueRepository.songsFlow.collectAsState()
    val recentlyPlayed by NormalCatalogueRepository.recentlyPlayedFlow.collectAsState()
    val newReleasesDiscovered by NormalCatalogueRepository.newReleasesFlow.collectAsState()

    // 180-day window for "New Releases"
    val windowMillis = 180L * 24 * 60 * 60 * 1000L
    val now = System.currentTimeMillis()
    
    val newReleases = remember(newReleasesDiscovered, allSongs) {
        val combined = (newReleasesDiscovered + allSongs.filter { 
            it.releaseTimestamp > (now - windowMillis) && AssameseMusicFilter.isValidForNormalCatalogue(it)
        }).distinctBy { it.id }
        combined.sortedByDescending { it.releaseTimestamp }
    }

    val topCharts = allSongs
        .filter { AssameseMusicFilter.isValidForNormalCatalogue(it) }
        .take(10)

    val listenAgain = recentlyPlayed.take(5)
    val remainingRecentlyPlayed = if (recentlyPlayed.size > 5) recentlyPlayed.drop(5) else emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientPurpleStart,
                        GradientIndigoEnd
                    )
                )
            ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Radio Mode / Normal Mode
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Radio Mode Card
                GlassModeCard(
                    modifier = Modifier.weight(1f),
                    title = "RADIO MODE",
                    subtitle = "ZUBEEN FM LIVE",
                    icon = "📻",
                    onClick = onNavigateToRadio,
                    accentColor = LiveCrimson
                )

                // Normal Mode Card
                GlassModeCard(
                    modifier = Modifier.weight(1f),
                    title = "NORMAL MODE",
                    subtitle = "ASSAMESE MUSIC",
                    icon = "🎵",
                    onClick = onNavigateToSongs,
                    accentColor = RoyalGold
                )
            }
        }

        // 2. NEW RELEASES
        if (newReleases.isNotEmpty()) {
            item {
                SectionHeader(title = "NEW RELEASES", subtitle = "Fresh Assamese Music")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(newReleases, key = { "new_${it.id}" }) { song ->
                        ListenAgainCard(song = song, onClick = { onPlaySong(song) })
                    }
                }
            }
        }

        // 3. TOP CHARTS
        item {
            SectionHeader(title = "TOP CHARTS", subtitle = "Trending Assamese Songs")
        }

        if (topCharts.isEmpty()) {
            item {
                EmptyStateCard("No trending data available yet.")
            }
        } else {
            itemsIndexed(topCharts, key = { _, song -> "chart_${song.id}" }) { index, song ->
                TopChartRow(
                    index = index + 1,
                    song = song,
                    onClick = { onPlaySong(song) }
                )
            }
        }

        // 3. LISTEN AGAIN
        item {
            SectionHeader(title = "LISTEN AGAIN", subtitle = "Jump back in")
        }

        if (listenAgain.isEmpty()) {
            item {
                EmptyStateCard("Start listening to build your history.")
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(listenAgain, key = { "listen_${it.id}" }) { song ->
                        ListenAgainCard(song = song, onClick = { onPlaySong(song) })
                    }
                }
            }
        }

        // 4. RECENTLY PLAYED
        if (remainingRecentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "RECENTLY PLAYED", subtitle = "Your history")
            }

            items(remainingRecentlyPlayed, key = { "recent_${it.id}" }) { song ->
                RecentlyPlayedRow(song = song, onClick = { onPlaySong(song) })
            }
        }

        // 5. Tribute Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTribute() }
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassWhite),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, TributeGlow.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(GlassPurple).border(1.dp, TributeGlow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🪔", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "জুবিন গাৰ্গ শ্ৰদ্ধাঞ্জলী", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPureWhite)
                        Text(text = "Explore facts & pay tribute", fontSize = 11.sp, color = SoftGold)
                    }
                    Text(text = "›", fontSize = 22.sp, color = RoyalGold)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun GlassModeCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    accentColor: Color
) {
    GlassCard(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        cornerRadius = 24.dp,
        backgroundColor = GlassPurple.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPureWhite,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = SoftGold,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = TextPureWhite,
            letterSpacing = 1.sp
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TopChartRow(
    index: Int,
    song: Song,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassWhite, RoundedCornerShape(16.dp))
                .border(0.5.dp, GlassWhiteBorder.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index.toString().padStart(2, '0'),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = RoyalGold,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center
            )

            AsyncArtwork(
                url = song.artworkUrl,
                modifier = Modifier.size(52.dp),
                cornerRadius = 10.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.titleAssamese,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artistAssamese,
                    fontSize = 12.sp,
                    color = SoftGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(text = "⋮", color = TextMuted, fontSize = 20.sp)
        }
    }
}

@Composable
private fun ListenAgainCard(
    song: Song,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() },
        cornerRadius = 20.dp,
        backgroundColor = GlassPurple.copy(alpha = 0.8f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncArtwork(
                url = song.artworkUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                cornerRadius = 12.dp,
                placeholderIcon = "💿"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = song.titleAssamese,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artistAssamese,
                fontSize = 11.sp,
                color = SoftGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentlyPlayedRow(
    song: Song,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncArtwork(
            url = song.artworkUrl,
            modifier = Modifier.size(44.dp),
            cornerRadius = 10.dp,
            placeholderIcon = "🕒"
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.titleAssamese,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artistAssamese,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder.copy(alpha = 0.1f))
    ) {
        Text(
            text = message,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = TextMuted,
            fontSize = 14.sp
        )
    }
}
