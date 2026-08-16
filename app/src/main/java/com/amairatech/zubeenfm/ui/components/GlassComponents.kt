package com.amairatech.zubeenfm.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amairatech.zubeenfm.R
import com.amairatech.zubeenfm.ui.theme.DeepAmber
import com.amairatech.zubeenfm.ui.theme.GlassDarkOverlay
import com.amairatech.zubeenfm.ui.theme.GlassPurple
import com.amairatech.zubeenfm.ui.theme.GlassPurpleBorder
import com.amairatech.zubeenfm.ui.theme.GlassWhite
import com.amairatech.zubeenfm.ui.theme.GlassWhiteBorder
import com.amairatech.zubeenfm.ui.theme.GradientIndigoEnd
import com.amairatech.zubeenfm.ui.theme.GradientPurpleStart
import com.amairatech.zubeenfm.ui.theme.ObsidianBackground
import com.amairatech.zubeenfm.ui.theme.ObsidianBorder
import com.amairatech.zubeenfm.ui.theme.ObsidianCard
import com.amairatech.zubeenfm.ui.theme.RoyalGold
import com.amairatech.zubeenfm.ui.theme.SoftGold
import com.amairatech.zubeenfm.ui.theme.TextMuted
import com.amairatech.zubeenfm.ui.theme.TextPrimary
import com.amairatech.zubeenfm.ui.theme.TextPureWhite

/**
 * Glassmorphism Card — translucent dark surface with moderate backdrop blur.
 * Requirement: ~25% frosted-glass effect.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderColor: Color = GlassWhiteBorder.copy(alpha = 0.3f),
    backgroundColor: Color = Color(0xFF1E1E32).copy(alpha = 0.7f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(cornerRadius))
            .background(backgroundColor, RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

/**
 * Optimized Artwork rendering component with Glass fallback.
 */
@Composable
fun AsyncArtwork(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cornerRadius: Dp = 12.dp,
    placeholderIcon: String = "🎵"
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(GlassPurple.copy(alpha = 0.5f))
            .border(0.5.dp, GlassWhiteBorder.copy(alpha = 0.2f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = "Artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                error = painterResource(id = R.drawable.zubeen_portrait), // Branded fallback
                placeholder = painterResource(id = R.drawable.zubeen_portrait)
            )
        } else {
            // Branded Fallback
            Image(
                painter = painterResource(id = R.drawable.zubeen_portrait),
                contentDescription = "Zubeen FM",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
            Text(text = placeholderIcon, fontSize = 24.sp)
        }
    }
}

/**
 * Glass Chip — for tab/category selection with glassmorphism.
 */
@Composable
fun GlassChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = DeepAmber
) {
    val bgColor = if (isSelected) selectedColor.copy(alpha = 0.25f) else GlassWhite
    val borderCol = if (isSelected) selectedColor.copy(alpha = 0.6f) else GlassWhiteBorder
    val textColor = if (isSelected) TextPureWhite else TextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * Glass Bottom Navigation Bar — floating glass capsule with purple glow for active item.
 * Presentation-only: accepts items and selected state, emits onSelect.
 */
@Composable
fun GlassBottomNavBar(
    items: List<GlassNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF12122A),
                            Color(0xFF0A0A1C)
                        )
                    )
                )
                .border(1.dp, GlassWhiteBorder, RoundedCornerShape(28.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) DeepAmber.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onItemSelected(index) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Icon with glow effect when selected
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    // Purple glow behind icon
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        DeepAmber.copy(alpha = 0.35f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                }
                                Text(
                                    text = item.icon,
                                    fontSize = if (isSelected) 18.sp else 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SoftGold else TextMuted,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class GlassNavItem(
    val label: String,
    val icon: String
)
