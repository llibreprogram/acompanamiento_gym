package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.dao.WorkoutDao
import com.gymcompanion.app.data.local.dao.ExerciseDao
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analizador de Historial de Entrenamiento
 * 
 * Implementa adaptación inteligente basada en el historial:
 * - Detecta ejercicios donde el usuario falla consistentemente
 * - Identifica músculos débiles/subentrenados
 * - Rastrea variedad de ejercicios para evitar monotonía
 * - Calcula tendencias de progresión
 */
@Singleton
class WorkoutHistoryAnalyzer @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao
) {

    companion object {
        const val RECENT_SESSIONS_LIMIT = 20
        const val FAILURE_THRESHOLD = 0.75 // Si completa < 75% de reps, es un "fallo"
        const val CONSECUTIVE_FAILURES = 3  // 3+ fallos consecutivos → sugerir cambio
        const val VARIETY_WEEKS = 3 // No repetir exactamente los mismos ejercicios en 3 semanas
        const val MILLIS_PER_WEEK = 7L * 24 * 60 * 60 * 1000
    }

    /**
     * Obtiene ejercicios donde el usuario falla consistentemente
     * Retorna lista de exerciseIds que deberían ser sustituidos
     */
    suspend fun getFailedExercises(userId: Long): List<FailedExerciseInfo> {
        val failedExercises = mutableListOf<FailedExerciseInfo>()
        
        try {
            val recentSessions = workoutDao.getRecentCompletedWorkouts(userId, RECENT_SESSIONS_LIMIT).first()
            
            // Agrupar sets por ejercicio
            val exerciseSets = mutableMapOf<Long, MutableList<ExerciseSetEntity>>()
            for (session in recentSessions) {
                val sets = workoutDao.getSetsForWorkoutSession(session.id).first()
                for (set in sets) {
                    exerciseSets.getOrPut(set.exerciseId) { mutableListOf() }.add(set)
                }
            }
            
            // Analizar cada ejercicio
            for ((exerciseId, sets) in exerciseSets) {
                val workingSets = sets.filter { !it.isWarmUpSet && !it.isDropSet }
                if (workingSets.size < 3) continue // No suficientes datos
                
                // Contar fallos consecutivos recientes
                var consecutiveFailures = 0
                for (set in workingSets.sortedByDescending { it.performedAt }) {
                    // Un "fallo" es cuando el RPE es 10 o las reps completadas son mucho menores a las planeadas
                    if (set.rpe != null && set.rpe >= 10) {
                        consecutiveFailures++
                    } else {
                        break
                    }
                }
                
                // Alto RPE promedio
                val avgRPE = workingSets.mapNotNull { it.rpe }.average()
                
                if (consecutiveFailures >= CONSECUTIVE_FAILURES || (avgRPE > 9.5 && workingSets.size >= 6)) {
                    val exercise = try {
                        exerciseDao.getExerciseById(exerciseId).first()
                    } catch (e: Exception) { null }
                    
                    failedExercises.add(FailedExerciseInfo(
                        exerciseId = exerciseId,
                        exerciseName = exercise?.name ?: "Ejercicio #$exerciseId",
                        consecutiveFailures = consecutiveFailures,
                        averageRPE = avgRPE,
                        recommendation = "Considerar sustituir por un ejercicio alternativo más apropiado."
                    ))
                }
            }
        } catch (e: Exception) {
            // Si no hay datos, retornar lista vacía
        }
        
        return failedExercises
    }

    /**
     * Identifica grupos musculares débiles/subentrenados
     * Compara volumen total por grupo muscular
     */
    suspend fun getWeakMuscleGroups(userId: Long): List<MuscleGroupAnalysis> {
        val muscleVolume = mutableMapOf<String, Double>()
        
        try {
            val recentSessions = workoutDao.getRecentCompletedWorkouts(userId, RECENT_SESSIONS_LIMIT).first()
            
            for (session in recentSessions) {
                val sets = workoutDao.getSetsForWorkoutSession(session.id).first()
                for (set in sets) {
                    if (set.isWarmUpSet) continue
                    val exercise = try {
                        exerciseDao.getExerciseById(set.exerciseId).first()
                    } catch (e: Exception) { null } ?: continue
                    
                    val volume = (set.weightUsed ?: 0.0) * set.repsCompleted
                    muscleVolume[exercise.muscleGroup] = 
                        (muscleVolume[exercise.muscleGroup] ?: 0.0) + volume
                }
            }
        } catch (e: Exception) {
            // Sin datos
        }
        
        if (muscleVolume.isEmpty()) return emptyList()
        
        val avgVolume = muscleVolume.values.average()
        
        return muscleVolume.map { (muscleGroup, volume) ->
            val status = when {
                volume < avgVolume * 0.5 -> MuscleGroupStatus.UNDERTRAINED
                volume < avgVolume * 0.8 -> MuscleGroupStatus.BELOW_AVERAGE
                volume > avgVolume * 1.5 -> MuscleGroupStatus.OVERTRAINED
                else -> MuscleGroupStatus.BALANCED
            }
            MuscleGroupAnalysis(
                muscleGroup = muscleGroup,
                totalVolume = volume,
                status = status,
                recommendation = when (status) {
                    MuscleGroupStatus.UNDERTRAINED -> "Incrementar volumen y frecuencia de entrenamiento"
                    MuscleGroupStatus.BELOW_AVERAGE -> "Considerar agregar más ejercicios"
                    MuscleGroupStatus.OVERTRAINED -> "Reducir volumen para mejorar recuperación"
                    MuscleGroupStatus.BALANCED -> "Volumen adecuado"
                }
            )
        }.sortedBy { it.totalVolume }
    }

    /**
     * Obtiene ejercicios usados recientemente para evitar repetición
     * Retorna IDs de ejercicios usados en las últimas N semanas
     */
    suspend fun getRecentlyUsedExerciseIds(userId: Long, weeks: Int = VARIETY_WEEKS): Set<Long> {
        val cutoff = System.currentTimeMillis() - (weeks * MILLIS_PER_WEEK)
        val usedExercises = mutableSetOf<Long>()
        
        try {
            val sessions = workoutDao.getWorkoutSessionsInDateRange(
                userId, cutoff, System.currentTimeMillis()
            ).first()
            
            for (session in sessions) {
                val sets = workoutDao.getSetsForWorkoutSession(session.id).first()
                usedExercises.addAll(sets.map { it.exerciseId })
            }
        } catch (e: Exception) {
            // Sin datos
        }
        
        return usedExercises
    }

    /**
     * Calcula la tendencia de progresión para un ejercicio
     */
    suspend fun getProgressionTrend(exerciseId: Long): ProgressionTrend {
        try {
            val recentSets = workoutDao.getRecentSetsForExercise(exerciseId, 30).first()
            val workingSets = recentSets.filter { !it.isWarmUpSet && it.weightUsed != null && it.weightUsed > 0 }
            
            if (workingSets.size < 4) return ProgressionTrend.INSUFFICIENT_DATA
            
            // Comparar primeras vs últimas series
            val sortedByTime = workingSets.sortedBy { it.performedAt }
            val firstHalf = sortedByTime.take(sortedByTime.size / 2)
            val secondHalf = sortedByTime.drop(sortedByTime.size / 2)
            
            val firstAvgWeight = firstHalf.mapNotNull { it.weightUsed }.average()
            val secondAvgWeight = secondHalf.mapNotNull { it.weightUsed }.average()
            
            val changePercent = ((secondAvgWeight - firstAvgWeight) / firstAvgWeight) * 100
            
            return when {
                changePercent > 5 -> ProgressionTrend.PROGRESSING
                changePercent < -5 -> ProgressionTrend.REGRESSING
                else -> ProgressionTrend.PLATEAUED
            }
        } catch (e: Exception) {
            return ProgressionTrend.INSUFFICIENT_DATA
        }
    }
}

data class FailedExerciseInfo(
    val exerciseId: Long,
    val exerciseName: String,
    val consecutiveFailures: Int,
    val averageRPE: Double,
    val recommendation: String
)

data class MuscleGroupAnalysis(
    val muscleGroup: String,
    val totalVolume: Double,
    val status: MuscleGroupStatus,
    val recommendation: String
)

enum class MuscleGroupStatus(val displayName: String) {
    UNDERTRAINED("Sub-entrenado"),
    BELOW_AVERAGE("Por debajo del promedio"),
    BALANCED("Balanceado"),
    OVERTRAINED("Sobre-entrenado")
}

enum class ProgressionTrend(val displayName: String) {
    PROGRESSING("Progresando"),
    PLATEAUED("Estancado"),
    REGRESSING("Regresión"),
    INSUFFICIENT_DATA("Datos insuficientes")
}
