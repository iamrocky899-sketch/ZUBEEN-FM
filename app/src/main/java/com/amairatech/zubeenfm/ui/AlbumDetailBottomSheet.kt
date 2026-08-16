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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amairatech.zubeenfm.data.model.Album
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.ui.theme.DeepAmber
import com.amairatech.zubeenfm.ui.theme.GlassPurple
import com.amairatech.zubeenfm.ui.theme.GlassPurpleBorder
import com.amairatech.zubeenfm.ui.theme.GlassWhite
import com.amairatech.zubeenfm.ui.theme.GlassWhiteBorder
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
fun AlbumDetailBottomSheet(
    album: Album,
    currentPlayingSongId: String,
    isPlaying: Boolean,
    onPlaySong: (Song) -> Unit,
    onPlayAll: (Album) -> Unit,
    onShuffleAll: (Album) -> Unit,
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
            // Album Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassPurple)
                        .border(1.dp, GlassPurpleBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💿", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.nameAssamese,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = album.artistAssamese,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${album.songs.size} tracks • ${album.releaseYear}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Play All & Shuffle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onPlayAll(album) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("▶ Play All", color = TextPureWhite, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onShuffleAll(album) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftGold)
                ) {
                    Text("🔀 Shuffle", color = SoftGold, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TRACKS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SoftGold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Track List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(album.songs) { index, song ->
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) RoyalGold else TextMuted,
                                    modifier = Modifier.width(24.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.titleAssamese,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isCurrent) RoyalGold else TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.genreAssamese,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

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
