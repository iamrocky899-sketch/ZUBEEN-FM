package com.amairatech.zubeenfm.ui.tribute

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

import com.amairatech.zubeenfm.ui.theme.TributeGlow
import com.amairatech.zubeenfm.ui.theme.DiyaFlameGold
import com.amairatech.zubeenfm.ui.theme.DiyaFlameOrange

@Composable
fun AnimatedDiya(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true
) {
    val transitionStates = if (isAnimating) {
        val transition = rememberInfiniteTransition(label = "DiyaFlicker")
        val flameScaleY = transition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "FlameScaleY"
        )
        val flameSway = transition.animateFloat(
            initialValue = -1.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "FlameSway"
        )
        val glowAlpha = transition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "GlowPulse"
        )
        Triple(flameScaleY.value, flameSway.value, glowAlpha.value)
    } else {
        Triple(1.0f, 0f, 0.5f)
    }

    val currentScaleY = transitionStates.first
    val currentSway = transitionStates.second
    val currentGlow = transitionStates.third

    Box(
        modifier = modifier.size(100.dp, 85.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val lampBaseY = height * 0.55f

            // 1. Purple Radial Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        TributeGlow.copy(alpha = 0.4f * currentGlow),
                        Color(0xFF7C3AED).copy(alpha = 0.2f * currentGlow),
                        Color.Transparent
                    ),
                    center = Offset(centerX, height * 0.35f),
                    radius = width * 0.7f
                ),
                radius = width * 0.7f,
                center = Offset(centerX, height * 0.35f)
            )

            // 2. Flickering Flame (Keep it warm against purple background)
            val flameCenter = Offset(centerX + currentSway, height * 0.32f)
            val flameW = 12f
            val flameH = 26f * currentScaleY

            // Outer Orange/Gold Flame
            val outerFlamePath = Path().apply {
                moveTo(flameCenter.x, flameCenter.y - flameH)
                cubicTo(
                    flameCenter.x + flameW, flameCenter.y - flameH * 0.3f,
                    flameCenter.x + flameW * 0.8f, flameCenter.y + flameH * 0.5f,
                    flameCenter.x, flameCenter.y + flameH * 0.5f
                )
                cubicTo(
                    flameCenter.x - flameW * 0.8f, flameCenter.y + flameH * 0.5f,
                    flameCenter.x - flameW, flameCenter.y - flameH * 0.3f,
                    flameCenter.x, flameCenter.y - flameH
                )
                close()
            }
            drawPath(
                path = outerFlamePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE082),
                        DiyaFlameGold,
                        DiyaFlameOrange
                    ),
                    startY = flameCenter.y - flameH,
                    endY = flameCenter.y + flameH * 0.5f
                ),
                style = Fill
            )

            // Inner Bright Yellow/White Core Flame
            val innerFlameH = flameH * 0.6f
            val innerFlameW = flameW * 0.5f
            val innerFlamePath = Path().apply {
                moveTo(flameCenter.x, flameCenter.y - innerFlameH * 0.8f)
                cubicTo(
                    flameCenter.x + innerFlameW, flameCenter.y,
                    flameCenter.x + innerFlameW * 0.7f, flameCenter.y + innerFlameH * 0.4f,
                    flameCenter.x, flameCenter.y + innerFlameH * 0.4f
                )
                cubicTo(
                    flameCenter.x - innerFlameW * 0.7f, flameCenter.y + innerFlameH * 0.4f,
                    flameCenter.x - innerFlameW, flameCenter.y,
                    flameCenter.x, flameCenter.y - innerFlameH * 0.8f
                )
                close()
            }
            drawPath(
                path = innerFlamePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFF9C4),
                        Color(0xFFFFD54F)
                    )
                ),
                style = Fill
            )

            // 3. Brass/Terracotta Diya Base
            val diyaPath = Path().apply {
                moveTo(centerX - width * 0.36f, lampBaseY)
                // Top curve/rim
                cubicTo(
                    centerX - width * 0.18f, lampBaseY + 4f,
                    centerX + width * 0.18f, lampBaseY + 4f,
                    centerX + width * 0.36f, lampBaseY
                )
                // Right bottom contour
                cubicTo(
                    centerX + width * 0.28f, lampBaseY + height * 0.26f,
                    centerX + width * 0.12f, lampBaseY + height * 0.34f,
                    centerX, lampBaseY + height * 0.35f
                )
                // Left bottom contour
                cubicTo(
                    centerX - width * 0.12f, lampBaseY + height * 0.34f,
                    centerX - width * 0.28f, lampBaseY + height * 0.26f,
                    centerX - width * 0.36f, lampBaseY
                )
                close()
            }

            drawPath(
                path = diyaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFC107), // Gold top rim
                        Color(0xFFD84315), // Warm terracotta
                        Color(0xFF4E2609)  // Deep shadow base
                    ),
                    startY = lampBaseY,
                    endY = lampBaseY + height * 0.35f
                )
            )

            // Diya Rim Golden Accent
            drawArc(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFB8860B), Color(0xFFFFD700), Color(0xFFB8860B))
                ),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerX - width * 0.36f, lampBaseY - 3f),
                size = androidx.compose.ui.geometry.Size(width * 0.72f, 10f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
            )
        }
    }
}

@Composable
fun SubtleMemorialParticles(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true
) {
    val drifts = if (isAnimating) {
        val transition = rememberInfiniteTransition(label = "Particles")
        val drift1 = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(7000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Drift1"
        )
        val drift2 = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(11000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Drift2"
        )
        Pair(drift1.value, drift2.value)
    } else {
        Pair(0f, 0f)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Pre-computed particle points with varying phase
        val particles = listOf(
            Triple(0.15f, 0.25f, 0.4f),
            Triple(0.85f, 0.35f, 0.7f),
            Triple(0.22f, 0.65f, 0.2f),
            Triple(0.78f, 0.75f, 0.5f),
            Triple(0.50f, 0.15f, 0.9f),
            Triple(0.12f, 0.85f, 0.3f),
            Triple(0.88f, 0.90f, 0.6f)
        )

        particles.forEachIndexed { i, p ->
            val drift = if (i % 2 == 0) drifts.first else drifts.second
            val currentY = (p.second - (drift * 0.3f) + 1f) % 1f * h
            val currentX = p.first * w
            val alpha = (1f - ((currentY / h) - 0.5f).let { kotlin.math.abs(it) * 2f }).coerceIn(0.1f, 0.6f)

            drawCircle(
                color = TributeGlow.copy(alpha = if (isAnimating) alpha else 0.2f),
                radius = 2.dp.toPx() * p.third,
                center = Offset(currentX, currentY)
            )
        }
    }
}
