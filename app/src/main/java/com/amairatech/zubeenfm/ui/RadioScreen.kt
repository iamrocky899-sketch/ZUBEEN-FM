package com.amairatech.zubeenfm.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil.compose.AsyncImage
import com.amairatech.zubeenfm.data.repository.ZubeenFactRepository
import com.amairatech.zubeenfm.ui.components.AsyncArtwork
import com.amairatech.zubeenfm.ui.theme.*
import androidx.compose.ui.draw.blur
import com.amairatech.zubeenfm.R

/**
 * Modern Radio Station Experience for ZUBEEN FM.
 * Visually distinct from the Normal Mode player:
 * - Station header with LIVE indicator
 * - Dedicated Zubeen artwork card with ambient purple glow
 * - Dynamic audio visualizer reacting to playback
 * - Strictly PLAY / PAUSE control only (No seekbar, No next/prev, No timers)
 * - Synchronized continuous broadcast via RadioStationClock
 */
@Composable
fun RadioScreen(
    viewModel: RadioViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val waveformAmplitudes by viewModel.waveformAmplitudes.collectAsState()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val accentColor = Color(uiState.radioCurrentSong.accentColorHex)
        val isLandscape = maxWidth > maxHeight
        val availableHeight = maxHeight
        val boxWidthPx = constraints.maxWidth.toFloat()
        val boxHeightPx = constraints.maxHeight.toFloat()

        // Background: Deep purple/indigo gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
        )

        // Blurred Artwork Background Effect
        if (!uiState.radioCurrentSong.artworkUrl.isNullOrBlank()) {
            AsyncImage(
                model = uiState.radioCurrentSong.artworkUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (android.os.Build.VERSION.SDK_INT >= 31) Modifier.blur(60.dp) else Modifier),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.25f
            )
        }

        // Blurred ambient colors behind the artwork
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(boxWidthPx / 2f, boxHeightPx / 3f),
                        radius = boxWidthPx * 0.8f
                    )
                )
        )

        // Dark Overlay for Glassmorphism depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GlassDarkOverlay)
        )

        if (isLandscape) {
            // Landscape Layout: Side-by-side
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Zubeen Station Artwork
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    RadioArtworkCard(
                        isPlaying = uiState.isRadioPlaying,
                        artworkUrl = uiState.radioCurrentSong.artworkUrl,
                        maxCardSize = 200.dp
                    )
                }

                // Right: Station Metadata & Single Large Play/Pause
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(start = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    RadioStationHeader(isPlaying = uiState.isRadioPlaying)

                    RadioNowPlayingInfo(
                        titleAssamese = uiState.radioCurrentSong.titleAssamese,
                        albumAssamese = uiState.radioCurrentSong.albumAssamese,
                        artistAssamese = uiState.radioCurrentSong.artistAssamese
                    )

                    RadioAudioVisualizer(
                        isPlaying = uiState.isRadioPlaying,
                        amplitudes = waveformAmplitudes,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(28.dp)
                    )

                    RadioPlayPauseControl(
                        isPlaying = uiState.isRadioPlaying,
                        isBuffering = uiState.isRadioBuffering,
                        onTogglePlayPause = { viewModel.togglePlayPauseRadio() }
                    )
                }
            }
        } else {
            // Portrait Layout: Responsive vertical fit
            val cardSize = when {
                availableHeight < 600.dp -> 160.dp
                availableHeight < 720.dp -> 210.dp
                else -> 250.dp
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Station Branding & Live Status
                RadioStationHeader(isPlaying = uiState.isRadioPlaying)

                // Error Notice Banner if any
                AnimatedVisibility(
                    visible = uiState.errorMessage != null || uiState.isRadioUnavailable,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassPurple),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LiveCrimson.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "Radio stream reconnecting...",
                                color = TextPureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { viewModel.retryPlayback() },
                                colors = ButtonDefaults.buttonColors(containerColor = GradientPurpleStart),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Retry", fontSize = 11.sp, color = TextPureWhite)
                            }
                        }
                    }
                }

                // 2. Central Zubeen Station Artwork Card
                RadioArtworkCard(
                    isPlaying = uiState.isRadioPlaying,
                    artworkUrl = uiState.radioCurrentSong.artworkUrl,
                    maxCardSize = cardSize
                )

                // 3. Now Playing Song Information
                RadioNowPlayingInfo(
                    titleAssamese = uiState.radioCurrentSong.titleAssamese,
                    albumAssamese = uiState.radioCurrentSong.albumAssamese,
                    artistAssamese = uiState.radioCurrentSong.artistAssamese
                )

                // 4. Audio Visualizer Equalizer Waves
                RadioAudioVisualizer(
                    isPlaying = uiState.isRadioPlaying,
                    amplitudes = waveformAmplitudes,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(32.dp)
                )

                // 5. Single Large Central Play/Pause Button
                RadioPlayPauseControl(
                    isPlaying = uiState.isRadioPlaying,
                    isBuffering = uiState.isRadioBuffering,
                    onTogglePlayPause = { viewModel.togglePlayPauseRadio() }
                )

                // 6. "আপুনি জানেনে ?" Assamese Fact Card
                ZubeenFactCard()

                // 7. Footer: Live Broadcast Note
                Text(
                    text = "LIVE SYNCHRONIZED BROADCAST • 24/7",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Compact, premium "আপুনি জানেনে ?" Assamese Fact Card for Radio Screen.
 * Displays fact-checked milestones and biographical facts about Zubeen Garg.
 * Completely isolated state so changing facts never interrupts or restarts playback.
 */
@Composable
private fun ZubeenFactCard(
    modifier: Modifier = Modifier
) {
    var factIndex by remember { mutableStateOf(0) }
    val facts = remember { ZubeenFactRepository.verifiedFacts }
    val currentFact = facts[factIndex % facts.size]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "আপুনি জানেনে ?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0E7FF)
                    )
                }

                Text(
                    text = currentFact.categoryAssamese,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentFact.factAssamese,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = TextPureWhite,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { factIndex++ },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "আন এটি তথ্য →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA78BFA)
                    )
                }
            }
        }
    }
}

/**
 * Station Branding Header with pulsing LIVE indicator.
 */
@Composable
private fun RadioStationHeader(
    isPlaying: Boolean
) {
    val alpha by if (isPlaying) {
        val livePulse = rememberInfiniteTransition(label = "LivePulse")
        livePulse.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "LiveAlpha"
        )
    } else {
        remember { mutableStateOf(0.4f) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ZUBEEN FM",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = TextPureWhite
            )
            Text(
                text = "হৃদয়ৰ গান • অফিচিয়েল ৰেডিঅ'",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        // Live Badge Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isPlaying) LiveCrimson.copy(alpha = 0.2f) else GlassWhite,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isPlaying) LiveCrimson.copy(alpha = alpha) else GlassWhiteBorder
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) LiveCrimson.copy(alpha = alpha) else TextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPlaying) "LIVE" else "ON AIR",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) TextPureWhite else TextMuted
                )
            }
        }
    }
}

/**
 * Dedicated Zubeen Station Artwork Card with subtle ambient purple glow.
 */
@Composable
private fun RadioArtworkCard(
    isPlaying: Boolean,
    artworkUrl: String?,
    maxCardSize: Dp
) {
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 0.6f else 0.2f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "RadioGlowAlpha"
    )

    Box(
        modifier = Modifier
            .sizeIn(maxWidth = maxCardSize, maxHeight = maxCardSize)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Radial Glow
        Box(
            modifier = Modifier
                .fillMaxSize(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GradientPurpleStart.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Station Artwork Card Container
        Box(
            modifier = Modifier
                .fillMaxSize(0.88f)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(GlassWhite)
                .border(1.5.dp, GlassPurpleBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncArtwork(
                url = artworkUrl,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 24.dp,
                placeholderIcon = "📻"
            )
        }
    }
}

/**
 * Now Playing Metadata: Song Title, Zubeen Garg, Album.
 */
@Composable
private fun RadioNowPlayingInfo(
    titleAssamese: String,
    albumAssamese: String,
    artistAssamese: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "NOW PLAYING",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Color(0xFFA78BFA)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = titleAssamese,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPureWhite,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$artistAssamese • $albumAssamese",
            fontSize = 13.sp,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Modern Audio Equalizer Bars Visualizer in Purple/Indigo.
 * Restrained motion; static/dim when paused to save battery.
 */
@Composable
private fun RadioAudioVisualizer(
    isPlaying: Boolean,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val barCount = amplitudes.size.coerceAtLeast(1)
        val spacing = 3.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val amp = if (isPlaying) amplitudes[i] else 0.15f
            val barHeight = (maxHeight * amp).coerceIn(4.dp.toPx(), maxHeight)

            val x = i * (barWidth + spacing)
            val y = (maxHeight - barHeight) / 2

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = if (isPlaying) listOf(Color(0xFFC4B5FD), Color(0xFF7C3AED)) else listOf(GlassPurple, GlassPurple)
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

/**
 * Single Large Central Play/Pause Control.
 * Radio does NOT have seek, next, prev, shuffle, or repeat.
 */
@Composable
private fun RadioPlayPauseControl(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onTogglePlayPause: () -> Unit
) {
    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.95f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "RadioPlayScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Glowing Outer Ring
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(
                    if (isPlaying) GlassPurple else Color.Transparent
                )
                .border(
                    width = 1.5.dp,
                    color = if (isPlaying) GlassPurpleBorder else GlassWhiteBorder,
                    shape = CircleShape
                )
        )

        // Large Play / Pause Button
        FilledIconButton(
            onClick = onTogglePlayPause,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = GlassWhite,
                contentColor = TextPureWhite
            ),
            modifier = Modifier
                .size(72.dp)
                .scale(playScale)
                .shadow(16.dp, CircleShape)
                .border(1.5.dp, GlassWhiteBorder, CircleShape)
        ) {
            if (isBuffering) {
                CircularProgressIndicator(
                    color = TextPureWhite,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Text(
                    text = if (isPlaying) "⏸" else "▶",
                    fontSize = 30.sp,
                    color = TextPureWhite,
                    modifier = Modifier.padding(start = if (isPlaying) 0.dp else 2.dp)
                )
            }
        }
    }
}
