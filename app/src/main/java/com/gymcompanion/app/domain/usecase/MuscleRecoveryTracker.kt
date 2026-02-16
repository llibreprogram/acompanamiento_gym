package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.dao.WorkoutDao
import com.gymcompanion.app.data.local.dao.ExerciseDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracker de Recuperación Muscular
 * 
 * Calcula el estado de recuperación de cada grupo muscular basado en:
 * - Tiempo desde el último entrenamiento del músculo
 * - Tiempos de recuperación basados en ciencia por grupo muscular
 * - Intensidad del último entrenamiento (via RPE)
 */
@Singleton
class MuscleRecoveryTracker @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao
) {

    companion object {
        // Tiempos de recuperación en horas, basados en ciencia deportiva
        val RECOVERY_TIMES = mapOf(
            "legs" to RecoveryWindow(minHours = 48, optimalHours = 72, maxHours = 96),
            "back" to RecoveryWindow(minHours = 48, optimalHours = 60, maxHours = 72),
            "chest" to RecoveryWindow(minHours = 48, optimalHours = 60, maxHours = 72),
            "shoulders" to RecoveryWindow(minHours = 48, optimalHours = 48, maxHours = 72),
            "arms" to RecoveryWindow(minHours = 24, optimalHours = 36, maxHours = 48),
            "core" to RecoveryWindow(minHours = 24, optimalHours = 36, maxHours = 48),
            "full_body" to RecoveryWindow(minHours = 48, optimalHours = 72, maxHours = 96)
        )

        val ALL_MUSCLE_GROUPS = listOf("chest", "back", "legs", "shoulders", "arms", "core")
    }

    /**
     * Obtiene el estado de recuperación de todos los grupos musculares
     */
    suspend fun getMuscleRecoveryStatus(userId: Long): Map<String, MuscleRecoveryInfo> {
        val now = System.currentTimeMillis()
        val result = mutableMapOf<String, MuscleRecoveryInfo>()

        for (muscleGroup in ALL_MUSCLE_GROUPS) {
            val lastWorkoutTime = getLastWorkoutTimeForMuscle(userId, muscleGroup)
            val recoveryWindow = RECOVERY_TIMES[muscleGroup]
                ?: RecoveryWindow(48, 60, 72)

            if (lastWorkoutTime == null) {
                // Nunca entrenado → totalmente recuperado
                result[muscleGroup] = MuscleRecoveryInfo(
                    muscleGroup = muscleGroup,
                    status = RecoveryStatus.RECOVERED,
                    hoursSinceLastWorkout = null,
                    percentRecovered = 100,
                    hoursUntilRecovered = 0
                )
            } else {
                val hoursSince = (now - lastWorkoutTime) / (1000.0 * 60.0 * 60.0)
                val percentRecovered = calculateRecoveryPercent(hoursSince, recoveryWindow)
                val status = when {
                    percentRecovered >= 100 -> RecoveryStatus.RECOVERED
                    percentRecovered >= 70 -> RecoveryStatus.PARTIAL
                    else -> RecoveryStatus.FATIGUED
                }
                val hoursUntilRecovered = if (percentRecovered >= 100) 0
                    else (recoveryWindow.optimalHours - hoursSince).coerceAtLeast(0.0).toInt()

                result[muscleGroup] = MuscleRecoveryInfo(
                    muscleGroup = muscleGroup,
                    status = status,
                    hoursSinceLastWorkout = hoursSince.toInt(),
                    percentRecovered = percentRecovered.toInt().coerceIn(0, 100),
                    hoursUntilRecovered = hoursUntilRecovered
                )
            }
        }

        return result
    }

    /**
     * Verifica si un músculo está listo para entrenar
     */
    suspend fun isMuscleReadyForTraining(userId: Long, muscleGroup: String): Boolean {
        val status = getMuscleRecoveryStatus(userId)
        val info = status[muscleGroup] ?: return true
        return info.status != RecoveryStatus.FATIGUED
    }

    /**
     * Obtiene los músculos más recuperados (para priorizar en la rutina)
     */
    suspend fun getMostRecoveredMuscles(userId: Long): List<String> {
        val status = getMuscleRecoveryStatus(userId)
        return status.entries
            .sortedByDescending { it.value.percentRecovered }
            .map { it.key }
    }

    /**
     * Obtiene el timestamp del último entrenamiento de un grupo muscular
     */
    private suspend fun getLastWorkoutTimeForMuscle(userId: Long, muscleGroup: String): Long? {
        // Obtener ejercicios del grupo muscular
        val exercises = try {
            exerciseDao.getExercisesByMuscleGroup(muscleGroup).first()
        } catch (e: Exception) {
            return null
        }
        if (exercises.isEmpty()) return null

        // Buscar la serie más reciente de cualquier ejercicio de ese grupo
        var latestTime: Long? = null
        for (exercise in exercises) {
            try {
                val recentSets = workoutDao.getRecentSetsForExercise(exercise.id, 1).first()
                val lastSet = recentSets.firstOrNull()
                if (lastSet != null && (latestTime == null || lastSet.performedAt > latestTime)) {
                    latestTime = lastSet.performedAt
                }
            } catch (e: Exception) {
                continue
            }
        }
        return latestTime
    }

    /**
     * Calcula el porcentaje de recuperación basado en el tiempo transcurrido
     * Usa una curva progresiva (no lineal)
     */
    private fun calculateRecoveryPercent(hoursSince: Double, window: RecoveryWindow): Double {
        return when {
            hoursSince >= window.optimalHours -> 100.0
            hoursSince <= 0 -> 0.0
            else -> {
                // Curva de recuperación: rápida al principio, más lenta al final
                val progress = hoursSince / window.optimalHours
                // Función de raíz cuadrada para simular curva de recuperación real
                (Math.sqrt(progress) * 100.0).coerceIn(0.0, 100.0)
            }
        }
    }
}

/**
 * Ventana de recuperación para un grupo muscular
 */
data class RecoveryWindow(
    val minHours: Int,     // Mínimo para entrenar de nuevo
    val optimalHours: Int, // Tiempo óptimo de recuperación
    val maxHours: Int      // Máximo — después de esto no hay beneficio extra
)

/**
 * Estado de recuperación de un músculo
 */
enum class RecoveryStatus(val displayName: String, val spanishName: String) {
    RECOVERED("Recovered", "Recuperado"),
    PARTIAL("Partially Recovered", "Parcial"),
    FATIGUED("Fatigued", "Fatigado")
}

/**
 * Información de recuperación de un grupo muscular
 */
data class MuscleRecoveryInfo(
    val muscleGroup: String,
    val status: RecoveryStatus,
    val hoursSinceLastWorkout: Int?,
    val percentRecovered: Int,
    val hoursUntilRecovered: Int
)
