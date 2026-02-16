package com.gymcompanion.app.presentation.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.presentation.components.GlassmorphicCard
import com.gymcompanion.app.presentation.components.PulsingWorkoutButton
import com.gymcompanion.app.presentation.screens.workout.components.ExerciseInfoBottomSheet
import com.gymcompanion.app.presentation.screens.workout.components.FocusModeSession
import com.gymcompanion.app.presentation.screens.workout.components.RestTimerOverlay
import com.gymcompanion.app.presentation.theme.*

/**
 * ⚡ WORKOUT SCREEN — Premium Dark Neon Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    routineId: Long,
    onWorkoutComplete: () -> Unit,
    onWorkoutCancel: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val routine by viewModel.routine.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isResting by viewModel.isResting.collectAsState()
    val restTimerSeconds by viewModel.restTimerSeconds.collectAsState()
    val sessionSets by viewModel.sessionSets.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isNavigating by viewModel.isNavigating.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSetDialog by remember { mutableStateOf(false) }
    var selectedExerciseId by remember { mutableStateOf<Long?>(null) }
    var isFocusMode by remember { mutableStateOf(true) }
    var exerciseInfoTarget by remember { mutableStateOf<ExerciseEntity?>(null) }
    
    // Iniciar sesión al cargar
    LaunchedEffect(routineId) {
        viewModel.startWorkoutSession(routineId)
    }
    
    // Observar feedback de RPE
    LaunchedEffect(Unit) {
        viewModel.rpeFeedback.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    // Manejar estados finales
    LaunchedEffect(uiState) {
        when (uiState) {
            is WorkoutUiState.Completed -> onWorkoutComplete()
            is WorkoutUiState.Cancelled -> onWorkoutCancel()
            else -> {}
        }
    }
    
    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = routine?.routine?.name ?: "Entrenamiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = viewModel.formatTime(timerSeconds),
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.Default.Close, "Cancelar", tint = TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = { isFocusMode = !isFocusMode }) {
                        Icon(
                            if (isFocusMode) Icons.Default.List else Icons.Default.ViewCarousel,
                            contentDescription = "Cambiar vista",
                            tint = NeonPurple
                        )
                    }
                    IconButton(onClick = { showCompleteDialog = true }) {
                        Icon(Icons.Default.Check, "Finalizar", tint = NeonGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = DarkSurface.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido principal
            if (isFocusMode) {
                routine?.let { routineWithExercises ->
                    if (routineWithExercises.routineExercises.isNotEmpty()) {
                        val currentExercise = routineWithExercises.routineExercises.getOrNull(currentExerciseIndex)
                        currentExercise?.let { exerciseWithDetails ->
                            val exercise = exerciseWithDetails.exercise
                            val routineExercise = exerciseWithDetails.routineExercise
                            val sets = viewModel.getSetsForExercise(exercise.id)
                            
                            FocusModeSession(
                                exercise = exercise,
                                routineExercise = routineExercise,
                                currentSetNumber = sets.size + 1,
                                completedSets = sets,
                                plateCalculator = viewModel.plateCalculator,
                                onLogSet = { weight, reps, rir, rpe ->
                                    viewModel.logSet(exercise.id, sets.size + 1, weight, reps, rir, rpe)
                                },
                                onDeleteSet = { set -> viewModel.deleteSet(set) },
                                onNextExercise = { viewModel.nextExercise() },
                                onPrevExercise = { viewModel.prevExercise() },
                                hasNext = currentExerciseIndex < routineWithExercises.routineExercises.size - 1,
                                hasPrev = currentExerciseIndex > 0,
                                isNavigating = isNavigating
                            )
                        }
                    }
                }
            } else {
                // Lista de ejercicios (Legacy view redesign)
                routine?.let { routineWithExercises ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Timer visible en lista si no hay focus mode
                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Tiempo Total", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                        Text(
                                            viewModel.formatTime(timerSeconds),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonBlue
                                        )
                                    }
                                    if (isResting) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Descanso", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                            Text(
                                                viewModel.formatTime(restTimerSeconds.toLong()),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(routineWithExercises.routineExercises) { routineExerciseWithExercise ->
                            val exercise = routineExerciseWithExercise.exercise
                            val routineExercise = routineExerciseWithExercise.routineExercise
                            val sets = viewModel.getSetsForExercise(exercise.id)
                            val isCurrentExercise = routineWithExercises.routineExercises.indexOf(routineExerciseWithExercise) == currentExerciseIndex
                            
                            ExerciseWorkoutCard(
                                exerciseName = exercise.name,
                                targetSets = routineExercise.plannedSets,
                                targetReps = routineExercise.plannedReps,
                                completedSets = sets,
                                isCurrentExercise = isCurrentExercise,
                                onAddSet = {
                                    selectedExerciseId = exercise.id
                                    showSetDialog = true
                                },
                                onDeleteSet = { set -> viewModel.deleteSet(set) },
                                onInfoClick = { exerciseInfoTarget = exercise }
                            )
                        }
                        
                        item {
                             PulsingWorkoutButton(
                                text = "FINALIZAR ENTRENAMIENTO",
                                onClick = { showCompleteDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                gradientColors = GradientSuccess
                            )
                        }
                    }
                }
            }
            
            // Overlay de descanso (Solo en Focus Mode y si está descansando)
            // En modo lista, se muestra el timer arriba
            if (isFocusMode && isResting) {
                RestTimerOverlay(
                    secondsRemaining = restTimerSeconds,
                    totalSeconds = viewModel.restTimerTotalSeconds.collectAsState().value,
                    onSkip = { viewModel.skipRest() },
                    onAdd30s = { viewModel.addRestTime(30) },
                    voiceCoach = viewModel.voiceCoach
                )
            }
        }
    }
    
    // Diálogo para agregar set (Legacy/List Mode)
    if (showSetDialog && selectedExerciseId != null) {
        val sets = viewModel.getSetsForExercise(selectedExerciseId!!)
        AddSetDialog(
            exerciseId = selectedExerciseId!!,
            setNumber = sets.size + 1,
            onDismiss = { showSetDialog = false },
            onConfirm = { weight, reps, rir, rpe ->
                viewModel.logSet(selectedExerciseId!!, sets.size + 1, weight, reps, rir, rpe)
                showSetDialog = false
            }
        )
    }
    
    // Diálogo de completar
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Finalizar Entrenamiento", color = TextPrimary) },
            text = { Text("¿Has completado tu entrenamiento?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeWorkout()
                        showCompleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Finalizar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancelar", color = TextTertiary)
                }
            }
        )
    }
    
    // Diálogo de cancelar
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Cancelar Entrenamiento", color = TextPrimary) },
            text = { Text("¿Seguro que quieres cancelar? Se perderán los datos.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelWorkout()
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar Entrenamiento")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Volver", color = TextTertiary)
                }
            }
        )
    }

    // Bottom Sheet de información del ejercicio
    if (exerciseInfoTarget != null) {
        ExerciseInfoBottomSheet(
            exercise = exerciseInfoTarget!!,
            onDismiss = { exerciseInfoTarget = null }
        )
    }
}

/**
 * Card de ejercicio en modo lista (Redesigned)
 */
@Composable
fun ExerciseWorkoutCard(
    exerciseName: String,
    targetSets: Int,
    targetReps: String,
    completedSets: List<ExerciseSetEntity>,
    isCurrentExercise: Boolean,
    onAddSet: () -> Unit,
    onDeleteSet: (ExerciseSetEntity) -> Unit,
    onInfoClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isCurrentExercise) NeonBlue else GlassBorderSubtle
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$targetSets sets × $targetReps reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = NeonBlue
                    )
                }
                
                // Progreso Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (completedSets.size >= targetSets) NeonGreen.copy(alpha = 0.2f)
                            else DarkSurface
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${completedSets.size}/$targetSets",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (completedSets.size >= targetSets) NeonGreen else TextSecondary
                    )
                }
            }
            
            // Sets completados
            if (completedSets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    completedSets.forEach { set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface.copy(alpha = 0.5f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Set ${set.setNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonBlue
                            )
                            Text(
                                text = "${set.weightUsed ?: "—"}lbs × ${set.repsCompleted} reps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            IconButton(
                                onClick = { onDeleteSet(set) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Botón agregar set
            if (completedSets.size < targetSets) {
                OutlinedButton(
                    onClick = onAddSet,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Set ${completedSets.size + 1}")
                }
            }
        }
    }
}

@Composable
fun AddSetDialog(
    exerciseId: Long,
    setNumber: Int,
    onDismiss: () -> Unit,
    onConfirm: (Double?, Int?, Int?, Int?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rir by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf(8f) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = { Text("Registrar Set $setNumber", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Peso (lbs)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = TextTertiary,
                        focusedLabelColor = NeonBlue,
                        unfocusedLabelColor = TextTertiary,
                        cursorColor = NeonBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Repeticiones") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = TextTertiary,
                        focusedLabelColor = NeonBlue,
                        unfocusedLabelColor = TextTertiary,
                        cursorColor = NeonBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                Column {
                    Text("Esfuerzo (RPE): ${rpe.toInt()}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Slider(
                        value = rpe,
                        onValueChange = { rpe = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonBlue,
                            activeTrackColor = NeonBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(weight.toDoubleOrNull(), reps.toIntOrNull(), rir.toIntOrNull(), rpe.toInt())
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextTertiary)
            }
        }
    )
}
