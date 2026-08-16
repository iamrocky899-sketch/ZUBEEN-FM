package com.amairatech.zubeenfm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amairatech.zubeenfm.ui.components.AsyncArtwork
import com.amairatech.zubeenfm.ui.theme.*

val GlassPurple = Color(0x268B5CF6)
val GlassWhiteBorderToken = Color(0x26FFFFFF)
val GlassPurpleBorderToken = Color(0x338B5CF6)
val GradientPurpleStartToken = Color(0xFF4C1D95)

/**
 * Floating Glass Capsule Mini Player for ZUBEEN FM.
 * Glassmorphism design with translucent background and purple accents.
 * Dynamically switches display between Normal Music Mode and Radio Mode.
 */
@Composable
fun MiniPlayer(
    uiState: RadioUiState,
    onTogglePlayPause: () -> Unit,
    onClickExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRadio = uiState.isRadioPlaying || (uiState.activePlaybackMode == PlaybackMode.RADIO)
    val currentSong = if (isRadio) uiState.radioCurrentSong else uiState.normalCurrentSong
    val isPlaying = if (isRadio) uiState.isRadioPlaying else uiState.isNormalPlaying
    val songProgress = if (isRadio) uiState.radioSongProgress else uiState.normalSongProgress

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .shadow(16.dp, RoundedCornerShape(percent = 50))
            .clickable { onClickExpand() },
        shape = RoundedCornerShape(percent = 50),
        colors = CardDefaults.cardColors(
            containerColor = GlassPurple
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorderToken)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Mini Artwork Thumbnail
                AsyncArtwork(
                    url = currentSong.artworkUrl,
                    modifier = Modifier.size(44.dp),
                    cornerRadius = 22.dp, // Circle
                    placeholderIcon = if (isRadio) "📻" else "🎵"
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Center: Text Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isRadio) {
                        // Radio Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ZUBEEN FM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftGold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LiveCrimson)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiveCrimson
                            )
                        }
                        Text(
                            text = currentSong.titleAssamese,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        // Normal Mode Track Info
                        Text(
                            text = currentSong.titleAssamese,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentSong.artistAssamese} • ${currentSong.albumAssamese}",
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Play / Pause Button — glass purple style
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = GradientPurpleStartToken,
                        contentColor = TextPureWhite
                    )
                ) {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPureWhite
                    )
                }
            }

            // Slim Bottom Progress Indicator — purple
            LinearProgressIndicator(
                progress = { songProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = GradientPurpleStartToken,
                trackColor = Color(0x33000000)
            )
        }
    }
}
