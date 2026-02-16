package com.gymcompanion.app.presentation.screens.workout.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import com.gymcompanion.app.domain.usecase.PlateCalculator
import com.gymcompanion.app.presentation.components.*
import com.gymcompanion.app.presentation.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 🏋️ FOCUS MODE COMPONENTS — Premium Dark Neon Design
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
    isNavigating: Boolean = false,
    modifier: Modifier = Modifier
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf(8) }
    var showExerciseInfo by remember { mutableStateOf(false) }

    // Pre-fill logic
    LaunchedEffect(exercise.id) {
        if (completedSets.isNotEmpty()) {
            val lastSet = completedSets.last()
            weight = lastSet.weightUsed?.toString() ?: ""
            reps = lastSet.repsCompleted.toString()
        }
    }

    LaunchedEffect(completedSets.lastOrNull()?.id) {
        completedSets.lastOrNull()?.let { lastSet ->
            weight = lastSet.weightUsed?.toString() ?: ""
            reps = lastSet.repsCompleted.toString()
        }
    }

    var energyLevel by remember { mutableStateOf("Normal") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Neon Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f, fill = false)
                )
                IconButton(onClick = { showExerciseInfo = true }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ver instrucciones",
                        tint = NeonBlue
                    )
                }
            }

            // Energy Level Selector (Neon Chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Energía:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )

                listOf(
                    "Normal" to "💪 Normal",
                    "Tired" to "😓 Cansado",
                    "Very Tired" to "😴 Muy Cansado"
                ).forEach { (level, label) ->
                    val isSelected = energyLevel == level
                    val chipColor = when (level) {
                        "Normal" -> NeonGreen
                        "Tired" -> NeonOrange
                        else -> NeonPink
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) chipColor.copy(alpha = 0.2f) else DarkSurfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isSelected) chipColor else Color.Transparent,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { energyLevel = level }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) chipColor else TextSecondary
                        )
                    }
                }
            }

            // Target Text
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
                text = if (energyLevel != "Normal") "Meta ajustada: $adjustedSets sets × $adjustedReps reps" else "Meta: ${routineExercise.plannedSets} sets × ${routineExercise.plannedReps} reps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (energyLevel != "Normal") NeonOrange else NeonBlue
            )

            if (energyLevel != "Normal") {
                Text(
                    text = when (energyLevel) {
                        "Tired" -> "-20% sets, -10% reps"
                        "Very Tired" -> "-40% sets, -25% reps"
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonOrange
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // History / Completed Sets
            if (completedSets.isNotEmpty()) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Sets completados",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        completedSets.forEach { set ->
                            CompletedSetItem(set = set, onDelete = { onDeleteSet(set) })
                        }
                    }
                }
            }

            // Input Area (Neon Gradient Card)
            NeonGradientCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = GradientPrimary
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Set $currentSetNumber",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LargeSetInput(
                            value = weight,
                            onValueChange = { weight = it },
                            label = "LBS",
                            modifier = Modifier.weight(1f)
                        )
                        LargeSetInput(
                            value = reps,
                            onValueChange = { reps = it },
                            label = "REPS",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    PlateCalculatorView(
                        targetWeight = weight.toDoubleOrNull() ?: 0.0,
                        equipmentType = exercise.equipmentNeeded,
                        plateCalculator = plateCalculator
                    )

                    // RPE Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Esfuerzo (RPE)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("$rpe/10", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeonBlue)
                        }
                        Slider(
                            value = rpe.toFloat(),
                            onValueChange = { rpe = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonBlue,
                                activeTrackColor = NeonBlue,
                                inactiveTrackColor = NeonBlue.copy(alpha = 0.2f)
                            )
                        )
                    }

                    PulsingWorkoutButton(
                        text = "REGISTRAR SET",
                        onClick = {
                            val w = weight.toDoubleOrNull() ?: 0.0
                            val r = reps.toIntOrNull() ?: 0
                            if (r > 0) onLogSet(w, r, 0, rpe)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Check,
                        gradientColors = if (reps.toIntOrNull()?.let { it > 0 } == true) GradientSuccess else GradientPrimary
                    )
                }
            }

            // Navigation Buttons
            ExerciseNavigation(
                onNext = onNextExercise,
                onPrev = onPrevExercise,
                hasNext = hasNext,
                hasPrev = hasPrev,
                isNavigating = isNavigating
            )
        }
    }

    if (showExerciseInfo) {
        ExerciseInfoBottomSheet(
            exercise = exercise,
            onDismiss = { showExerciseInfo = false }
        )
    }
}

@Composable
fun CompletedSetItem(set: ExerciseSetEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${set.setNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${set.weightUsed} lbs × ${set.repsCompleted}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            if (set.rpe != null) {
                Text(
                    text = " @ RPE ${set.rpe}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Borrar",
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
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
    val isWeight = label.contains("lb", ignoreCase = true)
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
        ) {
            IconButton(
                onClick = {
                    if (isWeight) {
                        val current = value.toDoubleOrNull() ?: 100.0 // Default start
                        onValueChange(((current - 2.5).coerceAtLeast(0.0)).toString())
                    } else {
                        val current = value.toIntOrNull() ?: 8 // Default start
                        onValueChange(((current - 1).coerceAtLeast(0)).toString())
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Remove, null, tint = NeonBlue, modifier = Modifier.size(16.dp))
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = NeonBlue
                    ),
                    singleLine = true
                )
            }

            IconButton(
                onClick = {
                    if (isWeight) {
                        val current = value.toDoubleOrNull() ?: 100.0
                        onValueChange((current + 2.5).toString())
                    } else {
                        val current = value.toIntOrNull() ?: 8
                        onValueChange((current + 1).toString())
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = NeonBlue, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ExerciseNavigation(
    onNext: () -> Unit,
    onPrev: () -> Unit,
    hasNext: Boolean,
    hasPrev: Boolean,
    isNavigating: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasPrev) {
            OutlinedButton(
                onClick = onPrev,
                enabled = !isNavigating,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonBlue),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(NeonBlue, NeonPurple))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Anterior")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (hasNext) {
            Button(
                onClick = onNext,
                enabled = !isNavigating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(NeonBlue, NeonPurple)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Siguiente", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                    }
                }
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
    LaunchedEffect(Unit) {
        voiceCoach?.announceRestTimer(totalSeconds)
    }
    
    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining == 10) voiceCoach?.speak("Quedan 10 segundos")
        else if (secondsRemaining == 3) voiceCoach?.speak("3, 2, 1")
        else if (secondsRemaining == 0) voiceCoach?.announceRestComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground.copy(alpha = 0.95f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Text(
                text = "Descanso",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            
            NeonProgressRing(
                progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f,
                size = 280.dp,
                strokeWidth = 16.dp,
                gradientColors = GradientSuccess
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", secondsRemaining / 60, secondsRemaining % 60),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        fontSize = 56.sp
                    )
                    Text(
                        text = "RESTANTE",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary,
                        letterSpacing = 2.sp
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f))
                ) {
                    Text("SALTAR", color = NeonPink, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onAdd30s,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
                ) {
                    Text("+30s", color = NeonBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseInfoBottomSheet(
    exercise: ExerciseEntity,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextTertiary) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            if (!exercise.illustrationPath.isNullOrBlank()) {
                item {
                    AsyncImage(
                        model = exercise.illustrationPath,
                        contentDescription = exercise.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModernBadge(text = exercise.muscleGroup, containerColor = NeonBlue.copy(alpha = 0.2f), contentColor = NeonBlue)
                    if (exercise.equipmentNeeded.isNotBlank()) {
                        ModernBadge(text = exercise.equipmentNeeded, containerColor = NeonPurple.copy(alpha = 0.2f), contentColor = NeonPurple)
                    }
                }
            }

            if (exercise.description.isNotBlank()) {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📖 Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(exercise.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
            }

            if (exercise.instructionsSteps.isNotBlank()) {
                item {
                    val instructions = try {
                        val gson = Gson()
                        val type = object : TypeToken<List<String>>() {}.type
                        gson.fromJson<List<String>>(exercise.instructionsSteps, type)
                    } catch (e: Exception) {
                        listOf(exercise.instructionsSteps)
                    }
                    ExerciseInfoListSection(
                        title = "📋 Instrucciones",
                        items = instructions,
                        color = NeonBlue,
                        numbered = true
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseInfoListSection(
    title: String,
    items: List<String>,
    color: Color,
    numbered: Boolean = false
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            items.forEachIndexed { index, item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (numbered) "${index + 1}." else "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        fontWeight = FontWeight.Bold
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
