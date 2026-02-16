package com.gymcompanion.app.presentation.screens.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymcompanion.app.presentation.components.GlassmorphicCard
import com.gymcompanion.app.presentation.components.ModernBadge
import com.gymcompanion.app.presentation.theme.*

/**
 * ℹ️ EXERCISE DETAIL SCREEN — Premium Dark Neon Design
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
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Ejercicio", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = NeonBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = DarkSurface.copy(alpha = 0.95f)
                )
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
                CircularProgressIndicator(color = NeonBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Nombre del ejercicio
                item {
                    Text(
                        text = selectedExercise.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Imágenes
                item {
                    if (!selectedExercise.illustrationPath.isNullOrBlank()) {
                        if (!selectedExercise.illustrationPath2.isNullOrBlank()) {
                            // Dos imágenes lado a lado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = selectedExercise.illustrationPath,
                                    contentDescription = "Inicio",
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(DarkSurface),
                                    contentScale = ContentScale.Crop
                                )
                                AsyncImage(
                                    model = selectedExercise.illustrationPath2,
                                    contentDescription = "Fin",
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(DarkSurface),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            // Una sola imagen
                            AsyncImage(
                                model = selectedExercise.illustrationPath,
                                contentDescription = selectedExercise.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkSurface),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                // Tags
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Grupo muscular
                        ModernBadge(
                            text = getMuscleGroupName(selectedExercise.muscleGroup),
                            containerColor = NeonBlue.copy(alpha = 0.15f),
                            contentColor = NeonBlue
                        )
                        
                        // Dificultad
                        val diffColor = getDifficultyColor(selectedExercise.difficulty)
                        ModernBadge(
                            text = getDifficultyName(selectedExercise.difficulty),
                            containerColor = diffColor.copy(alpha = 0.15f),
                            contentColor = diffColor
                        )
                    }
                }
                
                // Descripción
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "📖 Descripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = selectedExercise.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }
                    }
                }
                
                // Equipo necesario
                if (selectedExercise.equipmentNeeded.isNotBlank()) {
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "🏋️ Equipo Necesario",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = selectedExercise.equipmentNeeded,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                
                // Instrucciones
                if (selectedExercise.instructionsSteps.isNotBlank()) {
                    item {
                        val instructions = try {
                            val gson = Gson()
                            val type = object : TypeToken<List<String>>() {}.type
                            gson.fromJson<List<String>>(selectedExercise.instructionsSteps, type)
                        } catch (e: Exception) {
                            listOf(selectedExercise.instructionsSteps)
                        }
                        
                        NeonListSection(
                            title = "📋 Instrucciones",
                            items = instructions,
                            color = NeonBlue,
                            numbered = true
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
                        
                        NeonListSection(
                            title = "⚠️ Errores Comunes",
                            items = mistakes,
                            color = NeonOrange
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
                        
                        NeonListSection(
                            title = "🛡️ Tips de Seguridad",
                            items = tips,
                            color = NeonGreen
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun NeonListSection(
    title: String,
    items: List<String>,
    color: Color,
    numbered: Boolean = false
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = color.copy(alpha = 0.3f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            items.forEachIndexed { index, item ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (numbered) "${index + 1}." else "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
