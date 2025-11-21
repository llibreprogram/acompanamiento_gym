package com.gymcompanion.app.presentation.screens.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.ExerciseEntity

/**
 * Pantalla de Ejercicios - Muestra biblioteca de ejercicios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    viewModel: ExercisesViewModel = hiltViewModel(),
    onExerciseClick: (Long) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMuscleGroup by viewModel.selectedMuscleGroup.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val filteredExercises by viewModel.filteredExercises.collectAsState()
    val muscleGroups by viewModel.muscleGroups.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Ejercicios",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar ejercicios...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar búsqueda"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                
                // Filtros de grupo muscular
                AnimatedVisibility(visible = muscleGroups.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Grupo Muscular",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedMuscleGroup == null,
                                    onClick = { viewModel.onMuscleGroupSelected(null) },
                                    label = { Text("Todos") }
                                )
                            }
                            items(muscleGroups) { muscleGroup ->
                                FilterChip(
                                    selected = selectedMuscleGroup == muscleGroup,
                                    onClick = { 
                                        viewModel.onMuscleGroupSelected(
                                            if (selectedMuscleGroup == muscleGroup) null else muscleGroup
                                        )
                                    },
                                    label = { Text(getMuscleGroupName(muscleGroup)) }
                                )
                            }
                        }
                    }
                }
                
                // Filtros de dificultad
                Column {
                    Text(
                        text = "Dificultad",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDifficulty == null,
                                onClick = { viewModel.onDifficultySelected(null) },
                                label = { Text("Todas") }
                            )
                        }
                        items(listOf("beginner", "intermediate", "advanced")) { difficulty ->
                            FilterChip(
                                selected = selectedDifficulty == difficulty,
                                onClick = { 
                                    viewModel.onDifficultySelected(
                                        if (selectedDifficulty == difficulty) null else difficulty
                                    )
                                },
                                label = { Text(getDifficultyName(difficulty)) }
                            )
                        }
                    }
                }
                
                Divider()
            }
        }
    ) { paddingValues ->
    when (uiState) {
            is ExercisesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ExercisesUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No hay ejercicios",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "La base de datos se inicializará automáticamente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is ExercisesUiState.Success -> {
                if (filteredExercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sin resultados",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Prueba con otros filtros o búsqueda",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text("Limpiar filtros")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredExercises) { exercise ->
                            ExerciseCard(
                                exercise = exercise,
                                onClick = { onExerciseClick(exercise.id) },
                                muscleGroupColor = Color(viewModel.getMuscleGroupColor(exercise.muscleGroup))
                            )
                        }
                    }
                }
            }
            is ExercisesUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as ExercisesUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Card de ejercicio individual
 */
@Composable
fun ExerciseCard(
    exercise: ExerciseEntity,
    onClick: () -> Unit,
    muscleGroupColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nombre del ejercicio
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Descripción
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Grupo muscular
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(muscleGroupColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = getMuscleGroupName(exercise.muscleGroup),
                            style = MaterialTheme.typography.labelSmall,
                            color = muscleGroupColor
                        )
                    }
                    
                    // Dificultad
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(getDifficultyColor(exercise.difficulty).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = getDifficultyName(exercise.difficulty),
                            style = MaterialTheme.typography.labelSmall,
                            color = getDifficultyColor(exercise.difficulty)
                        )
                    }
                    
                    // Equipo necesario
                    if (exercise.equipmentNeeded.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = exercise.equipmentNeeded,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Obtiene el nombre localizado del grupo muscular
 */
private fun getMuscleGroupName(muscleGroup: String): String {
    return when (muscleGroup) {
        "chest" -> "Pecho"
        "back" -> "Espalda"
        "legs" -> "Piernas"
        "shoulders" -> "Hombros"
        "arms" -> "Brazos"
        "core" -> "Core"
        "full_body" -> "Cuerpo completo"
        else -> muscleGroup
    }
}

/**
 * Obtiene el nombre localizado de la dificultad
 */
private fun getDifficultyName(difficulty: String): String {
    return when (difficulty) {
        "beginner" -> "Principiante"
        "intermediate" -> "Intermedio"
        "advanced" -> "Avanzado"
        else -> difficulty
    }
}

/**
 * Obtiene el color según la dificultad
 */
private fun getDifficultyColor(difficulty: String): Color {
    return when (difficulty) {
        "beginner" -> Color(0xFF4CAF50)
        "intermediate" -> Color(0xFFFF9800)
        "advanced" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}
