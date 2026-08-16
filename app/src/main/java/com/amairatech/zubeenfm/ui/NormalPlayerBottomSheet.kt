package com.amairatech.zubeenfm.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amairatech.zubeenfm.R
import com.amairatech.zubeenfm.data.model.Song
import com.amairatech.zubeenfm.ui.components.AsyncArtwork
import com.amairatech.zubeenfm.ui.theme.*

/**
 * Modern Premium Normal Mode Music Player Bottom Sheet.
 * Features:
 * - Purple/Indigo Glassmorphism design
 * - Full immersive dark gradient background with blurred artwork aura
 * - Lightweight waveform visualization
 * - Glass layout for controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalPlayerBottomSheet(
    song: Song,
    isPlaying: Boolean,
    elapsedSeconds: Int,
    songProgress: Float,
    isFavorite: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: NormalRepeatMode = NormalRepeatMode.OFF,
    isRepeatEnabled: Boolean = repeatMode != NormalRepeatMode.OFF,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Int) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(songProgress) }

    val safeDuration = song.durationSeconds.coerceAtLeast(1)

    val playScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.96f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "PlayButtonScale"
    )

    // Using a subtle purple/indigo theme with Dark overlay as specified.
    // Try to safely extract accentColorHex if available, otherwise fallback to DeepAmber
    val accentColorStr = runCatching { 
        val hexMethod = song.javaClass.getMethod("getAccentColorHex")
        hexMethod.invoke(song) as? String ?: "#FFB300"
    }.getOrDefault("#FFB300")

    val accentColor = runCatching { 
        Color(android.graphics.Color.parseColor(accentColorStr))
    }.getOrDefault(DeepAmber)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent, // Transparent so our gradient shows through fully
        dragHandle = null // We provide our own top bar
    ) {
        // Full immersive dark gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientPurpleStart.copy(alpha = 0.2f),
                            GradientIndigoEnd,
                            Color(0xFF0A0A18)
                        )
                    )
                )
        ) {
            // Blurred Artwork Background Effect
            if (!song.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = song.artworkUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .then(if (android.os.Build.VERSION.SDK_INT >= 31) Modifier.blur(60.dp) else Modifier),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    alpha = 0.3f
                )
            }
            
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            radius = 1500f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar: Close button on left, More options on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Text(
                            text = "▼",
                            color = TextPureWhite,
                            fontSize = 20.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp,
                            color = TextPureWhite.copy(alpha = 0.8f)
                        )
                    }

                    IconButton(onClick = { /* More options not explicitly requested to have logic */ }) {
                        Text(
                            text = "⋮",
                            color = TextPureWhite,
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Artwork: Large square, ~24dp corners, glass border, soft purple glow, subtle elevation.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(maxWidth = 320.dp, maxHeight = 320.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Soft purple glow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .aspectRatio(1f)
                            .shadow(32.dp, RoundedCornerShape(24.dp), spotColor = GradientPurpleStart, ambientColor = GradientPurpleStart)
                            .background(Color.Transparent)
                    )

                    // Glass border and artwork container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(GlassPurple.copy(alpha = 0.2f))
                            .border(1.5.dp, GlassWhiteBorder, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncArtwork(
                            url = song.artworkUrl,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 24.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Song Info: Large high-contrast title, smaller secondary artist text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.titleAssamese,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${song.artistAssamese} • ${song.languageAssamese}",
                            fontSize = 15.sp,
                            color = TextMuted, // Using TextMuted as requested for secondary text
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Secondary Controls: Redesign existing ones (Favorite)
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isFavorite) GlassPurple else GlassWhite.copy(alpha = 0.05f))
                            .border(1.dp, GlassWhiteBorder, CircleShape)
                    ) {
                        Text(
                            text = if (isFavorite) "❤️" else "🤍",
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Visualization: modern lightweight vertical bars connected to isPlaying
                SimpleWaveform(isPlaying = isPlaying)

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Area: Thin glass progress bar. Purple/indigo active indicator.
                Slider(
                    value = if (isDraggingSlider) sliderPosition else songProgress.coerceIn(0f, 1f),
                    onValueChange = {
                        isDraggingSlider = true
                        sliderPosition = it.coerceIn(0f, 1f)
                    },
                    onValueChangeFinished = {
                        val targetSecs = (sliderPosition * safeDuration).toInt().coerceIn(0, safeDuration)
                        onSeek(targetSecs)
                        isDraggingSlider = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = TextPureWhite,
                        activeTrackColor = GradientPurpleStart,
                        inactiveTrackColor = GlassWhiteBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp) // Thin look
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentSecs = if (isDraggingSlider) {
                        (sliderPosition * safeDuration).toInt().coerceIn(0, safeDuration)
                    } else {
                        elapsedSeconds.coerceIn(0, safeDuration)
                    }
                    Text(
                        text = formatTime(currentSecs),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPureWhite.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatTime(safeDuration),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPureWhite.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Playback Controls: Glass layout. Order: Shuffle, Previous, Large Play/Pause (Circular), Next, Repeat.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isShuffleEnabled) GlassPurple else Color.Transparent)
                    ) {
                        Text(text = "🔀", fontSize = 18.sp, color = if (isShuffleEnabled) TextPureWhite else TextMuted)
                    }

                    // Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Text(text = "⏮️", fontSize = 28.sp)
                    }

                    // Large Play/Pause (Circular)
                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = GradientPurpleStart,
                            contentColor = TextPureWhite
                        ),
                        modifier = Modifier
                            .size(80.dp) // Largest
                            .scale(playScale)
                            .shadow(16.dp, CircleShape, spotColor = GradientPurpleStart)
                            .border(1.5.dp, GlassWhiteBorder, CircleShape)
                    ) {
                        Text(
                            text = if (isPlaying) "⏸" else "▶",
                            fontSize = 32.sp,
                            color = TextPureWhite
                        )
                    }

                    // Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Text(text = "⏭️", fontSize = 28.sp)
                    }

                    // Repeat
                    val isRepeatActive = repeatMode != NormalRepeatMode.OFF
                    val repeatIconText = when (repeatMode) {
                        NormalRepeatMode.OFF -> "🔁"
                        NormalRepeatMode.ALL -> "🔁"
                        NormalRepeatMode.ONE -> "🔂"
                    }
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isRepeatActive) GlassPurple else Color.Transparent)
                    ) {
                        Text(text = repeatIconText, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SimpleWaveform(isPlaying: Boolean) {
    val GradientIndigoStart = Color(0xFF312E81)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "wave")
        for (i in 0 until 16) {
            val animDuration = remember { (300..700).random() }
            val scale by if (isPlaying) {
                infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(animDuration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "wave_anim_$i"
                )
            } else {
                animateFloatAsState(targetValue = 0.15f, label = "wave_idle_$i")
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.dp)
                    .fillMaxHeight(scale)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(if (i % 2 == 0) GradientPurpleStart else GradientIndigoStart)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    return "${seconds / 60}:${String.format("%02d", seconds % 60)}"
}
