@file:OptIn(ExperimentalMaterial3Api::class)

package com.gymcompanion.app.presentation.screens.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import coil.compose.SubcomposeAsyncImage

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
    
    // State for image zoom dialog
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header info
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
                        onImageClick = { imageUrl -> zoomedImageUrl = imageUrl }
                    )
                }
                
                // AI Generated badge
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
        }
    }
    
    // Image zoom dialog
    if (zoomedImageUrl != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { zoomedImageUrl = null }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { zoomedImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = zoomedImageUrl,
                    contentDescription = "Imagen ampliada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    loading = {
                        CircularProgressIndicator(color = Color.White)
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
    onImageClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
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
                
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Exercise images (both start and end positions)
                if (routineExercise.exercise.illustrationPath?.isNotBlank() == true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // First image (start position)
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { 
                                    routineExercise.exercise.illustrationPath?.let { onImageClick(it) }
                                }
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            SubcomposeAsyncImage(
                                model = routineExercise.exercise.illustrationPath,
                                contentDescription = "${routineExercise.exercise.name} - Inicio",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                },
                                error = {
                                    Text(
                                        text = "🏋️",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            )
                        }
                        
                        // Second image (end position) if available
                        if (routineExercise.exercise.illustrationPath2?.isNotBlank() == true) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { 
                                        routineExercise.exercise.illustrationPath2?.let { onImageClick(it) }
                                    }
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = routineExercise.exercise.illustrationPath2,
                                    contentDescription = "${routineExercise.exercise.name} - Final",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    loading = {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    },
                                    error = {
                                        Text(
                                            text = "🏋️",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                // Exercise info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = routineExercise.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    routineExercise.exercise.muscleGroup,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                        if (routineExercise.exercise.equipmentNeeded.isNotBlank()) {
                            AssistChip(
                                onClick = { },
                                label = { 
                                    Text(
                                        routineExercise.exercise.equipmentNeeded,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        }
                    }
                }
            }
            
            // Exercise tips/recommendations
            if (routineExercise.exercise.instructionsSteps.isNotBlank() && 
                routineExercise.exercise.instructionsSteps != "[]") {
                Spacer(modifier = Modifier.height(12.dp))
                
                val instructions = try {
                    val gson = com.google.gson.Gson()
                    val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                    gson.fromJson<List<String>>(routineExercise.exercise.instructionsSteps, type)
                } catch (e: Exception) {
                    emptyList()
                }
                
                if (instructions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Cómo hacer este ejercicio:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Show first 3 instructions
                            instructions.take(3).forEachIndexed { index, instruction ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}. ",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = instruction,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            
                            if (instructions.size > 3) {
                                Text(
                                    text = "... y ${instructions.size - 3} pasos más",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
