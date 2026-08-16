package com.amairatech.zubeenfm.ui.tribute

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.sin

/**
 * Memorial Flower Petal Data Model for realistic physics.
 */
private data class PetalPhysics(
    val id: Int,
    val startXFraction: Float,       // Horizontal start anchor (0.15f .. 0.85f)
    val startYFraction: Float,       // Vertical start anchor (0.05f .. 0.35f)
    val targetYOffsetDp: Float,      // Total fall distance
    val sizeDp: Float,               // Size of petal
    val baseColor: Color,            // Memorial petal color
    val accentColor: Color,          // Soft gradient shade
    val startRotationDeg: Float,     // Initial angle
    val rotationSweepDeg: Float,     // Total rotation during descent
    val horizontalDriftDp: Float,    // Max sway distance
    val swayFrequency: Float,        // Sinusoidal cycles
    val durationFraction: Float,     // Speed factor (0.8f .. 1.2f)
    val delayFraction: Float         // Staggered launch delay (0.0f .. 0.25f)
)

/**
 * Curated Memorial Rose Petal Palette: Adjusted slightly for purple background contrast.
 */
private val PetalPalette = listOf(
    Pair(Color(0xFFF8BBD0), Color(0xFFD81B60)), // Pink to Velvet
    Pair(Color(0xFFFCE4EC), Color(0xFFF48FB1)), // Blush Rose
    Pair(Color(0xFFE1BEE7), Color(0xFFAB47BC)), // Light Violet to Purple
    Pair(Color(0xFFFF80AB), Color(0xFFC2185B)), // Coral Rose
    Pair(Color(0xFFF3E5F5), Color(0xFFBA68C8)), // Purple mist
    Pair(Color(0xFFFFF0F5), Color(0xFFFFB6C1))  // Misty Rose
)

/**
 * Reusable Compose component rendering an elegant memorial shower of falling rose petals.
 * Battery-conscious: Driven by a single Animatable with auto-terminating lifecycle.
 */
@Composable
fun TributePetalEffect(
    burstTriggerId: Long,
    isScreenVisible: Boolean,
    modifier: Modifier = Modifier,
    petalCount: Int = 22
) {
    if (burstTriggerId <= 0L || !isScreenVisible) return

    val progressAnim = remember(burstTriggerId) { Animatable(0f) }

    val petals = remember(burstTriggerId) {
        generatePetals(petalCount, burstTriggerId)
    }

    LaunchedEffect(burstTriggerId, isScreenVisible) {
        if (isScreenVisible && burstTriggerId > 0L) {
            progressAnim.snapTo(0f)
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 3200,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    if (progressAnim.value >= 1f) return

    val animProgress = progressAnim.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (petal in petals) {
            // Apply delay and normalized progress for this specific petal
            val effectiveProgress = ((animProgress - petal.delayFraction) / petal.durationFraction).coerceIn(0f, 1f)
            if (effectiveProgress <= 0f) continue

            // 1. Vertical position (falls downward with smooth easing)
            val startY = petal.startYFraction * canvasHeight
            val totalFall = canvasHeight * 0.75f + (petal.targetYOffsetDp * density)
            val currentY = startY + (effectiveProgress * totalFall)

            // 2. Horizontal position with gentle sinusoidal swaying
            val startX = petal.startXFraction * canvasWidth
            val swayOffset = sin(effectiveProgress * petal.swayFrequency * 2 * PI.toFloat()) * (petal.horizontalDriftDp * density)
            val currentX = startX + swayOffset

            // 3. Rotation (slow natural tumbling)
            val currentRotation = petal.startRotationDeg + (effectiveProgress * petal.rotationSweepDeg)

            // 4. Alpha transparency curve (fade in quickly, float, then fade out gracefully)
            val alpha = when {
                effectiveProgress < 0.15f -> (effectiveProgress / 0.15f) * 0.9f
                effectiveProgress > 0.70f -> ((1f - effectiveProgress) / 0.30f) * 0.9f
                else -> 0.9f
            }.coerceIn(0f, 1f)

            if (alpha > 0.01f) {
                val petalSizePx = petal.sizeDp * density
                drawPetal(
                    center = Offset(currentX, currentY),
                    sizePx = petalSizePx,
                    rotationDeg = currentRotation,
                    baseColor = petal.baseColor.copy(alpha = alpha),
                    accentColor = petal.accentColor.copy(alpha = alpha * 0.85f)
                )
            }
        }
    }
}

/**
 * Draws a single delicate flower petal shape with soft gradient shading.
 */
private fun DrawScope.drawPetal(
    center: Offset,
    sizePx: Float,
    rotationDeg: Float,
    baseColor: Color,
    accentColor: Color
) {
    rotate(degrees = rotationDeg, pivot = center) {
        val w = sizePx * 0.55f
        val h = sizePx

        // Natural curved organic petal path
        val path = Path().apply {
            moveTo(center.x, center.y - h * 0.5f) // Top tip
            cubicTo(
                center.x + w * 0.9f, center.y - h * 0.2f,
                center.x + w * 0.8f, center.y + h * 0.3f,
                center.x, center.y + h * 0.5f // Bottom base
            )
            cubicTo(
                center.x - w * 0.8f, center.y + h * 0.3f,
                center.x - w * 0.9f, center.y - h * 0.2f,
                center.x, center.y - h * 0.5f // Back to top
            )
            close()
        }

        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(baseColor, accentColor, accentColor.copy(alpha = 0.4f)),
                center = center,
                radius = sizePx * 0.7f
            ),
            style = Fill
        )
    }
}

/**
 * Generates deterministic yet organic petal physics parameters for a given burst.
 */
private fun generatePetals(count: Int, seed: Long): List<PetalPhysics> {
    val random = java.util.Random(seed)
    return List(count) { index ->
        val palette = PetalPalette[index % PetalPalette.size]
        PetalPhysics(
            id = index,
            startXFraction = 0.20f + (random.nextFloat() * 0.60f),
            startYFraction = 0.08f + (random.nextFloat() * 0.22f),
            targetYOffsetDp = 160f + (random.nextFloat() * 200f),
            sizeDp = 13f + (random.nextFloat() * 10f),
            baseColor = palette.first,
            accentColor = palette.second,
            startRotationDeg = -45f + (random.nextFloat() * 90f),
            rotationSweepDeg = -120f + (random.nextFloat() * 240f),
            horizontalDriftDp = 20f + (random.nextFloat() * 35f),
            swayFrequency = 1.2f + (random.nextFloat() * 1.6f),
            durationFraction = 0.75f + (random.nextFloat() * 0.25f),
            delayFraction = random.nextFloat() * 0.20f
        )
    }
}
