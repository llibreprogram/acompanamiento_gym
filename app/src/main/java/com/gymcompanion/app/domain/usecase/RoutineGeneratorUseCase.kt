package com.gymcompanion.app.domain.usecase

import android.util.Log
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.local.entity.RoutineEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import com.gymcompanion.app.domain.model.AvailableEquipment
import com.gymcompanion.app.domain.model.FitnessGoal
import com.gymcompanion.app.domain.model.FitnessLevel
import com.gymcompanion.app.domain.model.RoutineGenerationRequest
import com.gymcompanion.app.domain.repository.ExerciseRepository
import com.gymcompanion.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.random.Random

/**
 * Generador inteligente de rutinas basado en ciencia del entrenamiento
 * Aplica principios de periodización, volumen óptimo y frecuencia
 * 
 * Versión 2.0 - IA mejorada con análisis científico
 */
class RoutineGeneratorUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val smartAnalyzer: SmartExerciseAnalyzer
) {
    
    /**
     * Genera una rutina completa personalizada
     */
    suspend fun generateRoutine(request: RoutineGenerationRequest, userId: Long): List<RoutineEntity> {
        val allExercises = exerciseRepository.getAllExercises().first()
        
        // Personalización avanzada:
        // En vez de filtrar estrictamente (lo que puede dejar 0 resultados), 
        // dejamos que SmartExerciseAnalyzer puntúe y ordene los ejercicios.
        // Solo filtramos explícitamente si hay restricciones de seguridad críticas que no maneje el Analyzer.
        
        val filteredExercises = allExercises.filter { exercise ->
             // Filtrar por restricciones médicas explícitas (texto libre) si el usuario las ingresó
             val restrictionMatch = request.restrictions.isNullOrBlank() || 
                                   !exercise.name.contains(request.restrictions, ignoreCase = true)
                                   
             restrictionMatch
        }

        Log.d("RoutineGeneratorUseCase", "Ejercicios filtrados: ${filteredExercises.size} de ${allExercises.size} para perfil: age=${request.age}, gender=${request.gender}, weight=${request.weight}, height=${request.height}, experienceLevel=${request.experienceLevel}, preferences=${request.preferences}, restrictions=${request.restrictions}")

        // Usar filteredExercises en vez de allExercises para la generación
        return when (request.daysPerWeek) {
            3 -> generateFullBodySplit(request, userId, filteredExercises)
            4 -> generateUpperLowerSplit(request, userId, filteredExercises)
            5, 6 -> generatePushPullLegsSplit(request, userId, filteredExercises)
            else -> generateFullBodySplit(request, userId, filteredExercises)
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
        val days = if (request.consecutiveDays) {
            listOf("Lunes", "Martes", "Miércoles")
        } else {
            listOf("Lunes", "Miércoles", "Viernes")
        }
        val routines = mutableListOf<RoutineEntity>()
        val globalUsedExercises: MutableSet<Long> = mutableSetOf()

        days.forEachIndexed { index, day ->
            // Asignar fase DUP para este día
            val phase = smartAnalyzer.assignPhaseForDay(index, days.size)
            
            val routineName = "Cuerpo Completo - ${phase.spanishName} $day"

            var selectedExercises = selectBalancedExercises(
                exercises = exercises,
                equipmentFilter = request.equipment,
                targetCount = calculateTargetExerciseCount(request.sessionDuration),
                physicalLimitations = request.physicalLimitations,
                goal = request.goal,
                level = request.level
            )

            // Evitar ejercicios repetidos en toda la semana
            selectedExercises = selectedExercises.filterNot { globalUsedExercises.contains(it.id) }

            // FALLBACK: Si no hay suficientes ejercicios, relajar el filtro
            if (selectedExercises.size < calculateTargetExerciseCount(request.sessionDuration)) {
                selectedExercises = exercises.filterNot { globalUsedExercises.contains(it.id) }.take(calculateTargetExerciseCount(request.sessionDuration))
            }

            val routineId = createRoutineWithExercises(
                userId = userId,
                name = routineName,
                exercises = selectedExercises,
                goal = request.goal,
                level = request.level,
                duration = request.sessionDuration,
                phase = phase
            )

            routines.add(RoutineEntity(
                id = routineId,
                userId = userId,
                name = routineName,
                description = "Rutina de cuerpo completo - ${phase.spanishName}: ${getPhaseDescription(phase)}",
                daysOfWeek = "[\"$day\"]",
                isActive = true,
                isAIGenerated = true,
                duration = request.sessionDuration,
                focusArea = "Cuerpo Completo",
                difficulty = request.level.toSpanish(),
                createdAt = System.currentTimeMillis()
            ))

            globalUsedExercises.addAll(selectedExercises.map { it.id })
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
            if (request.consecutiveDays) {
                listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
            } else {
                listOf("Lunes", "Martes", "Miércoles", "Viernes", "Sábado", "Domingo")
            }
        } else {
            if (request.consecutiveDays) {
                listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
            } else {
                listOf("Lunes", "Martes", "Jueves", "Viernes", "Sábado")
            }
        }
        
        val splits = listOf("Push", "Pull", "Legs", "Push", "Pull", "Legs").take(request.daysPerWeek)
        val routines = mutableListOf<RoutineEntity>()
        
    var previousMuscleGroups: List<String> = emptyList()
    var previousExercises: Set<Long> = emptySet()
    val globalUsedExercises: MutableSet<Long> = mutableSetOf()
    days.zip(splits).forEachIndexed { dayIndex, (day, split) ->
            // Asignar fase DUP para este día
            val phase = smartAnalyzer.assignPhaseForDay(dayIndex, days.size)
            
            val muscleGroups = when (split) {
                "Push" -> listOf("Pecho", "Hombros", "Tríceps")
                "Pull" -> listOf("Espalda", "Bíceps")
                "Legs" -> listOf("Piernas", "Core")
                else -> emptyList()
            }

            // Evitar repetir el mismo grupo muscular principal del día anterior
            val filteredMuscleGroups = muscleGroups.filterNot { previousMuscleGroups.contains(it) }
            val effectiveMuscleGroups = if (filteredMuscleGroups.isNotEmpty()) filteredMuscleGroups else muscleGroups

            // Filter by muscle group
            var muscleFiltered = exercises.filter { ex ->
                effectiveMuscleGroups.any { muscle -> ex.muscleGroup.contains(muscle, ignoreCase = true) }
            }

            // Filter by equipment - using helper function to avoid compiler issues
            var selectedExercises = filterByEquipment(muscleFiltered, request.equipment)

            // Filter by physical limitations
            selectedExercises = filterByPhysicalLimitations(selectedExercises, request.physicalLimitations)

            // Evitar ejercicios repetidos en toda la semana
            selectedExercises = selectedExercises.filterNot { globalUsedExercises.contains(it.id) }

            // FALLBACK: Si no hay suficientes ejercicios, relajar el filtro
            if (selectedExercises.size < 5) {
                selectedExercises = filterByPhysicalLimitations(muscleFiltered, request.physicalLimitations)
                selectedExercises = selectedExercises.filterNot { globalUsedExercises.contains(it.id) }
            }
            if (selectedExercises.size < 3) {
                selectedExercises = filterByPhysicalLimitations(
                    filterByEquipment(exercises, request.equipment),
                    request.physicalLimitations
                ).filterNot { globalUsedExercises.contains(it.id) }
            }

            selectedExercises = selectedExercises.take(calculateTargetExerciseCount(request.sessionDuration))

            val routineName = "$split ${phase.spanishName} - $day"
            val routineId = createRoutineWithExercises(
                userId = userId,
                name = routineName,
                exercises = selectedExercises,
                goal = request.goal,
                level = request.level,
                duration = request.sessionDuration,
                phase = phase
            )

            routines.add(RoutineEntity(
                id = routineId,
                userId = userId,
                name = routineName,
                description = "${phase.spanishName}: ${effectiveMuscleGroups.joinToString(", ")}",
                daysOfWeek = "[\"$day\"]",
                isActive = true,
                isAIGenerated = true,
                duration = request.sessionDuration,
                focusArea = effectiveMuscleGroups.firstOrNull() ?: split,
                difficulty = request.level.toSpanish(),
                createdAt = System.currentTimeMillis()
            ))

            previousMuscleGroups = muscleGroups
            previousExercises = selectedExercises.map { it.id }.toSet()
            globalUsedExercises.addAll(selectedExercises.map { it.id })
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
        
    var previousMuscleGroups: List<String> = emptyList()
    var previousExercises: Set<Long> = emptySet()
    val globalUsedExercises: MutableSet<Long> = mutableSetOf()
    days.zip(splits).forEachIndexed { dayIndex, (day, split) ->
            // Asignar fase DUP para este día
            val phase = smartAnalyzer.assignPhaseForDay(dayIndex, days.size)
            
            val muscleGroups = if (split == "Superior") {
                listOf("Pecho", "Espalda", "Hombros", "Brazos")
            } else {
                listOf("Piernas", "Core")
            }

            // Evitar repetir el mismo grupo muscular principal del día anterior
            val filteredMuscleGroups = muscleGroups.filterNot { previousMuscleGroups.contains(it) }
            val effectiveMuscleGroups = if (filteredMuscleGroups.isNotEmpty()) filteredMuscleGroups else muscleGroups

            // Filter by muscle group
            var muscleFiltered = exercises.filter { ex ->
                effectiveMuscleGroups.any { muscle -> ex.muscleGroup.contains(muscle, ignoreCase = true) }
            }

            // Filter by equipment - using helper function to avoid compiler issues
            var selectedExercises = filterByEquipment(muscleFiltered, request.equipment)

            // Filter by physical limitations
            selectedExercises = filterByPhysicalLimitations(selectedExercises, request.physicalLimitations)

            // Evitar ejercicios repetidos en toda la semana
            selectedExercises = selectedExercises.filterNot { globalUsedExercises.contains(it.id) }

            // FALLBACK: Si no hay suficientes ejercicios, relajar el filtro
            if (selectedExercises.size < 5) {
                selectedExercises = filterByPhysicalLimitations(muscleFiltered, request.physicalLimitations)
                selectedExercises = selectedExercises.filterNot { globalUsedExercises.contains(it.id) }
            }
            if (selectedExercises.size < 3) {
                selectedExercises = filterByPhysicalLimitations(
                    filterByEquipment(exercises, request.equipment),
                    request.physicalLimitations
                ).filterNot { globalUsedExercises.contains(it.id) }
            }

            selectedExercises = selectedExercises.take(calculateTargetExerciseCount(request.sessionDuration))

            val routineName = "Tren $split ${phase.spanishName} - $day"
            val routineId = createRoutineWithExercises(
                userId = userId,
                name = routineName,
                exercises = selectedExercises,
                goal = request.goal,
                level = request.level,
                duration = request.sessionDuration,
                phase = phase
            )

            routines.add(RoutineEntity(
                id = routineId,
                userId = userId,
                name = routineName,
                description = "${phase.spanishName}: Entrenamiento de tren ${split.lowercase()}",
                daysOfWeek = "[\"$day\"]",
                isActive = true,
                isAIGenerated = true,
                duration = request.sessionDuration,
                focusArea = split,
                difficulty = request.level.toSpanish(),
                createdAt = System.currentTimeMillis()
            ))

            previousMuscleGroups = muscleGroups
            previousExercises = selectedExercises.map { it.id }.toSet()
            globalUsedExercises.addAll(selectedExercises.map { it.id })
        }

        return routines
    }
    
    /**
     * Selecciona ejercicios balanceados para cuerpo completo usando IA
     */
    private fun selectBalancedExercises(
        exercises: List<ExerciseEntity>,
        equipmentFilter: AvailableEquipment,
        targetCount: Int,
        physicalLimitations: List<com.gymcompanion.app.domain.model.PhysicalLimitation> = emptyList(),
        goal: FitnessGoal = FitnessGoal.GENERAL_FITNESS,
        level: FitnessLevel = FitnessLevel.INTERMEDIATE
    ): List<ExerciseEntity> {
        // Filter by equipment
        var filtered = filterByEquipment(exercises, equipmentFilter)
        
        // Filter by physical limitations
        filtered = filterByPhysicalLimitations(filtered, physicalLimitations)
        
        // FALLBACK: Si no hay suficientes ejercicios con el filtro, usar todos
        if (filtered.size < targetCount) {
            filtered = exercises
        }
        
        val selected = mutableListOf<ExerciseEntity>()
        val musclesWorked = mutableMapOf<String, Int>()
        
        // FATIGA DEL CNS: Gestión de energía
        val maxFatigue = when(level) {
            FitnessLevel.BEGINNER -> 15
            FitnessLevel.INTERMEDIATE -> 24
            FitnessLevel.ADVANCED -> 35
        }
        var currentFatigue = 0
        
        // ANÁLISIS INTELIGENTE: Priorizar grupos musculares importantes
        val priorities = listOf(
            "Piernas" to 2,    // 2 ejercicios de piernas
            "Pecho" to 1,      // 1 de pecho
            "Espalda" to 1,    // 1 de espalda
            "Hombros" to 1,    // 1 de hombros
            "Core" to 1        // 1 de core
        )
        
        priorities.forEach { (muscle, count) ->
            // Si ya tenemos suficientes, no agregar más de esta prioridad
            if (selected.size >= targetCount) return@forEach

            val candidates = filtered
                .filter { it.muscleGroup.contains(muscle, ignoreCase = true) }
                .filter { it !in selected }
                .map { exercise ->
                    val suitability = smartAnalyzer.calculateExerciseSuitability(
                        exercise = exercise,
                        goal = goal,
                        level = level,
                        currentMusclesWorked = musclesWorked,
                        limitations = physicalLimitations
                    )
                    exercise to suitability
                }
                .filter { (ex, _) -> 
                    // Verificar límite de fatiga
                    val fatigue = smartAnalyzer.calculateFatigueScore(ex)
                    currentFatigue + fatigue <= maxFatigue
                }
                .sortedByDescending { it.second }
            
            // Selección ponderada (Weighted Random) para variedad
            val picked = selectWeighted(candidates, count)
            
            selected.addAll(picked)
            picked.forEach { 
                musclesWorked[muscle] = (musclesWorked[muscle] ?: 0) + 1
                currentFatigue += smartAnalyzer.calculateFatigueScore(it)
            }
        }
        
        // Rellenar slots restantes
        while (selected.size < targetCount) {
            val candidates = filtered
                .filterNot { it in selected }
                .map { exercise ->
                    val suitability = smartAnalyzer.calculateExerciseSuitability(
                        exercise = exercise,
                        goal = goal,
                        level = level,
                        currentMusclesWorked = musclesWorked,
                        limitations = physicalLimitations
                    )
                    exercise to suitability
                }
                .filter { (ex, _) -> 
                    // En relleno, ser más flexibles con fatiga si es necesario, 
                    // pero intentar respetarlo si hay opciones
                    val fatigue = smartAnalyzer.calculateFatigueScore(ex)
                    currentFatigue + fatigue <= maxFatigue
                }
                .sortedByDescending { it.second }
            
            if (candidates.isEmpty()) {
                // Si no hay candidatos por fatiga, tomar cualquiera (fallback)
                val fallbackCandidates = filtered
                    .filterNot { it in selected }
                    .map { it to 0.5 }
                
                if (fallbackCandidates.isNotEmpty()) {
                    val picked = selectWeighted(fallbackCandidates, 1)
                    selected.addAll(picked)
                } else {
                    break // No hay más ejercicios disponibles
                }
            } else {
                val picked = selectWeighted(candidates, 1)
                selected.addAll(picked)
                picked.forEach { 
                     currentFatigue += smartAnalyzer.calculateFatigueScore(it)
                }
            }
        }
        
        return selected.take(targetCount)
    }

    /**
     * Selecciona items aleatoriamente basándose en su peso (score)
     * Cuanto mayor score, mayor probabilidad, pero no garantizado.
     */
    private fun selectWeighted(
        candidates: List<Pair<ExerciseEntity, Double>>,
        count: Int
    ): List<ExerciseEntity> {
        val selected = mutableListOf<ExerciseEntity>()
        val mutableCandidates = candidates.toMutableList()

        repeat(count) {
            if (mutableCandidates.isEmpty()) return@repeat
            
            // Filtrar scores muy bajos para no seleccionar basura, pero permitir variedad
            // O simplemente usar el score tal cual
            
            val totalScore = mutableCandidates.sumOf { it.second }
            if (totalScore <= 0.0) {
                // Si todos tienen 0, elegir al azar uniforme
                val pick = mutableCandidates.random()
                selected.add(pick.first)
                mutableCandidates.remove(pick)
            } else {
                val randomValue = Random.nextDouble() * totalScore
                var currentSum = 0.0
                
                val pick = mutableCandidates.firstOrNull { 
                    currentSum += it.second
                    currentSum >= randomValue
                } ?: mutableCandidates.last()
                
                selected.add(pick.first)
                mutableCandidates.remove(pick)
            }
        }
        
        return selected
    }
    
    /**
     * Crea la rutina y sus ejercicios con sets/reps óptimos
     * Usa periodización DUP cuando se proporciona una fase
     */
    private suspend fun createRoutineWithExercises(
        userId: Long,
        name: String,
        exercises: List<ExerciseEntity>,
        goal: FitnessGoal,
        level: FitnessLevel,
        duration: Int,
        phase: TrainingPhase? = null
    ): Long {
        // Crear rutina
        val routineId = routineRepository.insertRoutine(
            RoutineEntity(
                id = 0,
                userId = userId,
                name = name,
                description = if (phase != null) "${phase.spanishName}: ${getPhaseDescription(phase)}" else "Generado automáticamente",
                daysOfWeek = "[]",
                isActive = true,
                isAIGenerated = true,
                duration = duration,
                focusArea = "Generado",
                difficulty = level.toSpanish(),
                createdAt = System.currentTimeMillis()
            )
        )
        
        // Agregar ejercicios con parámetros optimizados según fase DUP
        exercises.forEachIndexed { index, exercise ->
            val isCompound = exercise.exerciseType == "Compuesto"
            
            val (sets, repsRange, restTime) = if (phase != null) {
                // Usar periodización DUP
                val vol = smartAnalyzer.calculateVolumeForPhase(phase, level, isCompound)
                Triple(vol.sets, "${vol.repsMin}-${vol.repsMax}", vol.rest)
            } else {
                // Método legacy basado en objetivo
                val (s, r) = calculateOptimalVolume(goal, level, exercise.exerciseType)
                Triple(s, "$r", calculateRestTime(goal, exercise.exerciseType, level))
            }
            
            routineRepository.addExerciseToRoutine(
                RoutineExerciseEntity(
                    routineId = routineId,
                    exerciseId = exercise.id,
                    orderIndex = index,
                    plannedSets = sets,
                    plannedReps = repsRange,
                    restTimeSeconds = restTime
                )
            )
        }
        
        return routineId
    }

    /**
     * Descripción de cada fase de entrenamiento DUP
     */
    private fun getPhaseDescription(phase: TrainingPhase): String = when (phase) {
        TrainingPhase.STRENGTH -> "Peso pesado, pocas reps. Desarrolla fuerza máxima."
        TrainingPhase.HYPERTROPHY -> "Peso moderado, reps medianas. Maximiza crecimiento muscular."
        TrainingPhase.ENDURANCE -> "Peso ligero, muchas reps. Mejora resistencia muscular."
        TrainingPhase.DELOAD -> "Semana de descarga. Reduce volumen para recuperación."
    }
    
    /**
     * Calcula volumen óptimo (series x reps) según objetivo usando IA
     */
    private fun calculateOptimalVolume(
        goal: FitnessGoal,
        level: FitnessLevel,
        exerciseType: String
    ): Pair<Int, Int> {
        val isCompound = exerciseType == "Compuesto"
        
        // Usar el analizador inteligente para recomendaciones científicas
        val volumeRec = smartAnalyzer.calculateOptimalVolume(
            goal = goal,
            level = level,
            isCompound = isCompound,
            weekNumber = 1 // Podrías implementar periodización aquí
        )
        
        // Devolver el punto medio del rango de reps
        val avgReps = (volumeRec.repsMin + volumeRec.repsMax) / 2
        return Pair(volumeRec.sets, avgReps)
    }
    
    /**
     * Calcula tiempo de descanso óptimo usando IA
     */
    private fun calculateRestTime(goal: FitnessGoal, exerciseType: String, level: FitnessLevel): Int {
        val isCompound = exerciseType == "Compuesto"
        
        // Usar el analizador inteligente
        val volumeRec = smartAnalyzer.calculateOptimalVolume(
            goal = goal,
            level = level,
            isCompound = isCompound
        )
        
        return volumeRec.rest
    }
    
    // Funciones de ayuda
    
    /**
     * Filtra ejercicios por equipamiento disponible
     * Función auxiliar para evitar problemas de resolución del compilador Kotlin con lambdas anidados
     */
    private fun filterByEquipment(
        exercises: List<ExerciseEntity>,
        equipment: AvailableEquipment
    ): List<ExerciseEntity> {
        return exercises.filter { ex ->
            when (equipment) {
                AvailableEquipment.BODYWEIGHT_ONLY -> ex.equipmentNeeded == "Peso Corporal" || ex.equipmentNeeded == "bodyweight"
                AvailableEquipment.HOME_BASIC -> ex.equipmentNeeded in listOf("Mancuernas", "Barra", "Peso Corporal", "dumbbell", "barbell", "bodyweight")
                AvailableEquipment.MINIMAL -> ex.equipmentNeeded in listOf("Mancuernas", "Peso Corporal", "dumbbell", "bodyweight")
                AvailableEquipment.FULL_GYM -> true
            }
        }
    }
    
    /**
     * Filtra ejercicios excluyendo aquellos contraindicados por limitaciones físicas
     */
    private fun filterByPhysicalLimitations(
        exercises: List<ExerciseEntity>,
        limitations: List<com.gymcompanion.app.domain.model.PhysicalLimitation>
    ): List<ExerciseEntity> {
        if (limitations.isEmpty()) return exercises
        
        // Obtener todos los ejercicios a excluir
        val excludedExercises = limitations.flatMap { it.affectedExercises }.map { it.lowercase() }
        
        return exercises.filter { exercise ->
            val exerciseName = exercise.name.lowercase()
            // Excluir si el nombre contiene algún término prohibido
            !excludedExercises.any { excluded -> exerciseName.contains(excluded) }
        }
    }
    
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
    private fun calculateTargetExerciseCount(durationMinutes: Int): Int {
        return when {
            durationMinutes <= 30 -> 4  // Sesión rápida (3-4 ejercicios)
            durationMinutes <= 45 -> 5  // Sesión media (4-5 ejercicios)
            durationMinutes <= 60 -> 6  // Sesión estándar (5-6 ejercicios)
            durationMinutes <= 75 -> 7  // Sesión larga (6-7 ejercicios)
            else -> 8                   // Sesión muy larga (7-8 ejercicios)
        }
    }
}
