package com.gymcompanion.app.presentation.screens.workout

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.domain.repository.RoutineRepository
import com.gymcompanion.app.domain.repository.WorkoutSessionRepository
import com.gymcompanion.app.presentation.service.WorkoutNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la sesión de entrenamiento activa
 */
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val routineRepository: RoutineRepository,
    private val rpeAnalyzer: com.gymcompanion.app.domain.usecase.RPEAnalyzer,
    val voiceCoach: com.gymcompanion.app.domain.usecase.VoiceCoachManager,
    private val haptics: com.gymcompanion.app.domain.usecase.HapticFeedbackManager,
    val plateCalculator: com.gymcompanion.app.domain.usecase.PlateCalculator
) : ViewModel() {
    
    // Sesión actual
    private val _currentSessionId = MutableStateFlow<Long?>(null)
    
    // Rutina cargada
    private val _routine = MutableStateFlow<RoutineWithExercises?>(null)
    val routine: StateFlow<RoutineWithExercises?> = _routine.asStateFlow()
    
    // Navigation state to prevent rapid clicks
    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()
    
    // Sets registrados en la sesión
    val sessionSets: StateFlow<List<ExerciseSetEntity>> = _currentSessionId
        .filterNotNull()
        .flatMapLatest { sessionId ->
            workoutSessionRepository.getSessionSets(sessionId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Índice del ejercicio actual
    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()
    
    // Timer
    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()
    
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()
    
    // Rest timer
    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()
    
    private val _restTimerTotalSeconds = MutableStateFlow(0)
    val restTimerTotalSeconds: StateFlow<Int> = _restTimerTotalSeconds.asStateFlow()
    
    private val _isResting = MutableStateFlow(false)
    val isResting: StateFlow<Boolean> = _isResting.asStateFlow()
    
    // Estado de UI
    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
    
    // Feedback de RPE
    private val _rpeFeedback = MutableSharedFlow<String>()
    val rpeFeedback: SharedFlow<String> = _rpeFeedback.asSharedFlow()
    
    // Tiempo de inicio
    private var sessionStartTime: Long = 0
    
    // TODO: Fix NaN issue in notification updates
    /*
    init {
        // Observar cambios en ejercicio y timer para actualizar notificación
        viewModelScope.launch {
            combine(
                currentExerciseIndex,
                timerSeconds,
                routine
            ) { exerciseIndex, timer, routineData ->
                Triple(exerciseIndex, timer, routineData)
            }.collect { (exerciseIndex, timer, routineData) ->
                routineData?.let { routine ->
                    val currentExercise = routine.routineExercises.getOrNull(exerciseIndex)
                    currentExercise?.let { exerciseWithDetails ->
                        val exerciseName = exerciseWithDetails.exercise.name
                        val formattedTime = formatTime(timer)
                        WorkoutNotificationService.updateNotification(context, exerciseName, formattedTime)
                    }
                }
            }
        }
    }
    */
    
    /**
     * Inicia una nueva sesión de entrenamiento
     */
    fun startWorkoutSession(routineId: Long) {
        // ⚡ FIX: Prevent reset if session is already active for this routine
        if (_currentSessionId.value != null && 
            _routine.value?.routine?.id == routineId && 
            _uiState.value !is WorkoutUiState.Completed && 
            _uiState.value !is WorkoutUiState.Cancelled) {
            return
        }

        viewModelScope.launch {
            try {
                // Cargar la rutina
                routineRepository.getRoutineById(routineId).first()?.let { routineWithExercises ->
                    Log.d("WorkoutViewModel", "Loaded routine with ${routineWithExercises.routineExercises.size} exercises")
                    _routine.value = routineWithExercises

                    
                    // Crear nueva sesión
                    sessionStartTime = System.currentTimeMillis()
                    val session = WorkoutSessionEntity(
                        userId = 1L, // TODO: Get actual user ID
                        routineId = routineId,
                        startTime = sessionStartTime,
                        endTime = null,
                        totalVolume = 0.0,
                        notes = ""
                    )
                    val sessionId = workoutSessionRepository.insertSession(session)
                    _currentSessionId.value = sessionId
                    
                    // Iniciar timer
                    startTimer()
                    
                    // TODO: Fix notification service
                    // Iniciar servicio de notificación
                    // val firstExercise = routineWithExercises.routineExercises.firstOrNull()?.exercise?.name ?: "Workout"
                    // WorkoutNotificationService.startService(context, routineId, firstExercise, "00:00")
                    
                    _uiState.value = WorkoutUiState.Active
                }
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }
    
    /**
     * Registra un set completado
     */
    fun logSet(
        exerciseId: Long,
        setNumber: Int,
        weight: Double?,
        reps: Int?,
        rir: Int?,
        rpe: Int?,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _currentSessionId.value?.let { sessionId ->
                try {
                    val exerciseSet = ExerciseSetEntity(
                        workoutSessionId = sessionId,
                        exerciseId = exerciseId,
                        setNumber = setNumber,
                        weightUsed = weight,
                        repsCompleted = reps ?: 0,
                        rir = rir,
                        rpe = rpe,
                        notes = notes,
                        performedAt = System.currentTimeMillis()
                    )
                    workoutSessionRepository.insertExerciseSet(exerciseSet)
                    
                    // Feedback háptico y de voz en background para no bloquear UI
                    launch {
                        haptics.vibrateSuccess()
                        voiceCoach.announceSetComplete()
                    }
                    
                    // Analizar RPE y generar feedback
                    rpe?.let {
                        launch {
                            val feedback = rpeAnalyzer.analyzeSet(it)
                            if (feedback.adjustment != com.gymcompanion.app.domain.usecase.RPEAnalyzer.Adjustment.KEEP_SAME) {
                                _rpeFeedback.emit(feedback.message)
                            }
                        }
                    }
                    
                    // Iniciar descanso automático si está configurado
                    val currentRoutine = _routine.value
                    val exercise = currentRoutine?.routineExercises?.find { it.routineExercise.exerciseId == exerciseId }
                    exercise?.routineExercise?.restTimeSeconds?.let { restSeconds ->
                        if (restSeconds > 0) {
                            startRestTimer(restSeconds)
                            launch {
                                voiceCoach.announceRestTimer(restSeconds)
                            }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = WorkoutUiState.Error(e.message ?: "Error al registrar set")
                }
            }
        }
    }
    
    /**
     * Actualiza un set existente
     */
    fun updateSet(exerciseSet: ExerciseSetEntity) {
        viewModelScope.launch {
            try {
                workoutSessionRepository.updateExerciseSet(exerciseSet)
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.message ?: "Error al actualizar set")
            }
        }
    }
    
    /**
     * Elimina un set
     */
    fun deleteSet(exerciseSet: ExerciseSetEntity) {
        viewModelScope.launch {
            try {
                workoutSessionRepository.deleteExerciseSet(exerciseSet)
            } catch (e: Exception) {
                _uiState.value = WorkoutUiState.Error(e.message ?: "Error al eliminar set")
            }
        }
    }
    
    /**
     * Avanza al siguiente ejercicio
     */
    fun nextExercise() {
        if (_isNavigating.value) return // Prevent rapid clicks
        
        val routine = _routine.value ?: return
        if (_currentExerciseIndex.value < routine.routineExercises.size - 1) {
            _isNavigating.value = true
            _currentExerciseIndex.value++
            
            // Reset navigation lock after a short delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(300) // 300ms debounce
                _isNavigating.value = false
            }
        }
    }
    
    /**
     * Retrocede al ejercicio anterior
     */
    fun prevExercise() {
        if (_isNavigating.value) return // Prevent rapid clicks
        
        if (_currentExerciseIndex.value > 0) {
            _isNavigating.value = true
            _currentExerciseIndex.value--
            
            // Reset navigation lock after a short delay
            viewModelScope.launch {
                kotlinx.coroutines.delay(300) // 300ms debounce
                _isNavigating.value = false
            }
        }
    }
    
    /**
     * Finaliza la sesión de entrenamiento
     */
    fun completeWorkout(notes: String = "") {
        viewModelScope.launch {
            _currentSessionId.value?.let { sessionId ->
                try {
                    stopTimer()
                    val endTime = System.currentTimeMillis()
                    
                    // Calcular volumen total
                    val volume = workoutSessionRepository.calculateSessionVolume(sessionId)
                    
                    // Actualizar sesión
                    val session = WorkoutSessionEntity(
                        id = sessionId,
                        userId = 1L, // TODO: Get actual user ID
                        routineId = _routine.value?.routine?.id,
                        startTime = sessionStartTime,
                        endTime = endTime,
                        totalVolume = volume,
                        notes = notes,
                        isCompleted = true
                    )
                    workoutSessionRepository.updateSession(session)
                    
                    // Feedback de finalización
                    haptics.vibrateStrong()
                    voiceCoach.announceWorkoutComplete()
                    
                    // TODO: Fix notification service
                    // Detener servicio de notificación
                    // WorkoutNotificationService.stopService(context)
                    
                    _uiState.value = WorkoutUiState.Completed
                } catch (e: Exception) {
                    _uiState.value = WorkoutUiState.Error(e.message ?: "Error al finalizar sesión")
                }
            }
        }
    }
    
    /**
     * Cancela la sesión de entrenamiento
     */
    fun cancelWorkout() {
        viewModelScope.launch {
            _currentSessionId.value?.let { sessionId ->
                try {
                    stopTimer()
                    // Eliminar la sesión y todos sus sets
                    val session = WorkoutSessionEntity(
                        id = sessionId,
                        userId = 1L, // TODO: Get actual user ID
                        routineId = _routine.value?.routine?.id,
                        startTime = sessionStartTime,
                        endTime = null,
                        totalVolume = 0.0,
                        notes = ""
                    )
                    workoutSessionRepository.deleteSession(session)
                    
                    // TODO: Fix notification service
                    // Detener servicio de notificación
                    // WorkoutNotificationService.stopService(context)
                    
                    _uiState.value = WorkoutUiState.Cancelled
                } catch (e: Exception) {
                    _uiState.value = WorkoutUiState.Error(e.message ?: "Error al cancelar sesión")
                }
            }
        }
    }
    
    /**
     * Inicia el timer de la sesión
     */
    private fun startTimer() {
        _isTimerRunning.value = true
        viewModelScope.launch {
            while (_isTimerRunning.value) {
                kotlinx.coroutines.delay(1000)
                _timerSeconds.value++
            }
        }
    }
    
    /**
     * Detiene el timer de la sesión
     */
    private fun stopTimer() {
        _isTimerRunning.value = false
    }
    
    /**
     * Pausa/reanuda el timer
     */
    fun toggleTimer() {
        if (_isTimerRunning.value) {
            stopTimer()
        } else {
            startTimer()
        }
    }
    
    /**
     * Inicia el timer de descanso
     */
    fun startRestTimer(seconds: Int) {
        _isResting.value = true
        _restTimerSeconds.value = seconds
        _restTimerTotalSeconds.value = seconds // Store total for progress calculation
        
        viewModelScope.launch {
            while (_restTimerSeconds.value > 0 && _isResting.value) {
                kotlinx.coroutines.delay(1000)
                _restTimerSeconds.value--
            }
            if (_restTimerSeconds.value == 0) {
                _isResting.value = false
                // Vibración al completar descanso
                haptics.vibrateTimerFinished()
                voiceCoach.announceRestComplete()
            }
        }
    }
    
    /**
     * Cancela el timer de descanso
     */
    fun skipRest() {
        _isResting.value = false
        _restTimerSeconds.value = 0
    }

    /**
     * Añade tiempo al descanso actual
     */
    fun addRestTime(seconds: Int) {
        if (_isResting.value) {
            _restTimerSeconds.value += seconds
        }
    }
    
    /**
     * Obtiene los sets de un ejercicio específico
     */
    fun getSetsForExercise(exerciseId: Long): List<ExerciseSetEntity> {
        return sessionSets.value.filter { it.exerciseId == exerciseId }
            .sortedBy { it.setNumber }
    }
    
    /**
     * Formatea el tiempo del timer
     */
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
}

/**
 * Estados de UI para workout
 */
sealed class WorkoutUiState {
    object Loading : WorkoutUiState()
    object Active : WorkoutUiState()
    object Completed : WorkoutUiState()
    object Cancelled : WorkoutUiState()
    data class Error(val message: String) : WorkoutUiState()
}
