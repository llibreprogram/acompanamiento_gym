package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.domain.model.FitnessGoal
import com.gymcompanion.app.domain.model.FitnessLevel
import com.gymcompanion.app.domain.model.PhysicalLimitation
import javax.inject.Inject

/**
 * Analizador inteligente de ejercicios para optimización científica de rutinas
 * 
 * Implementa principios de:
 * - Biomecánica y balance muscular
 * - Periodización lineal
 * - Prevención de lesiones
 * - Especificidad del objetivo
 */
class SmartExerciseAnalyzer @Inject constructor() {
    
    /**
     * Calcula una puntuación de idoneidad para un ejercicio dado el contexto
     * @return puntuación de 0.0 a 1.0
     */
    fun calculateExerciseSuitability(
        exercise: ExerciseEntity,
        goal: FitnessGoal,
        level: FitnessLevel,
        currentMusclesWorked: Map<String, Int>, // músculo -> veces trabajado
        limitations: List<PhysicalLimitation>
    ): Double {
        var score = 0.5 // Base score
        
        // 1. Análisis de objetivo (30% del score)
        score += calculateGoalAlignment(exercise, goal) * 0.3
        
        // 2. Análisis de nivel (20% del score)
        score += calculateLevelAlignment(exercise, level) * 0.2
        
        // 3. Balance muscular (30% del score)
        score += calculateMuscleBalance(exercise, currentMusclesWorked) * 0.3
        
        // 4. Penalización por limitaciones (puede restar hasta 1.0)
        score -= calculateLimitationPenalty(exercise, limitations)
        
        return score.coerceIn(0.0, 1.0)
    }
    
    /**
     * Analiza qué tan bien se alinea el ejercicio con el objetivo
     */
    private fun calculateGoalAlignment(exercise: ExerciseEntity, goal: FitnessGoal): Double {
        return when (goal) {
            FitnessGoal.STRENGTH -> {
                // Favorece ejercicios compuestos con barra
                when {
                    exercise.exerciseType == "Compuesto" && exercise.equipmentNeeded == "Barra" -> 1.0
                    exercise.exerciseType == "Compuesto" -> 0.8
                    else -> 0.4
                }
            }
            FitnessGoal.MUSCLE_GAIN -> {
                // Balance entre compuestos y aislamiento
                when {
                    exercise.exerciseType == "Compuesto" -> 0.9
                    exercise.exerciseType == "Aislamiento" -> 0.7
                    else -> 0.5
                }
            }
            FitnessGoal.WEIGHT_LOSS, FitnessGoal.ENDURANCE -> {
                // Favorece ejercicios que quemen más calorías
                when {
                    exercise.muscleGroup == "Piernas" -> 1.0
                    exercise.muscleGroup == "Cuerpo Completo" -> 0.9
                    exercise.exerciseType == "Compuesto" -> 0.8
                    else -> 0.5
                }
            }
            FitnessGoal.BODY_RECOMPOSITION -> {
                // Mix equilibrado
                when {
                    exercise.exerciseType == "Compuesto" -> 0.9
                    else -> 0.6
                }
            }
            else -> 0.5
        }
    }
    
    /**
     * Analiza si el ejercicio es apropiado para el nivel
     */
    private fun calculateLevelAlignment(exercise: ExerciseEntity, level: FitnessLevel): Double {
        val exerciseDifficulty = exercise.difficulty
        
        return when (level) {
            FitnessLevel.BEGINNER -> {
                when (exerciseDifficulty) {
                    "Principiante" -> 1.0
                    "Intermedio" -> 0.6
                    "Avanzado" -> 0.2
                    else -> 0.5
                }
            }
            FitnessLevel.INTERMEDIATE -> {
                when (exerciseDifficulty) {
                    "Principiante" -> 0.7
                    "Intermedio" -> 1.0
                    "Avanzado" -> 0.7
                    else -> 0.5
                }
            }
            FitnessLevel.ADVANCED -> {
                when (exerciseDifficulty) {
                    "Principiante" -> 0.5
                    "Intermedio" -> 0.8
                    "Avanzado" -> 1.0
                    else -> 0.5
                }
            }
        }
    }
    
    /**
     * Analiza el balance muscular - prioriza músculos menos trabajados
     */
    private fun calculateMuscleBalance(
        exercise: ExerciseEntity,
        currentMusclesWorked: Map<String, Int>
    ): Double {
        val targetMuscle = exercise.muscleGroup
        val timesWorked = currentMusclesWorked[targetMuscle] ?: 0
        
        // Menos veces trabajado = mayor score
        return when (timesWorked) {
            0 -> 1.0  // Músculo no trabajado = máxima prioridad
            1 -> 0.8
            2 -> 0.5
            3 -> 0.3
            else -> 0.1 // Ya muy trabajado
        }
    }
    
    /**
     * Calcula penalización por limitaciones físicas
     */
    private fun calculateLimitationPenalty(
        exercise: ExerciseEntity,
        limitations: List<PhysicalLimitation>
    ): Double {
        if (limitations.isEmpty()) return 0.0
        
        val exerciseName = exercise.name.lowercase()
        val muscleGroup = exercise.muscleGroup.lowercase()
        
        for (limitation in limitations) {
            // Penalización severa si afecta directamente
            val affectedExercises = limitation.affectedExercises.map { it.lowercase() }
            if (affectedExercises.any { exerciseName.contains(it) }) {
                return 1.0 // Eliminar completamente
            }
            
            // Penalización moderada si afecta el grupo muscular
            when (limitation) {
                PhysicalLimitation.KNEE_PROBLEMS -> {
                    if (muscleGroup.contains("pierna")) return 0.5
                }
                PhysicalLimitation.SHOULDER_PROBLEMS -> {
                    if (muscleGroup.contains("hombro") || muscleGroup.contains("pecho")) return 0.5
                }
                PhysicalLimitation.LOWER_BACK_PROBLEMS -> {
                    if (muscleGroup.contains("espalda") || muscleGroup.contains("pierna")) return 0.4
                }
                PhysicalLimitation.WRIST_PROBLEMS -> {
                    if (muscleGroup.contains("brazo") || muscleGroup.contains("pecho")) return 0.3
                }
                else -> {}
            }
        }
        
        return 0.0
    }
    
    /**
     * Genera recomendaciones de volumen basadas en objetivo y semana de entrenamiento
     * Implementa periodización lineal
     */
    fun calculateOptimalVolume(
        goal: FitnessGoal,
        level: FitnessLevel,
        isCompound: Boolean,
        weekNumber: Int = 1 // Para periodización
    ): VolumeRecommendation {
        val baseVolume = when (goal) {
            FitnessGoal.STRENGTH -> {
                if (isCompound) {
                    VolumeRecommendation(sets = 5, repsMin = 3, repsMax = 5, rest = 180)
                } else {
                    VolumeRecommendation(sets = 3, repsMin = 6, repsMax = 8, rest = 120)
                }
            }
            FitnessGoal.MUSCLE_GAIN -> {
                if (isCompound) {
                    VolumeRecommendation(sets = 4, repsMin = 6, repsMax = 10, rest = 90)
                } else {
                    VolumeRecommendation(sets = 3, repsMin = 10, repsMax = 15, rest = 60)
                }
            }
            FitnessGoal.WEIGHT_LOSS, FitnessGoal.ENDURANCE -> {
                VolumeRecommendation(sets = 3, repsMin = 12, repsMax = 20, rest = 45)
            }
            else -> {
                VolumeRecommendation(sets = 3, repsMin = 8, repsMax = 12, rest = 60)
            }
        }
        
        // Ajuste por nivel
        return when (level) {
            FitnessLevel.BEGINNER -> baseVolume.copy(
                sets = (baseVolume.sets - 1).coerceAtLeast(2)
            )
            FitnessLevel.INTERMEDIATE -> baseVolume
            FitnessLevel.ADVANCED -> baseVolume.copy(
                sets = baseVolume.sets + 1
            )
        }
    }
    
    /**
     * Verifica el ratio push/pull para prevenir desbalances
     */
    fun analyzePushPullRatio(exercises: List<ExerciseEntity>): PushPullAnalysis {
        var pushCount = 0
        var pullCount = 0
        
        exercises.forEach { exercise ->
            when {
                exercise.muscleGroup.contains("Pecho", ignoreCase = true) ||
                exercise.muscleGroup.contains("Hombros", ignoreCase = true) ||
                exercise.muscleGroup.contains("Tríceps", ignoreCase = true) -> pushCount++
                
                exercise.muscleGroup.contains("Espalda", ignoreCase = true) ||
                exercise.muscleGroup.contains("Bíceps", ignoreCase = true) -> pullCount++
            }
        }
        
        val ratio = if (pullCount > 0) pushCount.toDouble() / pullCount else 0.0
        val isBalanced = ratio in 0.8..1.2 // Ratio ideal 1:1 con 20% tolerancia
        
        return PushPullAnalysis(
            pushCount = pushCount,
            pullCount = pullCount,
            ratio = ratio,
            isBalanced = isBalanced,
            recommendation = if (!isBalanced && pullCount < pushCount) {
                "Agregar más ejercicios de tirón (espalda/bíceps)"
            } else if (!isBalanced) {
                "Agregar más ejercicios de empuje (pecho/hombros/tríceps)"
            } else {
                "Balance push/pull óptimo"
            }
        )
    }
}



/**
 * Análisis de balance push/pull
 */
data class PushPullAnalysis(
    val pushCount: Int,
    val pullCount: Int,
    val ratio: Double,
    val isBalanced: Boolean,
    val recommendation: String
)
