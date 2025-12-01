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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.presentation.screens.workout.components.FocusModeSession
import com.gymcompanion.app.presentation.screens.workout.components.RestTimerOverlay

/**
 * Pantalla de sesión de entrenamiento activa
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
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSetDialog by remember { mutableStateOf(false) }
    var selectedExerciseId by remember { mutableStateOf<Long?>(null) }
    var isFocusMode by remember { mutableStateOf(true) }
    
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = routine?.routine?.name ?: "Entrenamiento",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = viewModel.formatTime(timerSeconds),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.Default.Close, "Cancelar")
                    }
                },
                actions = {
                    IconButton(onClick = { isFocusMode = !isFocusMode }) {
                        Icon(
                            if (isFocusMode) Icons.Default.List else Icons.Default.ViewCarousel,
                            contentDescription = "Cambiar vista"
                        )
                    }
                    IconButton(onClick = { showCompleteDialog = true }) {
                        Icon(Icons.Default.Check, "Finalizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
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
                                onPrevExercise = { viewModel.previousExercise() },
                                hasNext = currentExerciseIndex < routineWithExercises.routineExercises.size - 1,
                                hasPrev = currentExerciseIndex > 0
                            )
                        }
                    }
                }
            } else {
                // Timer de descanso (Legacy view)
                AnimatedVisibility(visible = isResting) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Descanso",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = viewModel.formatTime(restTimerSeconds.toLong()),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = { viewModel.skipRest() }) {
                                Text("Saltar descanso")
                            }
                        }
                    }
                }
                
                // Lista de ejercicios (Legacy view)
                routine?.let { routineWithExercises ->
                    if (routineWithExercises.routineExercises.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                                    onDeleteSet = { set ->
                                        viewModel.deleteSet(set)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Overlay de descanso (Solo en Focus Mode)
        if (isFocusMode && isResting) {
            RestTimerOverlay(
                secondsRemaining = restTimerSeconds,
                totalSeconds = viewModel.restTimerTotalSeconds.collectAsState().value,
                onSkip = { viewModel.skipRest() },
                onAdd30s = { viewModel.addRestTime(30) },
                voiceCoach = viewModel.voiceCoach
            )
        }
        
        // Diálogo para agregar set
        if (showSetDialog && selectedExerciseId != null) {
            AddSetDialog(
                exerciseId = selectedExerciseId!!,
                setNumber = viewModel.getSetsForExercise(selectedExerciseId!!).size + 1,
                onDismiss = { showSetDialog = false },
                onConfirm = { weight, reps, rir, rpe ->
                    viewModel.logSet(selectedExerciseId!!, viewModel.getSetsForExercise(selectedExerciseId!!).size + 1, weight, reps, rir, rpe)
                    showSetDialog = false
                }
            )
        }
        
        // Diálogo de completar
        if (showCompleteDialog) {
            AlertDialog(
                onDismissRequest = { showCompleteDialog = false },
                title = { Text("Finalizar Entrenamiento") },
                text = { Text("¿Has completado tu entrenamiento?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.completeWorkout()
                        showCompleteDialog = false
                    }) {
                        Text("Finalizar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCompleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo de cancelar
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Cancelar Entrenamiento") },
                text = { Text("¿Seguro que quieres cancelar? Se perderán todos los datos registrados.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.cancelWorkout()
                            showCancelDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancelar Entrenamiento")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Volver")
                    }
                }
            )
        }
    }
}

/**
 * Card de ejercicio en workout
 */
@Composable
fun ExerciseWorkoutCard(
    exerciseName: String,
    targetSets: Int,
    targetReps: String,
    completedSets: List<ExerciseSetEntity>,
    isCurrentExercise: Boolean,
    onAddSet: () -> Unit,
    onDeleteSet: (ExerciseSetEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentExercise) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentExercise) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre y objetivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$targetSets sets × $targetReps reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Progreso
                Text(
                    text = "${completedSets.size}/$targetSets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (completedSets.size >= targetSets) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Sets completados
            if (completedSets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    completedSets.forEach { set ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Set ${set.setNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${set.weightUsed ?: "—"}kg × ${set.repsCompleted} reps${if (set.rir != null) " (RIR ${set.rir})" else ""}${if (set.rpe != null) " (RPE ${set.rpe})" else ""}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = { onDeleteSet(set) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Botón agregar set
            if (completedSets.size < targetSets) {
                Button(
                    onClick = onAddSet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Set ${completedSets.size + 1}")
                }
            }
        }
    }
}

/**
 * Diálogo para agregar un set
 */
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
    var rpe by remember { mutableStateOf(8f) } // Default RPE 8
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Set $setNumber") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Repeticiones") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rir,
                    onValueChange = { rir = it },
                    label = { Text("RIR (Reps in Reserve)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // RPE Slider
                Column {
                    Text(
                        text = "Esfuerzo (RPE): ${rpe.toInt()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = rpe,
                        onValueChange = { rpe = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fácil", style = MaterialTheme.typography.bodySmall)
                        Text("Fallo", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        weight.toDoubleOrNull(),
                        reps.toIntOrNull(),
                        rir.toIntOrNull(),
                        rpe.toInt()
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
