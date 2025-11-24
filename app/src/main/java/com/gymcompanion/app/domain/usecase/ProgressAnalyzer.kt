package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.domain.model.FitnessGoal
import com.gymcompanion.app.domain.repository.WorkoutSessionRepository
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Analizador de progreso para detectar estancamiento y calcular mejoras
 */
class ProgressAnalyzer @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository
) {

    /**
     * Calcula el 1RM estimado (Una Repetición Máxima) usando la fórmula de Epley
     */
    fun calculateEstimated1RM(weight: Double, reps: Int): Double {
        if (reps == 1) return weight
        // Fórmula de Epley: 1RM = Peso * (1 + Reps/30)
        return weight * (1 + reps / 30.0)
    }

    /**
     * Analiza si el usuario está estancado en un ejercicio
     * Se considera estancamiento si no ha habido mejora en 1RM en las últimas 3 sesiones
     */
    suspend fun isStalled(exerciseId: Long, userId: Long): Boolean {
        // Obtener historial reciente del ejercicio (últimas 5 sesiones)
        val history = workoutSessionRepository.getExerciseHistory(userId, exerciseId, limit = 5)
        
        if (history.size < 3) return false // No hay suficientes datos para determinar estancamiento

        // Calcular 1RM máximo por sesión
        val oneRMs = history.map { session ->
            session.sets.maxOfOrNull { set -> 
                calculateEstimated1RM(set.weightUsed ?: 0.0, set.repsCompleted) 
            } ?: 0.0
        }

        // Verificar si hay mejora en las últimas 3 sesiones (comparando con la más antigua de las 3)
        // Si el 1RM más reciente es menor o igual al de hace 2 sesiones, y el del medio tampoco fue mejor...
        // Simplificación: Si la tendencia es plana o negativa
        val recent = oneRMs.take(3) // [Más reciente, Anterior, Tras-anterior]
        
        // Si el más reciente no es mayor que el promedio de los anteriores por un margen mínimo (e.g. 2.5%)
        val current = recent[0]
        val previous = recent.drop(1).average()
        
        return current <= previous * 1.01 // Margen de error del 1%
    }

    /**
     * Sugiere el peso para la próxima sesión basado en el rendimiento anterior y el objetivo
     */
    suspend fun suggestNextWeight(
        exerciseId: Long, 
        userId: Long, 
        targetReps: Int, 
        targetRPE: Int = 8
    ): Double {
        val history = workoutSessionRepository.getExerciseHistory(userId, exerciseId, limit = 1)
        val lastSession = history.firstOrNull() ?: return 0.0 // Sin historial, retornar 0 (usar default)

        val bestSet = lastSession.sets.maxByOrNull { calculateEstimated1RM(it.weightUsed ?: 0.0, it.repsCompleted) }
        val lastWeight = bestSet?.weightUsed ?: 0.0
        val lastReps = bestSet?.repsCompleted ?: 0
        val lastRPE = bestSet?.rpe ?: 8 // Asumir RPE 8 si no se registró

        // Si completó más reps de las objetivo o el RPE fue bajo, subir peso
        if (lastReps > targetReps || lastRPE < targetRPE) {
            // Sobrecarga progresiva: +2.5% a +5%
            return (lastWeight * 1.025).roundToStep(2.5)
        }
        
        // Si no llegó a las reps o RPE muy alto, mantener o bajar
        if (lastReps < targetReps - 2 || lastRPE > targetRPE + 1) {
             return (lastWeight * 0.9).roundToStep(2.5)
        }

        return lastWeight
    }

    private fun Double.roundToStep(step: Double): Double {
        return (this / step).roundToInt() * step
    }
}
