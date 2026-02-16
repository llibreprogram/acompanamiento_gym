@file:OptIn(ExperimentalMaterial3Api::class)

package com.gymcompanion.app.presentation.screens.routines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseWithExercise

/**
 * Pantalla de detalle de rutina
 * Muestra todos los ejercicios de la rutina por día
 */
@Composable
fun RoutineDetailScreen(
    routineId: Long,
    onNavigateBack: () -> Unit,
    viewModel: RoutinesViewModel = hiltViewModel()
) {
    val allRoutines by viewModel.allRoutines.collectAsState()
    val routine = allRoutines.find { it.routine.id == routineId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        routine?.routine?.name ?: "Detalle de Rutina",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (routine == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            var showSwapDialog by remember { mutableStateOf<RoutineExerciseWithExercise?>(null) }
            val alternatives by viewModel.alternativeExercises.collectAsState()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ... (existing items)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (!routine.routine.description.isNullOrBlank()) {
                                Text(
                                    text = routine.routine.description ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "⏱️ ${routine.routine.duration} min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "📊 ${routine.routine.difficulty}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "🎯 ${routine.routine.focusArea}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Exercises count
                item {
                    Text(
                        text = "📋 ${routine.routineExercises.size} ejercicios en esta rutina",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Exercise list
                items(routine.routineExercises) { routineExercise ->
                    ExerciseItemCard(
                        routineExercise = routineExercise,
                        position = routine.routineExercises.indexOf(routineExercise) + 1,
                        onSwapClick = {
                            viewModel.loadAlternativesForExercise(routineExercise.exercise)
                            showSwapDialog = routineExercise
                        }
                    )
                }
                
                // AI Generated badge (moved inside else to avoid duplication if I'm not careful)
                if (routine.routine.isAIGenerated) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🤖 Generada con IA",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Dialogo para cambiar ejercicio
            if (showSwapDialog != null) {
                AlertDialog(
                    onDismissRequest = { showSwapDialog = null },
                    title = { Text("Cambiar ejercicio") },
                    text = {
                        Column {
                            Text(
                                "Selecciona una alternativa para: ${showSwapDialog?.exercise?.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (alternatives.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Buscando alternativas...")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 400.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(alternatives) { alternative ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = {
                                                viewModel.replaceExerciseInRoutine(
                                                    showSwapDialog!!.routineExercise,
                                                    alternative
                                                )
                                                showSwapDialog = null
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        alternative.name,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        alternative.equipmentNeeded,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                                Icon(
                                                    Icons.Default.Sync,
                                                    contentDescription = "Seleccionar",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSwapDialog = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseItemCard(
    routineExercise: com.gymcompanion.app.data.local.entity.RoutineExerciseWithExercise,
    position: Int,
    onSwapClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position number
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$position",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            // Thumbnail
            if (!routineExercise.exercise.illustrationPath.isNullOrBlank()) {
                AsyncImage(
                    model = routineExercise.exercise.illustrationPath,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            } else {
                 // Fallback if no image, maybe show the number in a nicer way or just keep the spacer
            }
            
            // Exercise info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = routineExercise.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    IconButton(onClick = onSwapClick) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Cambiar ejercicio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Description
                if (!routineExercise.exercise.description.isNullOrBlank()) {
                    Text(
                        text = routineExercise.exercise.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Series and reps
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔢 ${routineExercise.routineExercise.plannedSets} series",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "🔁 ${routineExercise.routineExercise.plannedReps} reps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (routineExercise.routineExercise.restTimeSeconds > 0) {
                        Text(
                            text = "⏸️ ${routineExercise.routineExercise.restTimeSeconds}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Muscle groups and equipment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                routineExercise.exercise.muscleGroup,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (routineExercise.exercise.equipmentNeeded.isNotBlank()) {
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    routineExercise.exercise.equipmentNeeded,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }
    }
}
