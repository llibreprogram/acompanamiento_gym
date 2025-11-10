package com.gymcompanion.app.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.data.local.entity.BodyMetricsEntity
import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.domain.repository.BodyMetricsRepository
import com.gymcompanion.app.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel para la pantalla de progreso
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val bodyMetricsRepository: BodyMetricsRepository,
    private val workoutSessionRepository: WorkoutSessionRepository
) : ViewModel() {
    
    // Métricas corporales del último mes
    val recentBodyMetrics: StateFlow<List<BodyMetricsEntity>> = getLastMonthRange()
        .flatMapLatest { (start, end) ->
            bodyMetricsRepository.getMetricsInDateRange(start, end)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Sesiones del último mes
    val recentSessions: StateFlow<List<WorkoutSessionEntity>> = getLastMonthRange()
        .flatMapLatest { (start, end) ->
            workoutSessionRepository.getSessionsByDateRange(start, end)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Todas las sesiones para estadísticas totales
    val allSessions: StateFlow<List<WorkoutSessionEntity>> = workoutSessionRepository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Estadísticas totales
    val totalStats: StateFlow<TotalStats> = allSessions.map { sessions ->
        val completedSessions = sessions.filter { it.endTime != null }
        TotalStats(
            totalWorkouts = completedSessions.size,
            totalVolume = completedSessions.sumOf { it.totalVolume },
            totalTime = completedSessions.sumOf { session ->
                if (session.endTime != null) {
                    (session.endTime!! - session.startTime) / 1000
                } else 0L
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TotalStats(0, 0.0, 0L)
    )
    
    /**
     * Obtiene el rango del último mes
     */
    private fun getLastMonthRange(): Flow<Pair<Long, Long>> = flow {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, -1)
        val start = calendar.timeInMillis
        
        emit(Pair(start, end))
    }
    
    /**
     * Prepara datos para el gráfico de peso
     */
    fun getWeightChartData(): List<Pair<Long, Float>> {
        return recentBodyMetrics.value.map { metrics ->
            Pair(metrics.date, metrics.weight.toFloat())
        }.sortedBy { it.first }
    }
    
    /**
     * Prepara datos para el gráfico de volumen
     */
    fun getVolumeChartData(): List<Pair<Long, Float>> {
        return recentSessions.value
            .filter { it.endTime != null }
            .map { session ->
                Pair(session.startTime, session.totalVolume.toFloat())
            }
            .sortedBy { it.first }
    }
    
    /**
     * Prepara datos para el gráfico de IMC
     */
    fun getBmiChartData(): List<Pair<Long, Float>> {
        return recentBodyMetrics.value.map { metrics ->
            val bmi = BodyMetricsEntity.calculateBMI(metrics.weight, metrics.height)
            Pair(metrics.date, bmi.toFloat())
        }.sortedBy { it.first }
    }
    
    /**
     * Formatea el volumen
     */
    fun formatVolume(volume: Double): String {
        return if (volume >= 1000) {
            String.format("%.1ft", volume / 1000)
        } else {
            String.format("%.0fkg", volume)
        }
    }
    
    /**
     * Formatea el tiempo
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
     * Calcula el cambio de peso
     */
    fun getWeightChange(): WeightChange? {
        val metrics = recentBodyMetrics.value
        if (metrics.size < 2) return null
        
        val sorted = metrics.sortedBy { it.date }
        val oldest = sorted.first().weight
        val newest = sorted.last().weight
        val change = newest - oldest
        
        return WeightChange(
            change = change,
            percentage = (change / oldest) * 100
        )
    }
}

/**
 * Estadísticas totales
 */
data class TotalStats(
    val totalWorkouts: Int,
    val totalVolume: Double,
    val totalTime: Long
)

/**
 * Cambio de peso
 */
data class WeightChange(
    val change: Double,
    val percentage: Double
)
