package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.local.entity.RoutineEntity
import com.gymcompanion.app.domain.repository.ExerciseRepository
import com.gymcompanion.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Caso de uso para adaptar rutinas basado en el progreso del usuario
 */
class AdaptiveRoutineUseCase @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    private val progressAnalyzer: ProgressAnalyzer
) {

    data class AdaptationSuggestion(
        val originalExerciseId: Long,
        val suggestedExercise: ExerciseEntity,
        val reason: String
    )

    /**
     * Analiza una rutina y sugiere cambios si hay estancamiento
     */
    suspend fun analyzeAndAdaptRoutine(routineId: Long, userId: Long): List<AdaptationSuggestion> {
        val routineWithExercises = routineRepository.getRoutineById(routineId).first() ?: return emptyList()
        val suggestions = mutableListOf<AdaptationSuggestion>()
        val allExercises = exerciseRepository.getAllExercises().first()

        routineWithExercises.routineExercises.forEach { routineExerciseWithExercise ->
            val routineExercise = routineExerciseWithExercise.routineExercise
            val exerciseId = routineExercise.exerciseId
            
            // Verificar estancamiento
            if (progressAnalyzer.isStalled(exerciseId, userId)) {
                val currentExercise = allExercises.find { it.id == exerciseId }
                
                if (currentExercise != null) {
                    // Buscar sustituto
                    val substitute = findSubstitute(currentExercise, allExercises, routineWithExercises.routineExercises.map { it.routineExercise.exerciseId })
                    
                    if (substitute != null) {
                        suggestions.add(AdaptationSuggestion(
                            originalExerciseId = exerciseId,
                            suggestedExercise = substitute,
                            reason = "Estancamiento detectado. Cambiar el estímulo puede ayudar a progresar."
                        ))
                    }
                }
            }
        }
        
        return suggestions
    }

    /**
     * Busca un ejercicio sustituto similar
     */
    private fun findSubstitute(
        current: ExerciseEntity, 
        allExercises: List<ExerciseEntity>,
        currentRoutineExerciseIds: List<Long>
    ): ExerciseEntity? {
        return allExercises.filter { candidate ->
            candidate.id != current.id && // No el mismo
            !currentRoutineExerciseIds.contains(candidate.id) && // No ya en la rutina
            candidate.muscleGroup == current.muscleGroup && // Mismo grupo muscular
            candidate.exerciseType == current.exerciseType && // Mismo tipo (compuesto/aislamiento)
            candidate.difficulty == current.difficulty // Misma dificultad
        }.randomOrNull() // Elegir uno al azar de los candidatos válidos
    }
}
