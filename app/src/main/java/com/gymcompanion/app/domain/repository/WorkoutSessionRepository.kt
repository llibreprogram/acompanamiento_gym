package com.gymcompanion.app.domain.repository

import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.data.local.entity.WorkoutSessionWithSets
import kotlinx.coroutines.flow.Flow

/**
 * Repository para gestión de sesiones de entrenamiento
 */
interface WorkoutSessionRepository {
    /**
     * Obtiene todas las sesiones de entrenamiento
     */
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>
    
    /**
     * Obtiene una sesión específica con sus sets
     */
    fun getSessionById(sessionId: Long): Flow<WorkoutSessionWithSets?>
    
    /**
     * Obtiene las sesiones de una rutina específica
     */
    fun getSessionsByRoutine(routineId: Long): Flow<List<WorkoutSessionEntity>>
    
    /**
     * Obtiene las sesiones en un rango de fechas
     */
    fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutSessionEntity>>
    
    /**
     * Obtiene la última sesión del usuario
     */
    fun getLastSession(): Flow<WorkoutSessionEntity?>
    
    /**
     * Obtiene las sesiones de la semana actual
     */
    fun getSessionsThisWeek(): Flow<List<WorkoutSessionEntity>>
    
    /**
     * Inserta una nueva sesión
     */
    suspend fun insertSession(session: WorkoutSessionEntity): Long
    
    /**
     * Actualiza una sesión existente
     */
    suspend fun updateSession(session: WorkoutSessionEntity)
    
    /**
     * Elimina una sesión
     */
    suspend fun deleteSession(session: WorkoutSessionEntity)
    
    /**
     * Inserta un set de ejercicio
     */
    suspend fun insertExerciseSet(exerciseSet: ExerciseSetEntity)
    
    /**
     * Actualiza un set de ejercicio
     */
    suspend fun updateExerciseSet(exerciseSet: ExerciseSetEntity)
    
    /**
     * Elimina un set de ejercicio
     */
    suspend fun deleteExerciseSet(exerciseSet: ExerciseSetEntity)
    
    /**
     * Obtiene todos los sets de una sesión
     */
    fun getSessionSets(sessionId: Long): Flow<List<ExerciseSetEntity>>
    
    /**
     * Obtiene los sets de un ejercicio específico en una sesión
     */
    fun getExerciseSetsInSession(sessionId: Long, exerciseId: Long): Flow<List<ExerciseSetEntity>>
    
    /**
     * Finaliza una sesión de entrenamiento
     */
    suspend fun completeSession(sessionId: Long, endTime: Long)
    
    /**
     * Calcula el volumen total de una sesión (peso x reps)
     */
    suspend fun calculateSessionVolume(sessionId: Long): Double

    /**
     * Obtiene el historial de sesiones donde se realizó un ejercicio específico
     */
    suspend fun getExerciseHistory(userId: Long, exerciseId: Long, limit: Int): List<WorkoutSessionWithSets>
}
