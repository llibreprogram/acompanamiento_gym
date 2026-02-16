package com.gymcompanion.app.presentation.screens.exercises

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.presentation.components.AnimatedEntrance
import com.gymcompanion.app.presentation.components.GlassmorphicCard
import com.gymcompanion.app.presentation.components.ModernBadge
import com.gymcompanion.app.presentation.theme.*

/**
 * 🏋️ EXERCISES SCREEN — Premium Dark Neon Design
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
        containerColor = DarkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .background(DarkSurface.copy(alpha = 0.95f))
                    .padding(bottom = 16.dp)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Biblioteca",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Buscar ejercicios...", color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = NeonBlue
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        cursorColor = NeonBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Muscle Group Filters
                if (muscleGroups.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Grupo Muscular",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                NeonFilterChip(
                                    selected = selectedMuscleGroup == null,
                                    onClick = { viewModel.onMuscleGroupSelected(null) },
                                    label = "Todos",
                                    color = NeonBlue
                                )
                            }
                            items(muscleGroups) { muscleGroup ->
                                NeonFilterChip(
                                    selected = selectedMuscleGroup == muscleGroup,
                                    onClick = { 
                                        viewModel.onMuscleGroupSelected(
                                            if (selectedMuscleGroup == muscleGroup) null else muscleGroup
                                        )
                                    },
                                    label = getMuscleGroupName(muscleGroup),
                                    color = NeonBlue
                                )
                            }
                        }
                    }
                }

                // Difficulty Filters
                Column {
                    Text(
                        text = "Dificultad",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            NeonFilterChip(
                                selected = selectedDifficulty == null,
                                onClick = { viewModel.onDifficultySelected(null) },
                                label = "Todas",
                                color = NeonPurple
                            )
                        }
                        items(listOf("beginner", "intermediate", "advanced")) { difficulty ->
                            NeonFilterChip(
                                selected = selectedDifficulty == difficulty,
                                onClick = { 
                                    viewModel.onDifficultySelected(
                                        if (selectedDifficulty == difficulty) null else difficulty
                                    )
                                },
                                label = getDifficultyName(difficulty),
                                color = getDifficultyColor(difficulty)
                            )
                        }
                    }
                }
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
                    CircularProgressIndicator(color = NeonBlue)
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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(NeonBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = NeonBlue
                            )
                        }
                        Text(
                            text = "No se encontraron ejercicios",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Intenta ajustar los filtros o la búsqueda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is ExercisesUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        AnimatedEntrance {
                            ExerciseCard(
                                exercise = exercise,
                                onClick = { onExerciseClick(exercise.id) },
                                muscleGroupColor = NeonBlue
                            )
                        }
                    }
                    // Spacer for bottom nav
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
            is ExercisesUiState.Error -> {
                // Handle error
            }
        }
    }
}

@Composable
fun NeonFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) color.copy(alpha = 0.2f) else DarkSurfaceElevated
            )
            .border(
                1.dp,
                if (selected) color else GlassBorderSubtle,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun ExerciseCard(
    exercise: ExerciseEntity,
    onClick: () -> Unit,
    muscleGroupColor: Color
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        showGlow = false
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen/Icono
            if (!exercise.illustrationPath.isNullOrBlank()) {
                AsyncImage(
                    model = exercise.illustrationPath,
                    contentDescription = exercise.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exercise.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = muscleGroupColor
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ModernBadge(
                        text = getMuscleGroupName(exercise.muscleGroup),
                        containerColor = muscleGroupColor.copy(alpha = 0.15f),
                        contentColor = muscleGroupColor
                    )
                    
                    ModernBadge(
                        text = getDifficultyName(exercise.difficulty),
                        containerColor = getDifficultyColor(exercise.difficulty).copy(alpha = 0.15f),
                        contentColor = getDifficultyColor(exercise.difficulty)
                    )
                }
            }
        }
    }
}

// Helpers localizados (Mismo mapeo que antes pero en variables por simplicidad, o reutilizar funciones si son puras)
fun getMuscleGroupName(muscleGroup: String): String {
    return when (muscleGroup.lowercase()) {
        "chest" -> "Pecho"
        "back" -> "Espalda"
        "legs" -> "Piernas"
        "shoulders" -> "Hombros"
        "arms" -> "Brazos"
        "core" -> "Abdominales"
        else -> muscleGroup.replaceFirstChar { it.uppercase() }
    }
}

fun getDifficultyName(difficulty: String): String {
    return when (difficulty.lowercase()) {
        "beginner" -> "Principiante"
        "intermediate" -> "Intermedio"
        "advanced" -> "Avanzado"
        else -> difficulty
    }
}

fun getDifficultyColor(difficulty: String): Color {
    return when (difficulty.lowercase()) {
        "beginner" -> NeonGreen
        "intermediate" -> NeonOrange
        "advanced" -> NeonPink
        else -> NeonBlue
    }
}
