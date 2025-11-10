package com.gymcompanion.app.data.repository

import com.gymcompanion.app.data.local.dao.ExerciseDao
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de ejercicios
 */
@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {
    
    override fun getAllExercises(): Flow<List<ExerciseEntity>> {
        return exerciseDao.getAllExercises()
    }
    
    override fun getExerciseById(exerciseId: Long): Flow<ExerciseEntity?> {
        return exerciseDao.getExerciseById(exerciseId)
    }
    
    override fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByMuscleGroup(muscleGroup)
    }
    
    override fun getExercisesByDifficulty(difficulty: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByDifficulty(difficulty)
    }
    
    override fun getExercisesByEquipment(equipment: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByEquipment(equipment)
    }
    
    override fun searchExercises(query: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.searchExercises(query)
    }
    
    override fun getAllMuscleGroups(): Flow<List<String>> {
        return exerciseDao.getAllMuscleGroups()
    }
    
    override suspend fun insertExercise(exercise: ExerciseEntity): Long {
        return exerciseDao.insertExercise(exercise)
    }
    
    override suspend fun updateExercise(exercise: ExerciseEntity) {
        exerciseDao.updateExercise(exercise)
    }
    
    override suspend fun deleteExercise(exercise: ExerciseEntity) {
        exerciseDao.deleteExercise(exercise)
    }
}
