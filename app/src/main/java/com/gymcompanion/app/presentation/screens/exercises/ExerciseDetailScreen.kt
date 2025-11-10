package com.gymcompanion.app.presentation.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Pantalla de detalle de ejercicio
 * Muestra información completa del ejercicio seleccionado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ExercisesViewModel = hiltViewModel()
) {
    val exercise by viewModel.filteredExercises.collectAsState()
    val selectedExercise = exercise.find { it.id == exerciseId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Ejercicio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (selectedExercise == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre del ejercicio
                item {
                    Text(
                        text = selectedExercise.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Tags
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Grupo muscular
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Color(viewModel.getMuscleGroupColor(selectedExercise.muscleGroup))
                                        .copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = getMuscleGroupName(selectedExercise.muscleGroup),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(viewModel.getMuscleGroupColor(selectedExercise.muscleGroup))
                            )
                        }
                        
                        // Dificultad
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(getDifficultyColor(selectedExercise.difficulty).copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = getDifficultyName(selectedExercise.difficulty),
                                style = MaterialTheme.typography.labelMedium,
                                color = getDifficultyColor(selectedExercise.difficulty)
                            )
                        }
                    }
                }
                
                // Descripción
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = selectedExercise.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Equipo necesario
                if (selectedExercise.equipmentNeeded.isNotBlank()) {
                    item {
                        InfoSection(
                            title = "Equipo Necesario",
                            content = selectedExercise.equipmentNeeded
                        )
                    }
                }
                
                // Instrucciones
                if (selectedExercise.instructions.isNotBlank()) {
                    item {
                        val instructions = try {
                            val gson = Gson()
                            val type = object : TypeToken<List<String>>() {}.type
                            gson.fromJson<List<String>>(selectedExercise.instructions, type)
                        } catch (e: Exception) {
                            listOf(selectedExercise.instructions)
                        }
                        
                        ListSection(
                            title = "Instrucciones",
                            items = instructions,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Errores comunes
                if (selectedExercise.commonMistakes.isNotBlank()) {
                    item {
                        val mistakes = try {
                            val gson = Gson()
                            val type = object : TypeToken<List<String>>() {}.type
                            gson.fromJson<List<String>>(selectedExercise.commonMistakes, type)
                        } catch (e: Exception) {
                            listOf(selectedExercise.commonMistakes)
                        }
                        
                        ListSection(
                            title = "Errores Comunes a Evitar",
                            items = mistakes,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Tips de seguridad
                if (selectedExercise.safetyTips.isNotBlank()) {
                    item {
                        val tips = try {
                            val gson = Gson()
                            val type = object : TypeToken<List<String>>() {}.type
                            gson.fromJson<List<String>>(selectedExercise.safetyTips, type)
                        } catch (e: Exception) {
                            listOf(selectedExercise.safetyTips)
                        }
                        
                        ListSection(
                            title = "Tips de Seguridad",
                            items = tips,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección de información simple
 */
@Composable
fun InfoSection(
    title: String,
    content: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/**
 * Sección de lista con bullets
 */
@Composable
fun ListSection(
    title: String,
    items: List<String>,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
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
