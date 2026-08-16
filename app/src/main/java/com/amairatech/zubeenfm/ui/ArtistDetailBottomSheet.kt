package com.amairatech.zubeenfm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amairatech.zubeenfm.data.model.Album
import com.amairatech.zubeenfm.data.model.Artist
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.ui.theme.DeepAmber
import com.amairatech.zubeenfm.ui.theme.GlassPurple
import com.amairatech.zubeenfm.ui.theme.GlassPurpleBorder
import com.amairatech.zubeenfm.ui.theme.GlassWhite
import com.amairatech.zubeenfm.ui.theme.GlassWhiteBorder
import com.amairatech.zubeenfm.ui.theme.GradientPurpleEnd
import com.amairatech.zubeenfm.ui.theme.GradientPurpleStart
import com.amairatech.zubeenfm.ui.theme.ObsidianBackground
import com.amairatech.zubeenfm.ui.theme.ObsidianBorder
import com.amairatech.zubeenfm.ui.theme.ObsidianCard
import com.amairatech.zubeenfm.ui.theme.RoyalGold
import com.amairatech.zubeenfm.ui.theme.SoftGold
import com.amairatech.zubeenfm.ui.theme.TextMuted
import com.amairatech.zubeenfm.ui.theme.TextPrimary
import com.amairatech.zubeenfm.ui.theme.TextPureWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailBottomSheet(
    artist: Artist,
    currentPlayingSongId: String,
    isPlaying: Boolean,
    onPlaySong: (Song) -> Unit,
    onPlayAll: (Artist) -> Unit,
    onSelectAlbum: (Album) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF080818),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GlassPurpleBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Artist Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientPurpleStart, GradientPurpleEnd)))
                        .border(1.5.dp, GlassPurpleBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎤", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist.nameAssamese,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artist.nameEnglish,
                        fontSize = 13.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${artist.allSongs.size} songs • ${artist.albums.size} albums",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: Play All
            Button(
                onClick = { onPlayAll(artist) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DeepAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("▶ Play All Tracks", color = TextPureWhite, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val displaySongs = com.amairatech.zubeenfm.data.repository.NormalCatalogueRepository.getSongsForArtist(artist).ifEmpty { artist.allSongs }

            Text(
                text = if (artist.isZubeenGarg) "VERIFIED CATALOGUE (ALL LANGUAGES)" else "ASSAMESE SONGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SoftGold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Song List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displaySongs, key = { it.id }) { song ->
                    val isCurrent = song.id == currentPlayingSongId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaySong(song) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) GlassPurple else GlassWhite
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isCurrent) RoyalGold else GlassWhiteBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = song.titleAssamese,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isCurrent) RoyalGold else TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    // Original Language Badge Chip (অসমীয়া, হাদী, বাংলা, etc.)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (song.originalLanguage.uppercase()) {
                                            "HINDI" -> Color(0xFF4A148C).copy(alpha = 0.6f)
                                            "BENGALI" -> Color(0xFF311B92).copy(alpha = 0.6f)
                                            "ASSAMESE" -> Color(0xFF1A237E).copy(alpha = 0.6f)
                                            else -> Color(0xFF4A148C).copy(alpha = 0.6f)
                                        },
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            when (song.originalLanguage.uppercase()) {
                                                "HINDI" -> Color(0xFFAB47BC)
                                                "BENGALI" -> Color(0xFF7E57C2)
                                                "ASSAMESE" -> Color(0xFF5C6BC0)
                                                else -> Color(0xFFAB47BC)
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = song.languageAssamese,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPureWhite,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "${song.albumAssamese} • ${song.genreAssamese}",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = String.format("%02d:%02d", song.durationSeconds / 60, song.durationSeconds % 60),
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
