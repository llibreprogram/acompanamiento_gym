package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.local.entity.RoutineEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import com.gymcompanion.app.domain.model.*
import com.gymcompanion.app.domain.repository.ExerciseRepository
import com.gymcompanion.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

/**
 * Generador inteligente de rutinas basado en ciencia del entrenamiento
 * Aplica principios de periodización, volumen óptimo y frecuencia
 */
class RoutineGeneratorUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository
) {
    
    /**
     * Genera una rutina completa personalizada
     */
    suspend fun generateRoutine(request: RoutineGenerationRequest, userId: Long): List<RoutineEntity> {
        val allExercises = exerciseRepository.getAllExercises().first()
        
        return when (request.daysPerWeek) {
            3 -> generateFullBodySplit(request, userId, allExercises)
            4 -> generateUpperLowerSplit(request, userId, allExercises)
            5, 6 -> generatePushPullLegsSplit(request, userId, allExercises)
            else -> generateFullBodySplit(request, userId, allExercises)
        }
    }
    
    /**
     * Split de cuerpo completo (3 días)
     * Ideal para principiantes
     */
    private suspend fun generateFullBodySplit(
        request: RoutineGenerationRequest,
        userId: Long,
        exercises: List<ExerciseEntity>
    ): List<RoutineEntity> {
        val days = listOf("Lunes", "Miércoles", "Viernes")
        val routines = mutableListOf<RoutineEntity>()
        
        days.forEachIndexed { index, day ->
            val routineName = when (request.goal) {
                FitnessGoal.WEIGHT_LOSS -> "Cuerpo Completo - Pérdida de Peso $day"
                FitnessGoal.MUSCLE_GAIN -> "Cuerpo Completo - Hipertrofia $day"
                FitnessGoal.STRENGTH -> "Cuerpo Completo - Fuerza $day"
                else -> "Cuerpo Completo $day"
            }
            
            val selectedExercises = selectBalancedExercises(
                exercises = exercises,
                equipment = request.equipment,
                targetCount = 6 // 6 ejercicios por sesión
            )
            
            val routineId = createRoutineWithExercises(
                userId = userId,
                name = routineName,
                exercises = selectedExercises,
                goal = request.goal,
                level = request.level,
                duration = request.sessionDuration
            )
            
            routines.add(RoutineEntity(
                id = routineId,
                userId = userId,
                name = routineName,
                description = "Rutina de cuerpo completo optimizada para ${request.goal.toSpanish()}",
                daysOfWeek = "[\"$day\"]",
                isActive = true,
                isAIGenerated = true,
                duration = request.sessionDuration,
                focusArea = "Cuerpo Completo",
                difficulty = request.level.toSpanish(),
                createdAt = System.currentTimeMillis()
            ))
        }
        
        return routines
    }
    
    /**
     * Split Tirón/Empuje/Pierna (5-6 días)
     * Ideal para intermedios/avanzados
     */
    private suspend fun generatePushPullLegsSplit(
        request: RoutineGenerationRequest,
        userId: Long,
        exercises: List<ExerciseEntity>
    ): List<RoutineEntity> {
        val days = if (request.daysPerWeek == 6) {
            listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        } else {
            listOf("Lunes", "Martes", "Jueves", "Viernes", "Sábado")
        }
        
        val splits = listOf("Push", "Pull", "Legs", "Push", "Pull", "Legs").take(request.daysPerWeek)
        val routines = mutableListOf<RoutineEntity>()
        
        days.zip(splits).forEach { (day, split) ->
            val muscleGroups = when (split) {
                "Push" -> listOf("Pecho", "Hombros", "Tríceps")
                "Pull" -> listOf("Espalda", "Bíceps")
                "Legs" -> listOf("Piernas", "Core")
                else -> emptyList()
            }
            
            val selectedExercises = exercises.filter { ex ->
                muscleGroups.any { muscle -> ex.muscleGroup.contains(muscle, ignoreCase = true) }
            }.take(7)
            
            val routineId = createRoutineWithExercises(
                userId = userId,
                name = "$split - $day",
                exercises = selectedExercises,
                goal = request.goal,
                level = request.level,
                duration = request.sessionDuration
            )
            
            routines.add(RoutineEntity(
                id = routineId,
                userId = userId,
                name = "$split - $day",
                description = "Enfoque en ${muscleGroups.joinToString(", ")}",
                daysOfWeek = "[\"$day\"]",
                isActive = true,
                isAIGenerated = true,
                duration = request.sessionDuration,
                focusArea = muscleGroups.firstOrNull() ?: split,
                difficulty = request.level.toSpanish(),
                createdAt = System.currentTimeMillis()
            ))
        }
        
        return routines
    }
    
    /**
     * Split Tren Superior/Inferior (4 días)
     */
    private suspend fun generateUpperLowerSplit(
        request: RoutineGenerationRequest,
        userId: Long,
        exercises: List<ExerciseEntity>
    ): List<RoutineEntity> {
        val days = listOf("Lunes", "Martes", "Jueves", "Viernes")
        val splits = listOf("Superior", "Inferior", "Superior", "Inferior")
        val routines = mutableListOf<RoutineEntity>()
        
        days.zip(splits).forEach { (day, split) ->
            val muscleGroups = if (split == "Superior") {
                listOf("Pecho", "Espalda", "Hombros", "Brazos")
            } else {
                listOf("Piernas", "Core")
            }
            
            val selectedExercises = exercises.filter { ex ->
                muscleGroups.any { muscle -> ex.muscleGroup.contains(muscle, ignoreCase = true) }
            }.take(7)
            
            val routineId = createRoutineWithExercises(
                userId = userId,
                name = "Tren $split - $day",
                exercises = selectedExercises,
                goal = request.goal,
                level = request.level,
                duration = request.sessionDuration
            )
            
            routines.add(RoutineEntity(
                id = routineId,
                userId = userId,
                name = "Tren $split - $day",
                description = "Entrenamiento de tren ${split.lowercase()}",
                daysOfWeek = "[\"$day\"]",
                isActive = true,
                isAIGenerated = true,
                duration = request.sessionDuration,
                focusArea = split,
                difficulty = request.level.toSpanish(),
                createdAt = System.currentTimeMillis()
            ))
        }
        
        return routines
    }
    
    /**
     * Selecciona ejercicios balanceados para cuerpo completo
     */
    private fun selectBalancedExercises(
        exercises: List<ExerciseEntity>,
        equipmentFilter: AvailableEquipment,
        targetCount: Int
    ): List<ExerciseEntity> {
        val filtered = exercises.filter { exercise ->
            when (equipmentFilter) {
                AvailableEquipment.BODYWEIGHT_ONLY -> exercise.equipment == "Peso Corporal"
                AvailableEquipment.HOME_BASIC -> exercise.equipment in listOf("Mancuernas", "Barra", "Peso Corporal")
                AvailableEquipment.MINIMAL -> exercise.equipment in listOf("Mancuernas", "Peso Corporal")
                AvailableEquipment.FULL_GYM -> true
            }
        }
        
        val selected = mutableListOf<ExerciseEntity>()
        val priorities = listOf(
            "Piernas" to 2,    // 2 ejercicios de piernas
            "Pecho" to 1,      // 1 de pecho
            "Espalda" to 1,    // 1 de espalda
            "Hombros" to 1,    // 1 de hombros
            "Core" to 1        // 1 de core
        )
        
        priorities.forEach { (muscle, count) ->
            val muscleExercises = filtered.filter { it.muscleGroup.contains(muscle, ignoreCase = true) }
                .shuffled()
                .take(count)
            selected.addAll(muscleExercises)
        }
        
        return selected.take(targetCount)
    }
    
    /**
     * Crea la rutina y sus ejercicios con sets/reps óptimos
     */
    private suspend fun createRoutineWithExercises(
        userId: Long,
        name: String,
        exercises: List<ExerciseEntity>,
        goal: FitnessGoal,
        level: FitnessLevel,
        duration: Int
    ): Long {
        // Crear rutina
        val routineId = routineRepository.insertRoutine(
            RoutineEntity(
                id = 0,
                userId = userId,
                name = name,
                description = "Generado automáticamente",
                daysOfWeek = "[]",
                isActive = true,
                isAIGenerated = true,
                duration = duration,
                focusArea = "Generado",
                difficulty = level.toSpanish(),
                createdAt = System.currentTimeMillis()
            )
        )
        
        // Agregar ejercicios con parámetros optimizados
        exercises.forEachIndexed { index, exercise ->
            val (sets, reps) = calculateOptimalVolume(goal, level, exercise.exerciseType)
            
            routineRepository.addExerciseToRoutine(
                routineId = routineId,
                exerciseId = exercise.id,
                orderIndex = index,
                plannedSets = sets,
                plannedReps = "$reps",
                restTimeSeconds = calculateRestTime(goal, exercise.exerciseType)
            )
        }
        
        return routineId
    }
    
    /**
     * Calcula volumen óptimo (series x reps) según objetivo
     */
    private fun calculateOptimalVolume(
        goal: FitnessGoal,
        level: FitnessLevel,
        exerciseType: String
    ): Pair<Int, Int> {
        val isCompound = exerciseType == "Compuesto"
        
        return when (goal) {
            FitnessGoal.STRENGTH -> {
                // Baja rep, alta intensidad
                if (isCompound) Pair(5, 5) else Pair(3, 8)
            }
            FitnessGoal.MUSCLE_GAIN -> {
                // Volumen moderado-alto
                when (level) {
                    FitnessLevel.BEGINNER -> Pair(3, 12)
                    FitnessLevel.INTERMEDIATE -> if (isCompound) Pair(4, 8) else Pair(3, 12)
                    FitnessLevel.ADVANCED -> if (isCompound) Pair(5, 8) else Pair(4, 12)
                }
            }
            FitnessGoal.WEIGHT_LOSS, FitnessGoal.ENDURANCE -> {
                // Alto rep, circuito
                Pair(3, 15)
            }
            else -> {
                // General fitness
                Pair(3, 10)
            }
        }
    }
    
    /**
     * Calcula tiempo de descanso óptimo
     */
    private fun calculateRestTime(goal: FitnessGoal, exerciseType: String): Int {
        val isCompound = exerciseType == "Compuesto"
        
        return when (goal) {
            FitnessGoal.STRENGTH -> if (isCompound) 180 else 120 // 2-3 min
            FitnessGoal.MUSCLE_GAIN -> if (isCompound) 90 else 60 // 60-90 seg
            FitnessGoal.WEIGHT_LOSS, FitnessGoal.ENDURANCE -> 45 // 45 seg
            else -> 60 // 1 min
        }
    }
    
    // Extensiones de ayuda
    private fun FitnessGoal.toSpanish(): String = when (this) {
        FitnessGoal.WEIGHT_LOSS -> "pérdida de peso"
        FitnessGoal.MUSCLE_GAIN -> "ganancia muscular"
        FitnessGoal.STRENGTH -> "aumento de fuerza"
        FitnessGoal.ENDURANCE -> "resistencia"
        FitnessGoal.GENERAL_FITNESS -> "fitness general"
        FitnessGoal.BODY_RECOMPOSITION -> "recomposición corporal"
    }
    
    private fun FitnessLevel.toSpanish(): String = when (this) {
        FitnessLevel.BEGINNER -> "Principiante"
        FitnessLevel.INTERMEDIATE -> "Intermedio"
        FitnessLevel.ADVANCED -> "Avanzado"
    }
}
