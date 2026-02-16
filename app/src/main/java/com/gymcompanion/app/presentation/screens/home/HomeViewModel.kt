package com.gymcompanion.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.domain.repository.RoutineRepository
import com.gymcompanion.app.domain.repository.WorkoutSessionRepository
import com.gymcompanion.app.domain.repository.UserRepository
import com.gymcompanion.app.domain.usecase.TrendAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel para la pantalla Home
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val userRepository: UserRepository,
    private val trendAnalyzer: TrendAnalyzer,
    private val healthConnectManager: com.gymcompanion.app.domain.usecase.HealthConnectManager
) : ViewModel() {
    
    // Estado de refresco
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Nombre del usuario
    val userName: StateFlow<String?> = userRepository.getCurrentUser()
        .map { it?.name }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Rutinas del día actual
    val todayRoutines: StateFlow<List<RoutineWithExercises>> = getCurrentDayOfWeek()
        .flatMapLatest { day ->
            routineRepository.getRoutinesForDay(day)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Última sesión
    val lastSession: StateFlow<WorkoutSessionEntity?> = workoutSessionRepository.getLastSession()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Sesiones de esta semana
    val sessionsThisWeek: StateFlow<List<WorkoutSessionEntity>> = workoutSessionRepository.getSessionsThisWeek()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Estadísticas calculadas
    val weeklyStats: StateFlow<WeeklyStats> = sessionsThisWeek.map { sessions ->
        WeeklyStats(
            totalWorkouts = sessions.size,
            totalTime = calculateTotalTime(sessions),
            totalVolume = sessions.sumOf { it.totalVolume }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeeklyStats(0, 0L, 0.0)
    )
    
    // Datos diarios para el gráfico semanal
    val dailyWorkoutData: StateFlow<List<Pair<String, Float>>> = sessionsThisWeek.map { sessions ->
        calculateDailyWorkoutPercentages(sessions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Health Connect data
    private val _healthData = MutableStateFlow<com.gymcompanion.app.domain.model.HealthData?>(null)
    val healthData: StateFlow<com.gymcompanion.app.domain.model.HealthData?> = _healthData.asStateFlow()
    
    private val _healthConnectAvailable = MutableStateFlow(false)
    val healthConnectAvailable: StateFlow<Boolean> = _healthConnectAvailable.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Migrate existing routines to populate daysOfWeek field
            migrateRoutineDaysIfNeeded()
            
            // Check Health Connect availability and load data
            _healthConnectAvailable.value = healthConnectManager.isAvailable()
            if (_healthConnectAvailable.value && healthConnectManager.hasAllPermissions()) {
                loadHealthData()
            }
        }
    }
    
    /**
     * Load health data from Health Connect
     */
    fun loadHealthData() {
        viewModelScope.launch {
            try {
                val data = healthConnectManager.getHealthData()
                _healthData.value = data
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading health data", e)
            }
        }
    }
    
    /**
     * Obtiene el día de la semana actual
     */
    private fun getCurrentDayOfWeek(): Flow<String> = flow {
        val calendar = java.util.Calendar.getInstance()
        val day = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Lunes"
            java.util.Calendar.TUESDAY -> "Martes"
            java.util.Calendar.WEDNESDAY -> "Miércoles"
            java.util.Calendar.THURSDAY -> "Jueves"
            java.util.Calendar.FRIDAY -> "Viernes"
            java.util.Calendar.SATURDAY -> "Sábado"
            java.util.Calendar.SUNDAY -> "Domingo"
            else -> "Lunes"
        }
        emit(day)
    }
    
    /**
     * Calcula el tiempo total de las sesiones en segundos
     */
    private fun calculateTotalTime(sessions: List<WorkoutSessionEntity>): Long {
        return sessions.sumOf { session ->
            if (session.endTime != null) {
                (session.endTime!! - session.startTime) / 1000
            } else {
                0L
            }
        }
    }
    
    /**
     * Formatea el tiempo en horas y minutos
     */
    fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
    
    /**
     * Calcula los porcentajes de entrenamiento por día de la semana
     */
    private fun calculateDailyWorkoutPercentages(sessions: List<WorkoutSessionEntity>): List<Pair<String, Float>> {
        val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        
        // Agrupar sesiones por día de la semana
        val sessionsByDay = sessions.groupBy { session ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = session.startTime
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
        }
        
        // Encontrar el volumen máximo para normalizar
        val maxVolume = sessions.maxOfOrNull { it.totalVolume } ?: 1.0
        
        // Calcular porcentaje para cada día (0-100)
        return daysOfWeek.mapIndexed { index, day ->
            val dayWorkouts = sessionsByDay[index] ?: emptyList()
            val percentage = if (dayWorkouts.isNotEmpty()) {
                val totalVolume = dayWorkouts.sumOf { it.totalVolume }
                // Normalizar al volumen máximo de la semana
                ((totalVolume / maxVolume) * 100).toFloat()
            } else {
                0f
            }
            day to percentage
        }
    }
    
    /**
     * Formatea el volumen en toneladas
     */
    fun formatVolume(volume: Double): String {
        return if (volume >= 1000) {
            String.format("%.1ft", volume / 1000)
        } else {
            String.format("%.0flbs", volume)
        }
    }
    
    // Análisis de tendencias
    val volumeTrend: StateFlow<com.gymcompanion.app.domain.usecase.TrendResult?> = combine(
        sessionsThisWeek,
        workoutSessionRepository.getSessionsLastWeek()
    ) { currentWeek, lastWeek ->
        if (currentWeek.isNotEmpty() && lastWeek.isNotEmpty()) {
            trendAnalyzer.analyzeTrend(
                currentPeriod = currentWeek,
                previousPeriod = lastWeek,
                metric = com.gymcompanion.app.domain.usecase.MetricType.VOLUME
            )
        } else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    val frequencyTrend: StateFlow<com.gymcompanion.app.domain.usecase.TrendResult?> = combine(
        sessionsThisWeek,
        workoutSessionRepository.getSessionsLastWeek()
    ) { currentWeek, lastWeek ->
        if (currentWeek.isNotEmpty() && lastWeek.isNotEmpty()) {
            trendAnalyzer.analyzeTrend(
                currentPeriod = currentWeek,
                previousPeriod = lastWeek,
                metric = com.gymcompanion.app.domain.usecase.MetricType.FREQUENCY
            )
        } else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    // Riesgo de sobreentrenamiento
    val overtrainingRisk: StateFlow<com.gymcompanion.app.domain.usecase.OvertrainingRisk?> = 
        workoutSessionRepository.getRecentSessions(14)
            .map { sessions ->
                if (sessions.isNotEmpty()) {
                    trendAnalyzer.detectOvertraining(sessions)
                } else null
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    
    // Predicción de próxima sesión
    val sessionPrediction: StateFlow<com.gymcompanion.app.domain.usecase.SessionPrediction?> = 
        workoutSessionRepository.getRecentSessions(10)
            .map { sessions ->
                if (sessions.size >= 3) {
                    trendAnalyzer.predictNextSession(sessions)
                } else null
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    
    /**
     * Migrates existing routines to populate daysOfWeek field
     * Extracts day from routine name (e.g., "Pull - Viernes" -> "Viernes")
     */
    private suspend fun migrateRoutineDaysIfNeeded() {
        try {
            val allRoutines = routineRepository.getAllRoutines().first()
            
            allRoutines.forEach { routineWithExercises ->
                val routine = routineWithExercises.routine
                
                // Check if daysOfWeek is empty or blank
                if (routine.daysOfWeek == "[]" || routine.daysOfWeek.isBlank()) {
                    // Extract day from name: "Pull - Viernes" -> "Viernes"
                    val parts = routine.name.split(" - ")
                    if (parts.size >= 2) {
                        val day = parts.last().trim()
                        val updatedRoutine = routine.copy(
                            daysOfWeek = "[\"$day\"]"
                        )
                        routineRepository.updateRoutine(updatedRoutine)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail - migration is best effort
        }
    }
    
    /**
     * Refresca los datos de la pantalla Home
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Simular un pequeño delay para mejor UX
                kotlinx.coroutines.delay(500)
                // Los flows se actualizarán automáticamente
                // No necesitamos hacer nada más porque los StateFlows
                // están conectados a los repositorios que observan la DB
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

/**
 * Estadísticas semanales
 */
data class WeeklyStats(
    val totalWorkouts: Int,
    val totalTime: Long,
    val totalVolume: Double
)
