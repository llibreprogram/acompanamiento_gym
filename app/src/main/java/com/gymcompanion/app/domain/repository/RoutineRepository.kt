package com.gymcompanion.app.domain.repository

import com.gymcompanion.app.data.local.entity.RoutineEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import kotlinx.coroutines.flow.Flow

/**
 * Repository para gestión de rutinas de entrenamiento
 */
interface RoutineRepository {
    /**
     * Obtiene todas las rutinas del usuario
     */
    fun getAllRoutines(): Flow<List<RoutineWithExercises>>
    
    /**
     * Obtiene una rutina específica con sus ejercicios
     */
    fun getRoutineById(routineId: Long): Flow<RoutineWithExercises?>
    
    /**
     * Obtiene las rutinas activas
     */
    fun getActiveRoutines(): Flow<List<RoutineWithExercises>>
    
    /**
     * Obtiene las rutinas programadas para un día específico
     */
    fun getRoutinesForDay(dayOfWeek: String): Flow<List<RoutineWithExercises>>
    
    /**
     * Inserta una nueva rutina
     */
    suspend fun insertRoutine(routine: RoutineEntity): Long
    
    /**
     * Actualiza una rutina existente
     */
    suspend fun updateRoutine(routine: RoutineEntity)
    
    /**
     * Elimina una rutina
     */
    suspend fun deleteRoutine(routine: RoutineEntity)
    
    /**
     * Agrega un ejercicio a una rutina
     */
    suspend fun addExerciseToRoutine(routineExercise: RoutineExerciseEntity)
    
    /**
     * Actualiza un ejercicio en una rutina
     */
    suspend fun updateRoutineExercise(routineExercise: RoutineExerciseEntity)
    
    /**
     * Elimina un ejercicio de una rutina
     */
    suspend fun removeExerciseFromRoutine(routineExercise: RoutineExerciseEntity)
    
    /**
     * Obtiene los ejercicios de una rutina
     */
    fun getRoutineExercises(routineId: Long): Flow<List<RoutineExerciseEntity>>
    
    /**
     * Reordena los ejercicios de una rutina
     */
    suspend fun reorderExercises(routineId: Long, newOrder: List<Long>)
}
