package com.gymcompanion.app.presentation.screens.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.RoutineWithExercises

/**
 * Pantalla de rutinas
 * Lista y gestiona las rutinas de entrenamiento del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel = hiltViewModel(),
    onRoutineClick: (Long) -> Unit = {},
    onCreateRoutine: () -> Unit = {}
) {
    val allRoutines by viewModel.allRoutines.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<RoutineWithExercises?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Rutinas") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoutine
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No tienes rutinas creadas aún",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Toca el botón + para crear tu primera rutina",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    items(allRoutines) { routineWithExercises ->
                        RoutineCard(
                            routineWithExercises = routineWithExercises,
                            onClick = { onRoutineClick(routineWithExercises.routine.id) },
                            onDelete = {
                                routineToDelete = routineWithExercises
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
        
        // Diálogo de confirmación para eliminar
        if (showDeleteDialog && routineToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
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
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Card de rutina individual
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCard(
    routineWithExercises: RoutineWithExercises,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val routine = routineWithExercises.routine
    val exercises = routineWithExercises.exercises
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con título y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (routine.description.isNotBlank()) {
                        Text(
                            text = routine.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar rutina",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Días de la semana
            if (routine.daysOfWeek.isNotBlank()) {
                Text(
                    text = "Días: ${routine.daysOfWeek}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Información de ejercicios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${exercises.size} ejercicios",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Badge de estado
                if (routine.isActive) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Activa") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            
            // Botón de iniciar entrenamiento
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar Entrenamiento")
            }
        }
    }
}

