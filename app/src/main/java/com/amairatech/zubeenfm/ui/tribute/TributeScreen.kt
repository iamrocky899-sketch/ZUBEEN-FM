package com.amairatech.zubeenfm.ui.tribute

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.amairatech.zubeenfm.data.model.ZubeenStory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amairatech.zubeenfm.R

// Glassmorphism Imports
import com.amairatech.zubeenfm.ui.theme.GlassWhite
import com.amairatech.zubeenfm.ui.theme.GlassWhiteBorder
import com.amairatech.zubeenfm.ui.theme.GlassPurple
import com.amairatech.zubeenfm.ui.theme.GlassPurpleBorder
import com.amairatech.zubeenfm.ui.theme.TributeGlow
import com.amairatech.zubeenfm.ui.theme.GradientPurpleStart
import com.amairatech.zubeenfm.ui.theme.GradientIndigoEnd

// Purple Memorial Color Palette
private val MemorialDeepPurple = Color(0xFF0F0F23)
private val PurplePrimary = Color(0xFF8B5CF6)
private val PurpleAccent = Color(0xFF7C3AED)
private val PurpleSoft = Color(0xFFA78BFA)
private val PaleViolet = Color(0xFFC4B5FD)
private val TextWhite = Color(0xFFF1F5F9)
private val TextSoftMuted = Color(0xFF94A3B8)

@Composable
fun TributeScreen(
    viewModel: TributeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var burstTriggerId by remember { mutableStateOf(0L) }

    // Manage 10-second timer lifecycle: starts on entry, cancels on exit
    DisposableEffect(Unit) {
        viewModel.onScreenVisible()
        onDispose {
            viewModel.onScreenHidden()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientPurpleStart, GradientIndigoEnd)
                )
            )
    ) {
        // Subtle floating memorial particles in the background
        SubtleMemorialParticles(
            modifier = Modifier.fillMaxSize(),
            isAnimating = uiState.isScreenVisible
        )

        // Interactive Memorial Flower Petals Effect
        TributePetalEffect(
            burstTriggerId = burstTriggerId,
            isScreenVisible = uiState.isScreenVisible,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Animated Diya 🪔 at the top
            AnimatedDiya(
                isAnimating = uiState.isScreenVisible
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Memorial Medallion Portrait of Zubeen Garg (Touch to offer flower petals)
            MemorialPortraitMedallion(
                isGlowing = uiState.isScreenVisible,
                onTouchPhoto = {
                    burstTriggerId = System.currentTimeMillis()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. শ্ৰদ্ধাঞ্জলী Header
            Text(
                text = "শ্ৰদ্ধাঞ্জলী",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = PurplePrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 4. ❤️ Heartthrob ZUBEEN DA with ONLY the Heart Gently Pulsing
            HeartthrobTitle(
                isAnimating = uiState.isScreenVisible
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 5. Memorial Dates
            MemorialDatesSection()

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Assamese Tribute Line
            TributeQuoteSection()

            Spacer(modifier = Modifier.height(18.dp))

            // 7. Assamese Stories System: "জুবিন দাৰ কাহিনী"
            Spacer(modifier = Modifier.height(18.dp))

            AssameseStoriesSection(
                story = uiState.currentStory,
                onNextStory = {
                    viewModel.loadNextStory()
                }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Memorial Medallion displaying zubeen_portrait.jpg with center-crop and purple halo.
 */
@Composable
private fun MemorialPortraitMedallion(
    isGlowing: Boolean,
    onTouchPhoto: () -> Unit = {}
) {
    val haloAlpha by if (isGlowing) {
        val haloTransition = rememberInfiniteTransition(label = "HaloPulse")
        haloTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "HaloAlpha"
        )
    } else {
        remember { mutableStateOf(0.4f) }
    }

    val currentHaloAlpha = haloAlpha

    Box(
        modifier = Modifier
            .size(190.dp)
            .clip(CircleShape)
            .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClickLabel = "পুষ্পাঞ্জলি শ্ৰদ্ধাঞ্জলী অৰ্পণ কৰক",
                onClick = onTouchPhoto
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer Subtle Purple Halo / Glow
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TributeGlow.copy(alpha = currentHaloAlpha),
                            PurpleAccent.copy(alpha = currentHaloAlpha * 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Multi-Layered Purple Memorial Frame
        Box(
            modifier = Modifier
                .size(164.dp)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PurpleSoft,
                            PurplePrimary,
                            Color(0xFF5B21B6),
                            PurpleSoft
                        )
                    )
                )
                .border(2.5.dp, PaleViolet, CircleShape)
                .padding(4.5.dp),
            contentAlignment = Alignment.Center
        ) {
            // Authentic portrait image of Zubeen Garg (Accessible and Interactive)
            Image(
                painter = painterResource(id = R.drawable.zubeen_portrait),
                contentDescription = "জুবিন গাৰ্গৰ প্ৰতিচ্ছবি (পুষ্পাঞ্জলি শ্ৰদ্ধাঞ্জলীৰ বাবে স্পৰ্শ কৰক)",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

/**
 * Renders "❤️ Heartthrob ZUBEEN DA" where ONLY the heart gently pulses/blinks.
 */
@Composable
private fun HeartthrobTitle(
    isAnimating: Boolean
) {
    val heartScale by if (isAnimating) {
        val heartTransition = rememberInfiniteTransition(label = "HeartPulse")
        heartTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.20f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "HeartScale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val currentHeartScale = heartScale

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Only the ❤️ heart gently pulses
        Text(
            text = "❤️",
            fontSize = 18.sp,
            modifier = Modifier.scale(currentHeartScale)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Text remains static and stable
        Text(
            text = "Heartthrob ZUBEEN DA",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = TextWhite
        )
    }
}

/**
 * Memorial Lifespan Dates
 */
@Composable
private fun MemorialDatesSection() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "18-11-1972",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PaleViolet
            )
            Text(
                text = "—",
                fontSize = 14.sp,
                color = PurpleSoft
            )
            Text(
                text = "19-09-2025",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PaleViolet
            )
        }
    }
}

/**
 * Exact Assamese Tribute Line
 */
@Composable
private fun TributeQuoteSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "“তোমাৰ সুৰে আমাক সদায় জীয়াই থকাৰ সাহস দিব, জুবিন দা।”",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 10-Second Fact Revelation: "আপুনি জানেনে ?"
 */
@Composable
private fun AssameseFactSection(
    isFactVisible: Boolean,
    factAssamese: String?,
    categoryAssamese: String?,
    milestoneYear: String?
) {
    AnimatedVisibility(
        visible = isFactVisible && factAssamese != null,
        enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 2 })
    ) {
        if (factAssamese != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GlassPurple),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GlassPurpleBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GlassWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhiteBorder)
                        ) {
                            Text(
                                text = "আপুনি জানেনে ?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PaleViolet,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        if (categoryAssamese != null) {
                            Text(
                                text = categoryAssamese,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSoftMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = factAssamese,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 23.sp,
                        color = TextWhite
                    )

                    if (milestoneYear != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ঐতিহাসিক মাইলষ্ট’ন: $milestoneYear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PaleViolet,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Assamese Story Section: "জুবিন দাৰ কাহিনী"
 */
@Composable
private fun AssameseStoriesSection(
    story: ZubeenStory?,
    onNextStory: () -> Unit
) {
    if (story == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, GlassWhiteBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: "জুবিন দাৰ কাহিনী" Badge + Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GlassPurple,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassPurpleBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "জুবিন দাৰ কাহিনী",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PaleViolet
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GlassWhite
                ) {
                    Text(
                        text = story.category,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = PaleViolet,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Story Title (Assamese)
            Text(
                text = story.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PaleViolet,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Short Assamese Story Text
            Text(
                text = story.assameseText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 23.sp,
                color = TextWhite.copy(alpha = 0.95f),
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Source Footnote
            Text(
                text = "উৎস: ${story.sourceName}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = TextSoftMuted,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // "আন এটি কাহিনী" Button
            Button(
                onClick = onNextStory,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlassPurple,
                    contentColor = PaleViolet
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassPurpleBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📖",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "আন এটি কাহিনী",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PaleViolet
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun TributeScreenPreview() {
    TributeScreen()
}
