package com.gymcompanion.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gymcompanion.app.presentation.theme.*
import kotlinx.coroutines.delay

// ══════════════════════════════════════════════════════════════════
// ✨ GYM COMPANION — ANIMATION UTILITIES
// Reusable animations for a premium, responsive feel
// ══════════════════════════════════════════════════════════════════

// ── Entrance Animations ─────────────────────────────────────────

/**
 * Staggered slide-up + fade-in entry for list items.
 * Use in LazyColumn items: item { AnimatedEntrance(index = index) { ... } }
 */
@Composable
fun AnimatedEntrance(
    index: Int = 0,
    delayPerItem: Long = 50L,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayPerItem * index)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 0,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        content()
    }
}

/**
 * Fade-in + slight scale animation for hero content
 */
@Composable
fun FadeInScale(
    delayMs: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, easing = FastOutSlowInEasing)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
    ) {
        content()
    }
}

// ── Shimmer / Skeleton Loading ──────────────────────────────────

/**
 * Shimmer loading effect modifier.
 * Apply to any composable: Modifier.shimmerEffect()
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                DarkSurfaceElevated,
                DarkSurfaceHighest,
                DarkSurfaceElevated
            ),
            start = Offset(translateX, 0f),
            end = Offset(translateX + 300f, 0f)
        )
    )
}

/**
 * Skeleton loading placeholder box
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Int = 20
) {
    Box(
        modifier = modifier
            .height(height.dp)
            .clip(RoundedCornerShape(8.dp))
            .shimmerEffect()
    )
}

// ── Number Count-Up Animation ───────────────────────────────────

/**
 * Animates a number from 0 to target with easing.
 * Returns the current animated value.
 */
@Composable
fun animateIntAsState(targetValue: Int, durationMs: Int = 800): Int {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(
                durationMillis = durationMs,
                easing = FastOutSlowInEasing
            )
        )
    }

    return animatable.value.toInt()
}

@Composable
fun animateFloatCountUp(targetValue: Float, durationMs: Int = 800): Float {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue,
            animationSpec = tween(
                durationMillis = durationMs,
                easing = FastOutSlowInEasing
            )
        )
    }

    return animatable.value
}

// ── Pulse / Glow Animation ──────────────────────────────────────

/**
 * Returns an animated alpha value for a soft pulsing glow effect.
 * Typically used with neon-colored borders or shadows.
 */
@Composable
fun rememberPulseAlpha(
    minAlpha: Float = 0.4f,
    maxAlpha: Float = 1f,
    durationMs: Int = 1500
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    return alpha
}

/**
 * Returns a pulsing scale value for subtle breathing effect
 */
@Composable
fun rememberPulseScale(
    minScale: Float = 1f,
    maxScale: Float = 1.05f,
    durationMs: Int = 1500
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseScale")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    return scale
}

// ── Animated Progress ───────────────────────────────────────────

/**
 * Animated progress from 0 to target (0f..1f)
 */
@Composable
fun animateProgress(targetProgress: Float, durationMs: Int = 1000): Float {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetProgress) {
        animatable.animateTo(
            targetValue = targetProgress.coerceIn(0f, 1f),
            animationSpec = tween(
                durationMillis = durationMs,
                easing = FastOutSlowInEasing
            )
        )
    }

    return animatable.value
}

// ── Gradient Brush Helpers ──────────────────────────────────────

fun neonGradientBrush(
    colors: Pair<Color, Color> = GradientPrimary
): Brush = Brush.linearGradient(
    colors = listOf(colors.first, colors.second)
)

fun neonRadialBrush(
    colors: Pair<Color, Color> = GradientPrimary
): Brush = Brush.radialGradient(
    colors = listOf(colors.first, colors.second)
)

fun shimmerGradientBrush(): Brush = Brush.linearGradient(
    colors = listOf(
        DarkSurfaceElevated,
        DarkSurfaceHighest.copy(alpha = 0.6f),
        DarkSurfaceElevated
    )
)
