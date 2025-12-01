package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import javax.inject.Inject

/**
 * Analiza tendencias en el rendimiento del usuario
 * Calcula si está mejorando, declinando o estable en diferentes métricas
 */
class TrendAnalyzer @Inject constructor() {
    
    /**
     * Analiza la tendencia de una métrica comparando dos períodos
     * @return TrendResult con dirección y porcentaje de cambio
     */
    fun analyzeTrend(
        currentPeriod: List<WorkoutSessionEntity>,
        previousPeriod: List<WorkoutSessionEntity>,
        metric: MetricType
    ): TrendResult {
        val currentValue = calculateMetric(currentPeriod, metric)
        val previousValue = calculateMetric(previousPeriod, metric)
        
        if (previousValue == 0.0) {
            return TrendResult(
                direction = TrendDirection.STABLE,
                percentageChange = 0.0,
                currentValue = currentValue,
                previousValue = previousValue
            )
        }
        
        val percentageChange = ((currentValue - previousValue) / previousValue) * 100
        val direction = when {
            percentageChange > 5.0 -> TrendDirection.IMPROVING
            percentageChange < -5.0 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
        
        return TrendResult(
            direction = direction,
            percentageChange = percentageChange,
            currentValue = currentValue,
            previousValue = previousValue
        )
    }
    
    /**
     * Calcula el valor de una métrica para un conjunto de sesiones
     */
    private fun calculateMetric(sessions: List<WorkoutSessionEntity>, metric: MetricType): Double {
        if (sessions.isEmpty()) return 0.0
        
        return when (metric) {
            MetricType.VOLUME -> sessions.sumOf { it.totalVolume }
            MetricType.FREQUENCY -> sessions.size.toDouble()
            MetricType.DURATION -> {
                sessions.sumOf { session ->
                    if (session.endTime != null) {
                        (session.endTime!! - session.startTime) / 1000.0
                    } else 0.0
                } / sessions.size // Promedio de duración
            }
            MetricType.INTENSITY -> {
                // Intensidad = Volumen / Duración (kg/segundo)
                val totalVolume = sessions.sumOf { it.totalVolume }
                val totalDuration = sessions.sumOf { session ->
                    if (session.endTime != null) {
                        (session.endTime!! - session.startTime) / 1000.0
                    } else 0.0
                }
                if (totalDuration > 0) totalVolume / totalDuration else 0.0
            }
        }
    }
    
    /**
     * Genera un mensaje descriptivo de la tendencia
     */
    fun getTrendMessage(result: TrendResult, metric: MetricType): String {
        val metricName = when (metric) {
            MetricType.VOLUME -> "volumen"
            MetricType.FREQUENCY -> "frecuencia"
            MetricType.DURATION -> "duración"
            MetricType.INTENSITY -> "intensidad"
        }
        
        return when (result.direction) {
            TrendDirection.IMPROVING -> {
                val change = String.format("%.1f", kotlin.math.abs(result.percentageChange))
                "Tu $metricName ha mejorado un $change% 📈"
            }
            TrendDirection.DECLINING -> {
                val change = String.format("%.1f", kotlin.math.abs(result.percentageChange))
                "Tu $metricName ha bajado un $change% 📉"
            }
            TrendDirection.STABLE -> "Tu $metricName se mantiene estable ➡️"
        }
    }
    
    /**
     * Detecta si el usuario está en riesgo de sobreentrenamiento
     */
    fun detectOvertraining(recentSessions: List<WorkoutSessionEntity>): OvertrainingRisk {
        if (recentSessions.size < 7) {
            return OvertrainingRisk(
                level = RiskLevel.LOW,
                message = "Datos insuficientes para análisis"
            )
        }
        
        // Criterios de sobreentrenamiento:
        // 1. Más de 6 días consecutivos sin descanso
        // 2. Volumen muy alto comparado con el promedio
        // 3. Duración de sesiones muy larga
        
        val consecutiveDays = countConsecutiveDays(recentSessions)
        val avgVolume = recentSessions.map { it.totalVolume }.average()
        val lastWeekVolume = recentSessions.takeLast(7).sumOf { it.totalVolume }
        
        val riskLevel = when {
            consecutiveDays >= 7 -> RiskLevel.HIGH
            consecutiveDays >= 5 && lastWeekVolume > avgVolume * 1.5 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        
        val message = when (riskLevel) {
            RiskLevel.HIGH -> "⚠️ Considera tomar un día de descanso. Has entrenado $consecutiveDays días seguidos."
            RiskLevel.MEDIUM -> "💡 Tu volumen está alto. Considera reducir la intensidad."
            RiskLevel.LOW -> "✅ Tu ritmo de entrenamiento es saludable."
        }
        
        return OvertrainingRisk(level = riskLevel, message = message)
    }
    
    /**
     * Cuenta días consecutivos de entrenamiento
     */
    private fun countConsecutiveDays(sessions: List<WorkoutSessionEntity>): Int {
        if (sessions.isEmpty()) return 0
        
        val sortedSessions = sessions.sortedByDescending { it.startTime }
        var consecutiveDays = 1
        
        for (i in 0 until sortedSessions.size - 1) {
            val current = java.util.Calendar.getInstance().apply {
                timeInMillis = sortedSessions[i].startTime
            }
            val next = java.util.Calendar.getInstance().apply {
                timeInMillis = sortedSessions[i + 1].startTime
            }
            
            // Verificar si son días consecutivos
            val dayDiff = ((current.timeInMillis - next.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (dayDiff == 1) {
                consecutiveDays++
            } else {
                break
            }
        }
        
        return consecutiveDays
    }
    
    /**
     * Predice el rendimiento de la próxima sesión basándose en tendencias
     */
    fun predictNextSession(recentSessions: List<WorkoutSessionEntity>): SessionPrediction {
        if (recentSessions.size < 3) {
            return SessionPrediction(
                predictedVolume = 0.0,
                confidence = 0.0,
                recommendation = "Necesitas más datos para predicciones precisas"
            )
        }
        
        val volumeTrend = analyzeTrend(
            currentPeriod = recentSessions.takeLast(3),
            previousPeriod = recentSessions.dropLast(3).takeLast(3),
            metric = MetricType.VOLUME
        )
        
        val avgVolume = recentSessions.takeLast(5).map { it.totalVolume }.average()
        val predictedVolume = when (volumeTrend.direction) {
            TrendDirection.IMPROVING -> avgVolume * 1.05
            TrendDirection.DECLINING -> avgVolume * 0.95
            TrendDirection.STABLE -> avgVolume
        }
        
        val confidence = if (recentSessions.size >= 10) 0.8 else 0.6
        
        val recommendation = when {
            volumeTrend.direction == TrendDirection.IMPROVING -> 
                "¡Vas muy bien! Intenta superar ${String.format("%.0f", predictedVolume)}kg de volumen."
            volumeTrend.direction == TrendDirection.DECLINING -> 
                "Enfócate en la calidad. Apunta a ${String.format("%.0f", predictedVolume)}kg."
            else -> 
                "Mantén el ritmo. Objetivo: ${String.format("%.0f", predictedVolume)}kg."
        }
        
        return SessionPrediction(
            predictedVolume = predictedVolume,
            confidence = confidence,
            recommendation = recommendation
        )
    }
}

/**
 * Tipos de métricas analizables
 */
enum class MetricType {
    VOLUME,      // Volumen total (kg)
    FREQUENCY,   // Número de entrenamientos
    DURATION,    // Duración promedio (segundos)
    INTENSITY    // Volumen por segundo
}

/**
 * Dirección de la tendencia
 */
enum class TrendDirection {
    IMPROVING,   // Mejorando (↑)
    DECLINING,   // Declinando (↓)
    STABLE       // Estable (→)
}

/**
 * Resultado del análisis de tendencia
 */
data class TrendResult(
    val direction: TrendDirection,
    val percentageChange: Double,
    val currentValue: Double,
    val previousValue: Double
)

/**
 * Nivel de riesgo de sobreentrenamiento
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Riesgo de sobreentrenamiento
 */
data class OvertrainingRisk(
    val level: RiskLevel,
    val message: String
)

/**
 * Predicción de la próxima sesión
 */
data class SessionPrediction(
    val predictedVolume: Double,
    val confidence: Double,  // 0.0 - 1.0
    val recommendation: String
)
