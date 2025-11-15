package com.gymcompanion.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GymPrimaryLight,
    secondary = GymSecondary,
    tertiary = GymAccentLight,
    background = androidx.compose.ui.graphics.Color(0xFF0F172A), // Slate-900
    surface = androidx.compose.ui.graphics.Color(0xFF1E293B), // Slate-800
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF334155), // Slate-700
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color(0xFFF1F5F9), // Slate-100
    onSurface = androidx.compose.ui.graphics.Color(0xFFF8FAFC), // Slate-50
    error = GymError,
    onError = androidx.compose.ui.graphics.Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GymPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = GymPrimaryLight,
    onPrimaryContainer = GymPrimaryDark,
    secondary = GymSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Color(0xFFEDE9FE), // Violet-100
    onSecondaryContainer = GymPrimaryDark,
    tertiary = GymAccent,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = GymAccentLight,
    onTertiaryContainer = GymPrimaryDark,
    background = GymBackground,
    onBackground = GymTextPrimary,
    surface = GymSurface,
    onSurface = GymTextPrimary,
    surfaceVariant = GymSurfaceVariant,
    onSurfaceVariant = GymTextSecondary,
    error = GymError,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = Color(0xFFFEE2E2), // Red-100
    onErrorContainer = Color(0xFF991B1B), // Red-800
    outline = GymTextTertiary,
    outlineVariant = Color(0xFFE2E8F0) // Slate-200
)

@Composable
fun GymCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
