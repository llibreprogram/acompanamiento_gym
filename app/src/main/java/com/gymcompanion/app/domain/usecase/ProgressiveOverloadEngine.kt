package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Motor de Progresión Automática
 * 
 * Implementa:
 * - Estimación de 1RM (fórmula de Epley)
 * - Progresión automática de peso basada en rendimiento
 * - Cálculo de peso de trabajo según fase de periodización
 * - Auto-regulación por RPE
 */
@Singleton
class ProgressiveOverloadEngine @Inject constructor() {

    companion object {
        // Incrementos en lbs
        const val COMPOUND_INCREMENT = 5.0   // +5 lbs para ejercicios compuestos
        const val ISOLATION_INCREMENT = 2.5  // +2.5 lbs para aislamiento
        const val MAX_WEEKLY_INCREASE_PCT = 0.10 // Máximo 10% por semana (NASM)
        const val DECREASE_PCT = 0.05        // Reducción del 5% si RPE muy alto
        const val LARGE_DECREASE_PCT = 0.10  // Reducción del 10% si falla reps

        // RPE thresholds
        const val RPE_TOO_EASY = 6
        const val RPE_OPTIMAL_MIN = 7
        const val RPE_OPTIMAL_MAX = 8
        const val RPE_TOO_HARD = 9
        const val RPE_MAXIMAL = 10

        // Porcentajes de 1RM por fase
        const val STRENGTH_MIN_PCT = 0.85
        const val STRENGTH_MAX_PCT = 0.90
        const val HYPERTROPHY_MIN_PCT = 0.70
        const val HYPERTROPHY_MAX_PCT = 0.75
        const val ENDURANCE_MIN_PCT = 0.50
        const val ENDURANCE_MAX_PCT = 0.60
        const val DELOAD_PCT = 0.50

        // Sesiones consecutivas completadas para subir peso
        const val SESSIONS_BEFORE_INCREASE = 2
    }

    /**
     * Estima el 1RM usando la fórmula de Epley
     * 1RM = peso × (1 + reps/30)
     */
    fun estimate1RM(weight: Double, reps: Int): Double {
        if (weight <= 0 || reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1.0 + reps.toDouble() / 30.0)
    }

    /**
     * Estima el 1RM a partir de un conjunto de series recientes
     * Usa la mejor serie (mayor 1RM estimado)
     */
    fun estimateBest1RM(recentSets: List<ExerciseSetEntity>): Double {
        return recentSets
            .filter { !it.isWarmUpSet && it.weightUsed != null && it.weightUsed > 0 }
            .maxOfOrNull { estimate1RM(it.weightUsed!!, it.repsCompleted) }
            ?: 0.0
    }

    /**
     * Calcula el peso de trabajo según la fase de periodización
     * 
     * @param oneRepMax El 1RM estimado del ejercicio
     * @param phase Fase actual de entrenamiento
     * @return Par con peso mínimo y máximo del rango recomendado
     */
    fun calculateWorkingWeight(oneRepMax: Double, phase: TrainingPhase): Pair<Double, Double> {
        if (oneRepMax <= 0) return Pair(0.0, 0.0)
        
        return when (phase) {
            TrainingPhase.STRENGTH -> Pair(
                roundToNearestIncrement(oneRepMax * STRENGTH_MIN_PCT),
                roundToNearestIncrement(oneRepMax * STRENGTH_MAX_PCT)
            )
            TrainingPhase.HYPERTROPHY -> Pair(
                roundToNearestIncrement(oneRepMax * HYPERTROPHY_MIN_PCT),
                roundToNearestIncrement(oneRepMax * HYPERTROPHY_MAX_PCT)
            )
            TrainingPhase.ENDURANCE -> Pair(
                roundToNearestIncrement(oneRepMax * ENDURANCE_MIN_PCT),
                roundToNearestIncrement(oneRepMax * ENDURANCE_MAX_PCT)
            )
            TrainingPhase.DELOAD -> Pair(
                roundToNearestIncrement(oneRepMax * DELOAD_PCT),
                roundToNearestIncrement(oneRepMax * DELOAD_PCT)
            )
        }
    }

    /**
     * Calcula el siguiente peso recomendado basado en rendimiento previo
     * 
     * @param recentSets Series recientes del ejercicio (últimas 2 sesiones)
     * @param plannedReps Reps planificadas
     * @param plannedSets Series planificadas
     * @param isCompound true para ejercicios compuestos
     * @return Peso sugerido para la siguiente sesión, o null si no hay datos
     */
    fun calculateNextWeight(
        recentSets: List<ExerciseSetEntity>,
        plannedReps: Int,
        plannedSets: Int,
        isCompound: Boolean
    ): Double? {
        val workingSets = recentSets.filter { !it.isWarmUpSet && !it.isDropSet }
        if (workingSets.isEmpty()) return null

        val lastWeight = workingSets.firstOrNull()?.weightUsed ?: return null
        if (lastWeight <= 0) return null

        // Análisis de rendimiento
        val avgRPE = calculateAverageRPE(workingSets)
        val completionRate = calculateCompletionRate(workingSets, plannedReps, plannedSets)
        val increment = if (isCompound) COMPOUND_INCREMENT else ISOLATION_INCREMENT

        return when {
            // Si RPE es demasiado alto → reducir peso
            avgRPE != null && avgRPE >= RPE_MAXIMAL -> {
                roundToNearestIncrement(lastWeight * (1.0 - DECREASE_PCT))
            }
            // Si RPE es alto pero manejable → mantener
            avgRPE != null && avgRPE >= RPE_TOO_HARD -> {
                lastWeight
            }
            // Si completó todas las reps con RPE ≤ 8 → subir peso
            completionRate >= 1.0 && (avgRPE == null || avgRPE <= RPE_OPTIMAL_MAX) -> {
                val newWeight = lastWeight + increment
                // No exceder 10% de aumento semanal
                val maxAllowed = lastWeight * (1.0 + MAX_WEEKLY_INCREASE_PCT)
                roundToNearestIncrement(minOf(newWeight, maxAllowed))
            }
            // Si RPE es muy bajo → subir más agresivamente
            avgRPE != null && avgRPE < RPE_TOO_EASY && completionRate >= 1.0 -> {
                val newWeight = lastWeight + (increment * 2)
                val maxAllowed = lastWeight * (1.0 + MAX_WEEKLY_INCREASE_PCT)
                roundToNearestIncrement(minOf(newWeight, maxAllowed))
            }
            // Si no completó las reps → reducir peso
            completionRate < 0.8 -> {
                roundToNearestIncrement(lastWeight * (1.0 - LARGE_DECREASE_PCT))
            }
            // Caso por defecto → mantener peso
            else -> lastWeight
        }
    }

    /**
     * Ajusta peso basado en RPE promedio de la sesión
     */
    fun adjustByRPE(currentWeight: Double, averageRPE: Double, isCompound: Boolean): Double {
        val increment = if (isCompound) COMPOUND_INCREMENT else ISOLATION_INCREMENT
        
        return when {
            averageRPE < RPE_TOO_EASY -> {
                // Muy fácil → subir 5-10%
                roundToNearestIncrement(currentWeight + increment * 2)
            }
            averageRPE in RPE_OPTIMAL_MIN.toDouble()..RPE_OPTIMAL_MAX.toDouble() -> {
                // Óptimo → subir normalmente
                roundToNearestIncrement(currentWeight + increment)
            }
            averageRPE >= RPE_TOO_HARD.toDouble() && averageRPE < RPE_MAXIMAL.toDouble() -> {
                // Duro pero manejable → mantener
                currentWeight
            }
            averageRPE >= RPE_MAXIMAL.toDouble() -> {
                // Demasiado duro → bajar 5%
                roundToNearestIncrement(currentWeight * (1.0 - DECREASE_PCT))
            }
            else -> currentWeight
        }
    }

    /**
     * Calcula el RPE promedio de un conjunto de series
     */
    private fun calculateAverageRPE(sets: List<ExerciseSetEntity>): Double? {
        val rpeValues = sets.mapNotNull { it.rpe }
        if (rpeValues.isEmpty()) return null
        return rpeValues.average()
    }

    /**
     * Calcula la tasa de completación (reps realizadas / reps planificadas)
     */
    private fun calculateCompletionRate(
        sets: List<ExerciseSetEntity>,
        plannedReps: Int,
        plannedSets: Int
    ): Double {
        if (plannedReps <= 0 || plannedSets <= 0) return 1.0
        val totalPlanned = plannedReps * plannedSets
        val totalCompleted = sets.take(plannedSets).sumOf { it.repsCompleted }
        return totalCompleted.toDouble() / totalPlanned.toDouble()
    }

    /**
     * Redondea al incremento más cercano (2.5 lbs)
     */
    private fun roundToNearestIncrement(weight: Double): Double {
        return (weight / ISOLATION_INCREMENT).roundToInt() * ISOLATION_INCREMENT
    }
}

/**
 * Fases de entrenamiento para periodización DUP
 */
enum class TrainingPhase(val displayName: String, val spanishName: String) {
    STRENGTH("Strength", "Fuerza"),
    HYPERTROPHY("Hypertrophy", "Hipertrofia"),
    ENDURANCE("Endurance", "Resistencia"),
    DELOAD("Deload", "Descarga");

    fun getSetsRange(): Pair<Int, Int> = when (this) {
        STRENGTH -> Pair(4, 6)
        HYPERTROPHY -> Pair(3, 4)
        ENDURANCE -> Pair(2, 3)
        DELOAD -> Pair(2, 3)
    }

    fun getRepsRange(): Pair<Int, Int> = when (this) {
        STRENGTH -> Pair(3, 6)
        HYPERTROPHY -> Pair(8, 12)
        ENDURANCE -> Pair(15, 20)
        DELOAD -> Pair(10, 15)
    }

    fun getRestSeconds(): Int = when (this) {
        STRENGTH -> 180
        HYPERTROPHY -> 90
        ENDURANCE -> 45
        DELOAD -> 60
    }
}
