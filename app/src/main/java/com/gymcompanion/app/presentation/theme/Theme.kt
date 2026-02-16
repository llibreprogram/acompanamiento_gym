package com.gymcompanion.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ══════════════════════════════════════════════════════════════════
// 🌙 PREMIUM DARK THEME — Always dark for gym context
// ══════════════════════════════════════════════════════════════════

private val GymDarkColorScheme = darkColorScheme(
    // Primary — Neon Blue
    primary = NeonBlue,
    onPrimary = Color(0xFF003344),
    primaryContainer = Color(0xFF004D66),
    onPrimaryContainer = NeonBlueLight,

    // Secondary — Neon Purple
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2D1B69),
    onSecondaryContainer = NeonPurpleLight,

    // Tertiary — Neon Green
    tertiary = NeonGreen,
    onTertiary = Color(0xFF003300),
    tertiaryContainer = Color(0xFF004D00),
    onTertiaryContainer = Color(0xFF66FF99),

    // Backgrounds — Deep dark layers
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,

    // Errors
    error = StatusError,
    onError = Color.White,
    errorContainer = Color(0xFF3D0D0D),
    onErrorContainer = Color(0xFFFFB4AB),

    // Outline / borders
    outline = DarkSurfaceBorder,
    outlineVariant = Color(0xFF1E1E2E),

    // Inverse (for snackbars etc.)
    inverseSurface = Color(0xFFE2E2E9),
    inverseOnSurface = Color(0xFF1A1A26),
    inversePrimary = NeonBlueDark,

    // Scrim
    scrim = Color(0xCC000000)
)

// Modern rounded shapes for premium feel
val GymShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun GymCompanionTheme(
    darkTheme: Boolean = true, // Always dark for gym context
    dynamicColor: Boolean = false, // Disabled — we use our custom neon palette
    content: @Composable () -> Unit
) {
    val colorScheme = GymDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            // Transparent status bar for edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GymTypography,
        shapes = GymShapes,
        content = content
    )
}
