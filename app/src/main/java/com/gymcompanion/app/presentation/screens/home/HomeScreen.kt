package com.gymcompanion.app.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import com.gymcompanion.app.presentation.components.*
import com.gymcompanion.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🏠 HOME SCREEN — Premium Dark Neon Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartWorkout: (Long) -> Unit = {},
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {}
) {
    val userName by viewModel.userName.collectAsState()
    val todayRoutines by viewModel.todayRoutines.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val lastSession by viewModel.lastSession.collectAsState()
    val dailyWorkoutData by viewModel.dailyWorkoutData.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val overtrainingRisk by viewModel.overtrainingRisk.collectAsState()
    val sessionPrediction by viewModel.sessionPrediction.collectAsState()
    val healthData by viewModel.healthData.collectAsState()
    val healthConnectAvailable by viewModel.healthConnectAvailable.collectAsState()

    // Health Connect permissions
    val requestPermissions = rememberHealthConnectPermissionsLauncher(
        onPermissionsGranted = {
            viewModel.loadHealthData()
        }
    )

    // Motivational quotes
    val quotes = remember {
        listOf(
            "\"La única manera de hacer un gran trabajo es amar lo que haces.\" - Steve Jobs",
            "\"El dolor que sientes hoy será la fuerza que sientes mañana.\"",
            "\"Tu cuerpo puede resistir casi cualquier cosa. Es tu mente la que debes convencer.\"",
            "\"No se trata de tener tiempo. Se trata de hacer tiempo.\"",
            "\"La disciplina es hacer lo que tiene que hacerse, cuando tiene que hacerse.\"",
            "\"El éxito es la suma de pequeños esfuerzos repetidos día tras día.\"",
            "\"No cuentes los días, haz que los días cuenten.\" - Muhammad Ali"
        )
    }
    val todayQuote = remember { quotes.random() }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            SmallTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Neon dot indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                        )
                        Text(
                            "GYM COMPANION",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = DarkSurface.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Hero Welcome Section ────────────────────────
                item {
                    AnimatedEntrance(index = 0) {
                        HeroWelcomeSection(
                            name = userName ?: "",
                            quote = todayQuote
                        )
                    }
                }

                // ── Smart Recommendation ────────────────────────
                item {
                    AnimatedEntrance(index = 1) {
                        SmartRecommendationCard(
                            overtrainingRisk = overtrainingRisk,
                            sessionPrediction = sessionPrediction,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ── Weekly Stats Rings ──────────────────────────
                item {
                    AnimatedEntrance(index = 2) {
                        NeonStatsSection(
                            weeklyStats = weeklyStats,
                            viewModel = viewModel
                        )
                    }
                }

                // ── Health Connect Data ─────────────────────────
                if (healthConnectAvailable) {
                    item {
                        AnimatedEntrance(index = 3) {
                            HealthDataSection(
                                steps = healthData?.steps?.count,
                                weight = healthData?.weight?.weightKg,
                                heartRate = healthData?.heartRate?.beatsPerMinute,
                                onRequestPermissions = requestPermissions,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── Quick Action Grid ───────────────────────────
                item {
                    AnimatedEntrance(index = 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionCard(
                                title = "Nueva Rutina",
                                subtitle = "IA personalizada",
                                icon = Icons.Default.Add,
                                onClick = onNavigateToRoutines,
                                modifier = Modifier.weight(1f),
                                gradient = Brush.linearGradient(
                                    colors = listOf(NeonBlue, NeonPurple)
                                )
                            )
                            QuickActionCard(
                                title = "Estadísticas",
                                subtitle = "Tu progreso",
                                icon = Icons.Default.BarChart,
                                onClick = onNavigateToAnalytics,
                                modifier = Modifier.weight(1f),
                                gradient = Brush.linearGradient(
                                    colors = listOf(NeonPurple, NeonPink)
                                )
                            )
                        }
                    }
                }

                // ── Today's Routines ────────────────────────────
                item {
                    AnimatedEntrance(index = 5) {
                        SectionHeader(
                            title = "🏋️ Rutinas de Hoy",
                            action = "${todayRoutines.size}",
                            onAction = onNavigateToRoutines
                        )
                    }
                }

                if (todayRoutines.isEmpty()) {
                    item {
                        AnimatedEntrance(index = 6) {
                            EmptyRoutineNeonCard(onCreateRoutine = onNavigateToRoutines)
                        }
                    }
                } else {
                    itemsIndexed(todayRoutines) { idx, routine ->
                        AnimatedEntrance(index = 6 + idx) {
                            TodayRoutineNeonCard(
                                routine = routine,
                                onStartWorkout = { onStartWorkout(routine.routine.id) }
                            )
                        }
                    }
                }

                // ── Last Session ────────────────────────────────
                lastSession?.let { session ->
                    item {
                        AnimatedEntrance(index = 7) {
                            SectionHeader(title = "📊 Último Entrenamiento")
                        }
                    }
                    item {
                        AnimatedEntrance(index = 8) {
                            LastSessionNeonCard(
                                session = session,
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // ── Weekly Chart ────────────────────────────────
                item {
                    AnimatedEntrance(index = 9) {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InteractiveChart(
                                dataPoints = dailyWorkoutData.ifEmpty {
                                    listOf(
                                        "Lun" to 0f, "Mar" to 0f, "Mié" to 0f,
                                        "Jue" to 0f, "Vie" to 0f, "Sáb" to 0f, "Dom" to 0f
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── Achievements ────────────────────────────────
                item {
                    AnimatedEntrance(index = 10) {
                        SectionHeader(title = "🏆 Logros")
                    }
                }

                item {
                    AnimatedEntrance(index = 11) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AchievementCard(
                                title = "Primera Semana",
                                description = "Completaste tu primera semana de entrenamientos",
                                icon = Icons.Default.EmojiEvents,
                                achieved = weeklyStats.totalWorkouts >= 3,
                                modifier = Modifier.fillMaxWidth()
                            )

                            AchievementCard(
                                title = "Racha de 5 Días",
                                description = "Entrenaste 5 días seguidos",
                                icon = Icons.Default.LocalFireDepartment,
                                achieved = weeklyStats.totalWorkouts >= 5,
                                modifier = Modifier.fillMaxWidth()
                            )

                            AchievementCard(
                                title = "Volumen Total",
                                description = "Levantaste más de 10,000 lbs esta semana",
                                icon = Icons.Default.TrendingUp,
                                achieved = weeklyStats.totalVolume >= 10000,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── Streak Calendar ─────────────────────────────
                item {
                    AnimatedEntrance(index = 12) {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            WorkoutStreakCalendar(
                                streakDays = listOf(true, true, false, true, true, false, false),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── Motivation ──────────────────────────────────
                item {
                    AnimatedEntrance(index = 13) {
                        MotivationCard(
                            tip = "La consistencia vence al talento cuando el talento no es consistente.",
                            author = "Anónimo",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Pull-to-refresh indicator
            AnimatedVisibility(
                visible = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NeonBlue,
                    trackColor = DarkSurface
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ══════════════════════════════════════════════════════════════════

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Buenos días 🌅"
        in 12..17 -> "Buenas tardes ☀️"
        in 18..21 -> "Buenas noches 🌆"
        else -> "Buenas noches 🌙"
    }
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    return sdf.format(Date()).capitalize(Locale.getDefault())
}

// ══════════════════════════════════════════════════════════════════
// SUB-COMPOSABLES — Premium Neon Design
// ══════════════════════════════════════════════════════════════════

/**
 * Hero welcome with gradient text and animated greeting
 */
@Composable
fun HeroWelcomeSection(name: String, quote: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = getGreeting() + (if (name.isNotBlank()) ", $name" else ""),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = getCurrentDate(),
            style = MaterialTheme.typography.bodyLarge,
            color = NeonBlue
        )
        if (quote.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = quote,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

/**
 * Weekly stats with neon progress rings
 */
@Composable
fun NeonStatsSection(
    weeklyStats: WeeklyStats,
    viewModel: HomeViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "📈 Esta Semana")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Workouts ring
            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NeonProgressRing(
                        progress = (weeklyStats.totalWorkouts / 7f).coerceAtMost(1f),
                        size = 72.dp,
                        strokeWidth = 6.dp,
                        gradientColors = GradientPrimary
                    ) {
                        Text(
                            text = "${weeklyStats.totalWorkouts}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sesiones",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Time
            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NeonProgressRing(
                        progress = (weeklyStats.totalTime / 7200f).coerceAtMost(1f), // 2h target
                        size = 72.dp,
                        strokeWidth = 6.dp,
                        gradientColors = GradientSuccess
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.formatTime(weeklyStats.totalTime),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tiempo",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Volume
            GlassmorphicCard(modifier = Modifier.weight(1f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NeonProgressRing(
                        progress = (weeklyStats.totalVolume.toFloat() / 50000f).coerceAtMost(1f),
                        size = 72.dp,
                        strokeWidth = 6.dp,
                        gradientColors = GradientPurple
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.formatVolume(weeklyStats.totalVolume),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Volumen",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Today's routine card with neon gradient border and pulsing CTA
 */
@Composable
fun TodayRoutineNeonCard(
    routine: RoutineWithExercises,
    onStartWorkout: () -> Unit
) {
    NeonGradientCard(
        modifier = Modifier.fillMaxWidth(),
        gradientColors = GradientPrimary,
        onClick = onStartWorkout
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.routine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (!routine.routine.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = routine.routine.description ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Neon icon badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonBlue, NeonPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Exercise count + badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernBadge(
                    text = "${routine.routineExercises.size} ejercicios",
                    containerColor = NeonBlue.copy(alpha = 0.15f),
                    contentColor = NeonBlue
                )
            }

            // Pulsing start button
            PulsingWorkoutButton(
                text = "Comenzar Entrenamiento",
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth(),
                gradientColors = GradientPrimary
            )
        }
    }
}

/**
 * Empty routine card — no routines for today
 */
@Composable
fun EmptyRoutineNeonCard(onCreateRoutine: () -> Unit) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Neon icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NeonBlue.copy(alpha = 0.15f),
                                NeonPurple.copy(alpha = 0.15f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = NeonBlue
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "No hay rutinas para hoy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Crea una rutina personalizada o usa nuestro generador con IA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            PulsingWorkoutButton(
                text = "Crear Primera Rutina",
                onClick = onCreateRoutine,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Add,
                gradientColors = GradientSuccess
            )
        }
    }
}

/**
 * Last workout session card with glassmorphic sub-stats
 */
@Composable
fun LastSessionNeonCard(
    session: com.gymcompanion.app.data.local.entity.WorkoutSessionEntity,
    viewModel: HomeViewModel
) {
    val duration = if (session.endTime != null) {
        (session.endTime!! - session.startTime) / 1000
    } else 0L

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Última Sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Duration stat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatTime(duration),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Duración",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }

                // Volume stat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatVolume(session.totalVolume),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Volumen",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

// Legacy aliases for backward compat
@Composable
fun TodayRoutineModernCard(routine: RoutineWithExercises, onStartWorkout: () -> Unit) =
    TodayRoutineNeonCard(routine, onStartWorkout)

@Composable
fun TodayRoutineEmptyModernCard(onCreateRoutine: () -> Unit) =
    EmptyRoutineNeonCard(onCreateRoutine)

@Composable
fun QuickStatsModernSection(weeklyStats: WeeklyStats, viewModel: HomeViewModel) =
    NeonStatsSection(weeklyStats, viewModel)

@Composable
fun LastSessionModernCard(
    session: com.gymcompanion.app.data.local.entity.WorkoutSessionEntity,
    viewModel: HomeViewModel
) = LastSessionNeonCard(session, viewModel)

@Composable
fun WelcomeSection(name: String, quote: String) =
    HeroWelcomeSection(name, quote)
