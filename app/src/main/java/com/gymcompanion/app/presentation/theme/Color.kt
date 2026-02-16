package com.gymcompanion.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════
// 🎨 GYM COMPANION — PREMIUM DARK NEON COLOR SYSTEM
// Inspired by Hevy, Strong, and modern fitness UI trends
// ══════════════════════════════════════════════════════════════════

// ── Primary Neon Palette ────────────────────────────────────────
val NeonBlue = Color(0xFF00D4FF)          // Electric cyan-blue — primary CTA
val NeonBlueDark = Color(0xFF00A8CC)      // Deeper for pressed states
val NeonBlueLight = Color(0xFF66E3FF)     // Light variant for highlights
val NeonGreen = Color(0xFF00FF87)         // Success, achievement glow
val NeonPink = Color(0xFFFF006E)          // Hot accent — alerts, PRs
val NeonPurple = Color(0xFF8B5CF6)        // Violet — secondary elements
val NeonPurpleLight = Color(0xFFA78BFA)   // Light violet highlights
val NeonOrange = Color(0xFFFF6B35)        // Warm accent — streaks, fire

// ── Dark Surface Hierarchy ──────────────────────────────────────
// Deep layered surfaces for glassmorphism depth
val DarkBackground = Color(0xFF0A0A0F)     // Deepest — nearly black with cool tint
val DarkSurface = Color(0xFF12121A)        // Card backgrounds
val DarkSurfaceElevated = Color(0xFF1A1A26)// Elevated cards
val DarkSurfaceHighest = Color(0xFF222233) // Dialogs, dropdowns
val DarkSurfaceBorder = Color(0xFF2A2A3E)  // Subtle borders

// ── Glassmorphism Colors ────────────────────────────────────────
val GlassWhite = Color(0x1AFFFFFF)         // 10% white overlay
val GlassWhiteHover = Color(0x26FFFFFF)    // 15% white on hover
val GlassBorder = Color(0x33FFFFFF)        // 20% white border
val GlassBorderSubtle = Color(0x1AFFFFFF)  // 10% subtle border

// ── Text Hierarchy ──────────────────────────────────────────────
val TextPrimary = Color(0xFFF8FAFC)        // Pure white — titles
val TextSecondary = Color(0xFFB0B8C8)      // Soft blue-gray — body
val TextTertiary = Color(0xFF6B7280)       // Muted — captions, hints
val TextDisabled = Color(0xFF4A4A5A)       // Ghosted

// ── Status Colors (Neon-tinted) ─────────────────────────────────
val StatusSuccess = Color(0xFF00FF87)       // Neon green
val StatusSuccessDim = Color(0xFF059669)    // Subdued green
val StatusError = Color(0xFFFF4757)         // Bright red
val StatusErrorDim = Color(0xFFDC2626)      // Subdued red
val StatusWarning = Color(0xFFFFA726)       // Warm amber
val StatusWarningDim = Color(0xFFD97706)    // Subdued amber
val StatusInfo = Color(0xFF00D4FF)          // Neon blue

// ── Muscle Group Colors (Neon vibrant) ──────────────────────────
val ChestColor = Color(0xFFFF4757)          // Hot red
val BackColor = Color(0xFF00D4FF)           // Electric blue
val LegsColor = Color(0xFF00FF87)           // Neon green
val ShouldersColor = Color(0xFFFFA726)      // Amber
val ArmsColor = Color(0xFF8B5CF6)           // Violet
val CoreColor = Color(0xFFFFD93D)           // Yellow
val FullBodyColor = Color(0xFF00D4FF)       // Cyan
val CardioColor = Color(0xFFFF006E)         // Hot pink

// ── Chart / Graph Colors ────────────────────────────────────────
val ChartPrimary = Color(0xFF00D4FF)
val ChartSecondary = Color(0xFF8B5CF6)
val ChartTertiary = Color(0xFF00FF87)
val ChartBackground = Color(0xFF12121A)

// ── Gradient Pairs (for Brush.linearGradient) ───────────────────
// Usage: Brush.linearGradient(listOf(GradientPrimary.first, GradientPrimary.second))
val GradientPrimary = Pair(Color(0xFF00D4FF), Color(0xFF8B5CF6))    // Blue → Purple
val GradientSuccess = Pair(Color(0xFF00FF87), Color(0xFF00D4FF))    // Green → Blue
val GradientFire = Pair(Color(0xFFFF6B35), Color(0xFFFF006E))       // Orange → Pink
val GradientPurple = Pair(Color(0xFF8B5CF6), Color(0xFFFF006E))     // Purple → Pink
val GradientDark = Pair(Color(0xFF1A1A26), Color(0xFF12121A))       // Elevated → Base

// ── Training Phase Colors ───────────────────────────────────────
val PhaseStrength = Color(0xFFFF4757)       // Red — power
val PhaseHypertrophy = Color(0xFF8B5CF6)    // Purple — growth
val PhaseEndurance = Color(0xFF00D4FF)      // Blue — stamina
val PhaseDeload = Color(0xFF00FF87)         // Green — recovery

// ── Legacy compat (kept for files that reference old names) ─────
val GymPrimary = NeonBlue
val GymPrimaryDark = NeonBlueDark
val GymPrimaryLight = NeonBlueLight
val GymSecondary = NeonPurple
val GymAccent = NeonGreen
val GymAccentLight = NeonBlueLight
val GymSuccess = StatusSuccess
val GymError = StatusError
val GymWarning = StatusWarning
val GymInfo = StatusInfo
val GymBackground = DarkBackground
val GymSurface = DarkSurface
val GymSurfaceVariant = DarkSurfaceElevated
val GymTextPrimary = TextPrimary
val GymTextSecondary = TextSecondary
val GymTextTertiary = TextTertiary
val Purple80 = NeonPurpleLight
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = NeonPurple
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
