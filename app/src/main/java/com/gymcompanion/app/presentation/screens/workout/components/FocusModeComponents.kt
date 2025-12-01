package com.gymcompanion.app.presentation.screens.workout.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import com.gymcompanion.app.domain.usecase.PlateCalculator

/**
 * Sesión de entrenamiento en Modo Enfoque (Inmersivo)
 */
@Composable
fun FocusModeSession(
    exercise: ExerciseEntity,
    routineExercise: RoutineExerciseEntity,
    currentSetNumber: Int,
    completedSets: List<ExerciseSetEntity>,
    plateCalculator: PlateCalculator,
    onLogSet: (Double, Int, Int, Int) -> Unit,
    onDeleteSet: (ExerciseSetEntity) -> Unit,
    onNextExercise: () -> Unit,
    onPrevExercise: () -> Unit,
    hasNext: Boolean,
    hasPrev: Boolean,
    modifier: Modifier = Modifier
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf(8) }
    
    // Pre-fill with last set values if available, or routine targets
    LaunchedEffect(exercise.id, completedSets.size) {
        if (completedSets.isNotEmpty()) {
            val lastSet = completedSets.last()
            weight = lastSet.weightUsed?.toString() ?: ""
            reps = lastSet.repsCompleted.toString()
        }
    }

    var energyLevel by remember { mutableStateOf("Normal") } // Normal, Tired, Very Tired
    
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Fixed Header: Exercise Name & Target
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Energy Level Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Energía:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                AssistChip(
                    onClick = { energyLevel = "Normal" },
                    label = { Text("💪 Normal") },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (energyLevel == "Normal") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                AssistChip(
                    onClick = { energyLevel = "Tired" },
                    label = { Text("😓 Cansado") },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (energyLevel == "Tired") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                AssistChip(
                    onClick = { energyLevel = "Very Tired" },
                    label = { Text("😴 Muy Cansado") },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (energyLevel == "Very Tired") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            
            // Adjusted target based on energy level
            val adjustedSets = when (energyLevel) {
                "Tired" -> (routineExercise.plannedSets.toDouble() * 0.8).toInt().coerceAtLeast(1)
                "Very Tired" -> (routineExercise.plannedSets.toDouble() * 0.6).toInt().coerceAtLeast(1)
                else -> routineExercise.plannedSets
            }
            
            val adjustedReps = when (energyLevel) {
                "Tired" -> (routineExercise.plannedReps.toDouble() * 0.9).toInt().coerceAtLeast(1)
                "Very Tired" -> (routineExercise.plannedReps.toDouble() * 0.75).toInt().coerceAtLeast(1)
                else -> routineExercise.plannedReps
            }
            
            Text(
                text = if (energyLevel != "Normal") {
                    "Meta ajustada: $adjustedSets sets × $adjustedReps reps"
                } else {
                    "Meta: ${routineExercise.plannedSets} sets × ${routineExercise.plannedReps} reps"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (energyLevel != "Normal") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            
            if (energyLevel != "Normal") {
                Text(
                    text = when (energyLevel) {
                        "Tired" -> "Reducción: -20% sets, -10% reps"
                        "Very Tired" -> "Reducción: -40% sets, -25% reps"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // History / Completed Sets
            if (completedSets.isNotEmpty()) {
                Text(
                    text = "Sets completados:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                completedSets.forEach { set ->
                    CompletedSetItem(set = set, onDelete = { onDeleteSet(set) })
                }
            }

            // Input Area
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Set $currentSetNumber",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LargeSetInput(
                            value = weight,
                            onValueChange = { weight = it },
                            label = "Peso (kg)",
                            modifier = Modifier.weight(1f)
                        )
                        LargeSetInput(
                            value = reps,
                            onValueChange = { reps = it },
                            label = "Reps",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Plate Calculator
                    PlateCalculatorView(
                        targetWeight = weight.toDoubleOrNull() ?: 0.0,
                        equipmentType = exercise.equipmentNeeded,
                        plateCalculator = plateCalculator
                    )
                    
                    // RPE Selector
                    Column {
                        Text("Esfuerzo (RPE): $rpe", style = MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = rpe.toFloat(),
                            onValueChange = { rpe = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }

                    Button(
                        onClick = {
                            val w = weight.toDoubleOrNull() ?: 0.0
                            val r = reps.toIntOrNull() ?: 0
                            android.util.Log.d("FocusMode", "Button clicked: weight='$weight' ($w), reps='$reps' ($r), rpe=$rpe")
                            if (r > 0) {
                                android.util.Log.d("FocusMode", "Logging set...")
                                onLogSet(w, r, 0, rpe)
                                // Fields will auto-fill from the LaunchedEffect when completedSets.size changes
                            } else {
                                android.util.Log.e("FocusMode", "Cannot log set: reps must be > 0. Current reps='$reps'")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = reps.toIntOrNull()?.let { it > 0 } ?: false
                    ) {
                        Text("REGISTRAR SET", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Navigation
            ExerciseNavigation(
                onNext = onNextExercise,
                onPrev = onPrevExercise,
                hasNext = hasNext,
                hasPrev = hasPrev
            )
        }
    }
}

@Composable
fun CompletedSetItem(set: ExerciseSetEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${set.setNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "${set.weightUsed}kg × ${set.repsCompleted}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LargeSetInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val isWeight = label.contains("Peso")
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 2.dp)
        ) {
            IconButton(
                onClick = {
                    if (isWeight) {
                        val current = value.toDoubleOrNull() ?: 0.0
                        if (current > 0) onValueChange((current - 2.5).toString())
                    } else {
                        val current = value.toIntOrNull() ?: 0
                        if (current > 0) onValueChange((current - 1).toString())
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp))
            }
            
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            IconButton(
                onClick = {
                    if (isWeight) {
                        val current = value.toDoubleOrNull() ?: 0.0
                        onValueChange((current + 2.5).toString())
                    } else {
                        val current = value.toIntOrNull() ?: 0
                        onValueChange((current + 1).toString())
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ExerciseNavigation(
    onNext: () -> Unit,
    onPrev: () -> Unit,
    hasNext: Boolean,
    hasPrev: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasPrev) {
            Button(
                onClick = onPrev,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Anterior", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (hasNext) {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text("Siguiente", color = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}

@Composable
fun RestTimerOverlay(
    secondsRemaining: Int,
    totalSeconds: Int,
    onSkip: () -> Unit,
    onAdd30s: () -> Unit,
    voiceCoach: com.gymcompanion.app.domain.usecase.VoiceCoachManager? = null
) {
    // Announce rest start
    LaunchedEffect(Unit) {
        voiceCoach?.announceRestTimer(totalSeconds)
    }
    
    // Announce 10 seconds remaining
    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining == 10) {
            voiceCoach?.speak("Quedan 10 segundos")
        } else if (secondsRemaining == 3) {
            voiceCoach?.speak("3, 2, 1")
        } else if (secondsRemaining == 0) {
            voiceCoach?.announceRestComplete()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f))
            .clickable(enabled = false) {}, // Block touches
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = "Descanso",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f,
                    modifier = Modifier.size(240.dp),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                Text(
                    text = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text("Saltar", color = MaterialTheme.colorScheme.onErrorContainer)
                }
                
                Button(
                    onClick = onAdd30s,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text("+30s", color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
    }
}
