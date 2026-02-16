package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.dao.WorkoutDao
import com.gymcompanion.app.data.local.entity.TrainingPhaseEntity
import com.gymcompanion.app.data.local.dao.TrainingPhaseDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de Semanas de Deload (Descarga)
 * 
 * Implementa:
 * - Detección automática de necesidad de deload (cada 3-4 semanas)
 * - Cálculo de volumen reducido para semana de descarga
 * - Tracking de semanas consecutivas de entrenamiento
 */
@Singleton
class DeloadManager @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val trainingPhaseDao: TrainingPhaseDao
) {

    companion object {
        const val WEEKS_BEFORE_DELOAD = 4 // Deload cada 4 semanas
        const val DELOAD_VOLUME_REDUCTION = 0.50 // Reducir volumen al 50%
        const val DELOAD_WEIGHT_REDUCTION = 0.60 // Reducir peso al 60%
        const val MIN_WORKOUTS_PER_WEEK = 2 // Mínimo 2 entrenamientos para contar como semana activa
        const val MILLIS_PER_WEEK = 7L * 24 * 60 * 60 * 1000
    }

    /**
     * Determina si el usuario necesita una semana de deload
     */
    suspend fun shouldDeload(userId: Long): DeloadRecommendation {
        val consecutiveWeeks = calculateConsecutiveTrainingWeeks(userId)
        val activePhase = getActivePhase(userId)

        // Si ya está en deload, no necesita otro
        if (activePhase?.phaseName == "Deload") {
            return DeloadRecommendation(
                shouldDeload = false,
                reason = "Ya estás en semana de descarga",
                consecutiveWeeks = consecutiveWeeks,
                weeksUntilDeload = 0
            )
        }

        // Verificar si han pasado suficientes semanas
        val needsDeload = consecutiveWeeks >= WEEKS_BEFORE_DELOAD
        val weeksRemaining = (WEEKS_BEFORE_DELOAD - consecutiveWeeks).coerceAtLeast(0)

        return DeloadRecommendation(
            shouldDeload = needsDeload,
            reason = if (needsDeload) {
                "Llevas $consecutiveWeeks semanas de entrenamiento consecutivo. Es hora de una semana de descarga para optimizar la recuperación."
            } else {
                "Faltan $weeksRemaining semanas para tu próxima descarga programada."
            },
            consecutiveWeeks = consecutiveWeeks,
            weeksUntilDeload = weeksRemaining
        )
    }

    /**
     * Calcula el volumen de deload para un ejercicio
     * 
     * @param currentSets Series actuales
     * @param currentWeight Peso actual en lbs
     * @return Par (sets reducidos, peso reducido)
     */
    fun calculateDeloadVolume(currentSets: Int, currentWeight: Double): Pair<Int, Double> {
        val reducedSets = (currentSets * DELOAD_VOLUME_REDUCTION).toInt().coerceAtLeast(2)
        val reducedWeight = currentWeight * DELOAD_WEIGHT_REDUCTION
        return Pair(reducedSets, reducedWeight)
    }

    /**
     * Calcula semanas de entrenamiento consecutivas
     */
    private suspend fun calculateConsecutiveTrainingWeeks(userId: Long): Int {
        val now = System.currentTimeMillis()
        var consecutiveWeeks = 0
        
        // Revisar las últimas 8 semanas hacia atrás
        for (weekOffset in 0..7) {
            val weekEnd = now - (weekOffset * MILLIS_PER_WEEK)
            val weekStart = weekEnd - MILLIS_PER_WEEK
            
            try {
                val sessions = workoutDao.getWorkoutSessionsInDateRange(
                    userId, weekStart, weekEnd
                ).first()
                
                val completedSessions = sessions.count { it.isCompleted }
                if (completedSessions >= MIN_WORKOUTS_PER_WEEK) {
                    consecutiveWeeks++
                } else {
                    break // Romper en la primera semana sin suficientes entrenamientos
                }
            } catch (e: Exception) {
                break
            }
        }
        
        return consecutiveWeeks
    }

    /**
     * Obtiene la fase de entrenamiento activa
     */
    private suspend fun getActivePhase(userId: Long): TrainingPhaseEntity? {
        return try {
            trainingPhaseDao.getActivePhase(userId).first()
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Recomendación de deload
 */
data class DeloadRecommendation(
    val shouldDeload: Boolean,
    val reason: String,
    val consecutiveWeeks: Int,
    val weeksUntilDeload: Int
)
