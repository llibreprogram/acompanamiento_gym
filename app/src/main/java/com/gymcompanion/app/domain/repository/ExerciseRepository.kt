package com.gymcompanion.app.domain.repository

import com.gymcompanion.app.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para ejercicios
 */
interface ExerciseRepository {
    
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    
    fun getExerciseById(exerciseId: Long): Flow<ExerciseEntity?>
    
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>
    
    fun getExercisesByDifficulty(difficulty: String): Flow<List<ExerciseEntity>>
    
    fun getExercisesByEquipment(equipment: String): Flow<List<ExerciseEntity>>
    
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>
    
    fun getAllMuscleGroups(): Flow<List<String>>
    
    suspend fun insertExercise(exercise: ExerciseEntity): Long
    
    suspend fun updateExercise(exercise: ExerciseEntity)
    
    suspend fun deleteExercise(exercise: ExerciseEntity)
}
