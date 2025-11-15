package com.gymcompanion.app.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import com.gymcompanion.app.presentation.components.*
import com.gymcompanion.app.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de inicio / Home
 * Muestra rutina del día, estadísticas rápidas y acceso a funciones principales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onStartWorkout: (Long) -> Unit = {},
    onNavigateToRoutines: () -> Unit = {}
) {
    val todayRoutines by viewModel.todayRoutines.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val lastSession by viewModel.lastSession.collectAsState()
    
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
        topBar = {
            SmallTopAppBar(
                title = { 
                    Text(
                        "Gym Companion",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome message with animation
            item {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600)) + slideInVertically { -it/2 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = getGreeting(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Hoy es ${getCurrentDate()}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Motivational quote card
            item {
                ModernCard(
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    ),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = todayQuote,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Quick stats with modern cards
            item {
                QuickStatsModernSection(
                    weeklyStats = weeklyStats,
                    viewModel = viewModel
                )
            }
            
            // Quick actions
            item {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Nueva Rutina",
                        subtitle = "Crea una rutina personalizada",
                        icon = Icons.Default.Add,
                        onClick = onNavigateToRoutines,
                        modifier = Modifier.weight(1f),
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                    QuickActionCard(
                        title = "Estadísticas",
                        subtitle = "Ve tu progreso",
                        icon = Icons.Default.BarChart,
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f),
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
                }
            }
            
            // Today's routines
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏋️ Rutinas de Hoy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    ModernBadge(
                        text = "${todayRoutines.size}",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            if (todayRoutines.isEmpty()) {
                item {
                    TodayRoutineEmptyModernCard(onCreateRoutine = onNavigateToRoutines)
                }
            } else {
                items(todayRoutines) { routine ->
                    TodayRoutineModernCard(
                        routine = routine,
                        onStartWorkout = { onStartWorkout(routine.routine.id) }
                    )
                }
            }
            
            // Last session
            lastSession?.let { session ->
                item {
                    ModernDivider()
                }
                item {
                    Text(
                        text = "📊 Último Entrenamiento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    LastSessionModernCard(
                        session = session,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

/**
 * Obtiene saludo según hora del día
 */
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Buenos días 🌅"
        in 12..17 -> "Buenas tardes ☀️"
        in 18..21 -> "Buenas noches 🌆"
        else -> "Buenas noches 🌙"
    }
}

/**
 * Obtiene fecha actual formateada
 */
private fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    return sdf.format(Date()).capitalize(Locale.getDefault())
}

@Composable
fun TodayRoutineModernCard(
    routine: RoutineWithExercises,
    onStartWorkout: () -> Unit
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            )
        ),
        elevation = 4.dp,
        onClick = onStartWorkout,
        animateOnClick = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.routine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!routine.routine.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = routine.routine.description ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernBadge(
                    text = "${routine.routineExercises.size} ejercicios",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                ModernBadge(
                    text = "Día ${routine.routine.dayOfWeek}",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            ModernButton(
                onClick = onStartWorkout,
                text = "Comenzar Entrenamiento",
                icon = Icons.Default.PlayArrow,
                gradient = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TodayRoutineEmptyModernCard(onCreateRoutine: () -> Unit) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "No hay rutinas para hoy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crea una rutina personalizada o usa nuestro generador con IA",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            ModernButton(
                onClick = onCreateRoutine,
                text = "Crear Primera Rutina",
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun QuickStatsModernSection(
    weeklyStats: WeeklyStats,
    viewModel: HomeViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "📈 Esta Semana",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Entrenamientos",
                value = weeklyStats.totalWorkouts.toString(),
                subtitle = "sesiones",
                icon = Icons.Default.FitnessCenter,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                iconBackgroundColor = MaterialTheme.colorScheme.primary,
                iconColor = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Tiempo Total",
                value = viewModel.formatTime(weeklyStats.totalTime),
                subtitle = "entrenando",
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                iconBackgroundColor = MaterialTheme.colorScheme.secondary,
                iconColor = MaterialTheme.colorScheme.onSecondary
            )
            StatCard(
                title = "Volumen",
                value = viewModel.formatVolume(weeklyStats.totalVolume),
                subtitle = "levantado",
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                iconBackgroundColor = MaterialTheme.colorScheme.tertiary,
                iconColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}



@Composable
fun LastSessionModernCard(
    session: com.gymcompanion.app.data.local.entity.WorkoutSessionEntity,
    viewModel: HomeViewModel
) {
    val duration = if (session.endTime != null) {
        (session.endTime!! - session.startTime) / 1000
    } else 0L
    
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        elevation = 3.dp
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
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(session.startTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernCard(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    elevation = 1.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatTime(duration),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Duración",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                ModernCard(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    elevation = 1.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatVolume(session.totalVolume),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Volumen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
