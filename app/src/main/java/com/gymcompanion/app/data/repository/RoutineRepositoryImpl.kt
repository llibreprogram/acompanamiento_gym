package com.gymcompanion.app.data.repository

import com.gymcompanion.app.data.local.dao.RoutineDao
import com.gymcompanion.app.data.local.entity.RoutineEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import com.gymcompanion.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio de rutinas
 */
class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {
    
    override fun getAllRoutines(): Flow<List<RoutineWithExercises>> {
        return routineDao.getAllRoutinesWithExercises(1L)
    }
    
    override fun getRoutineById(routineId: Long): Flow<RoutineWithExercises?> {
        return routineDao.getRoutineWithExercisesById(routineId)
    }
    
    override fun getActiveRoutines(): Flow<List<RoutineWithExercises>> {
        return routineDao.getActiveRoutinesWithExercises(1L) // TODO: Get actual user ID
    }
    
    override fun getRoutinesForDay(dayOfWeek: String): Flow<List<RoutineWithExercises>> {
        return routineDao.getRoutinesForDayWithExercises(1L, dayOfWeek)
    }
    
    override suspend fun insertRoutine(routine: RoutineEntity): Long {
        return routineDao.insertRoutine(routine)
    }
    
    override suspend fun updateRoutine(routine: RoutineEntity) {
        routineDao.updateRoutine(routine)
    }
    
    override suspend fun deleteRoutine(routine: RoutineEntity) {
        routineDao.deleteRoutine(routine)
    }
    
    override suspend fun addExerciseToRoutine(routineExercise: RoutineExerciseEntity) {
        routineDao.insertRoutineExercise(routineExercise)
    }
    
    override suspend fun updateRoutineExercise(routineExercise: RoutineExerciseEntity) {
        routineDao.updateRoutineExercise(routineExercise)
    }
    
    override suspend fun removeExerciseFromRoutine(routineExercise: RoutineExerciseEntity) {
        routineDao.deleteRoutineExercise(routineExercise)
    }
    
    override fun getRoutineExercises(routineId: Long): Flow<List<RoutineExerciseEntity>> {
        return routineDao.getExercisesForRoutine(routineId)
    }
    
    override suspend fun reorderExercises(routineId: Long, newOrder: List<Long>) {
        newOrder.forEachIndexed { index, exerciseId ->
            routineDao.updateExerciseOrder(routineId, exerciseId, index)
        }
    }
}
