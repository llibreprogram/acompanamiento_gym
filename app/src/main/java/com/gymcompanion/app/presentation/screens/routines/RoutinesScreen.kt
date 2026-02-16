package com.gymcompanion.app.presentation.screens.routines

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import com.gymcompanion.app.presentation.components.*
import com.gymcompanion.app.presentation.theme.*

/**
 * 📋 ROUTINES SCREEN — Premium Dark Neon Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel = hiltViewModel(),
    onRoutineClick: (Long) -> Unit = {},
    onCreateRoutine: () -> Unit = {},
    onViewDetails: (Long) -> Unit = {}
) {
    val allRoutines by viewModel.allRoutines.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<RoutineWithExercises?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Rutinas",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = DarkSurface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoutine,
                containerColor = NeonBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear rutina")
            }
        }
    ) { paddingValues ->
        when {
            allRoutines.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Neon icon
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(24.dp))
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
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Text(
                            text = "No tienes rutinas creadas aún",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toca el botón + para crear tu primera rutina",
                            textAlign = TextAlign.Center,
                            color = TextSecondary
                        )

                        PulsingWorkoutButton(
                            text = "Crear Rutina",
                            onClick = onCreateRoutine,
                            icon = Icons.Default.Add,
                            gradientColors = GradientPrimary
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(allRoutines) { index, routineWithExercises ->
                        AnimatedEntrance(index = index) {
                            RoutineCard(
                                routineWithExercises = routineWithExercises,
                                onClick = { onRoutineClick(routineWithExercises.routine.id) },
                                onViewDetails = { onViewDetails(routineWithExercises.routine.id) },
                                onDelete = {
                                    routineToDelete = routineWithExercises
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog — neon styled
        if (showDeleteDialog && routineToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = DarkSurfaceElevated,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary,
                title = { Text("Eliminar rutina") },
                text = { Text("¿Estás seguro de que quieres eliminar esta rutina?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            routineToDelete?.let { viewModel.deleteRoutine(it) }
                            showDeleteDialog = false
                            routineToDelete = null
                        }
                    ) {
                        Text("Eliminar", color = NeonPink)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}

/**
 * Routine card with glassmorphic style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCard(
    routineWithExercises: RoutineWithExercises,
    onClick: () -> Unit,
    onViewDetails: () -> Unit,
    onDelete: () -> Unit
) {
    val routine = routineWithExercises.routine
    val exercises = routineWithExercises.routineExercises

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (!routine.description.isNullOrBlank()) {
                        Text(
                            text = routine.description ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar rutina",
                        tint = NeonPink.copy(alpha = 0.7f)
                    )
                }
            }

            // Days badge
            if (routine.daysOfWeek.isNotBlank()) {
                Text(
                    text = "Días: ${routine.daysOfWeek}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonBlue
                )
            }

            // Exercise count + active badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernBadge(
                    text = "${exercises.size} ejercicios",
                    containerColor = NeonBlue.copy(alpha = 0.12f),
                    contentColor = NeonBlue
                )

                if (routine.isActive) {
                    ModernBadge(
                        text = "Activa",
                        containerColor = NeonGreen.copy(alpha = 0.12f),
                        contentColor = NeonGreen
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(NeonBlue.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.2f))
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver", fontSize = 13.sp)
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeonBlue, NeonPurple)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Iniciar", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
