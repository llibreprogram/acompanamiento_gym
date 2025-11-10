package com.gymcompanion.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.domain.repository.RoutineRepository
import com.gymcompanion.app.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * ViewModel para la pantalla Home
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutSessionRepository: WorkoutSessionRepository
) : ViewModel() {
    
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
     * Formatea el volumen en toneladas
     */
    fun formatVolume(volume: Double): String {
        return if (volume >= 1000) {
            String.format("%.1ft", volume / 1000)
        } else {
            String.format("%.0fkg", volume)
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
