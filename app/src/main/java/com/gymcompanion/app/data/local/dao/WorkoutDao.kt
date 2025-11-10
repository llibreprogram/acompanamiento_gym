package com.gymcompanion.app.data.local.dao

import androidx.room.*
import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para sesiones de entrenamiento y series de ejercicios
 */
@Dao
interface WorkoutDao {
    
    // Workout Session operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long
    
    @Update
    suspend fun updateWorkoutSession(session: WorkoutSessionEntity)
    
    @Delete
    suspend fun deleteWorkoutSession(session: WorkoutSessionEntity)
    
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllWorkoutSessionsByUser(userId: Long): Flow<List<WorkoutSessionEntity>>
    
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getWorkoutSessionById(sessionId: Long): Flow<WorkoutSessionEntity?>
    
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    fun getActiveWorkoutSession(userId: Long): Flow<WorkoutSessionEntity?>
    
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND startTime BETWEEN :startDate AND :endDate ORDER BY startTime ASC")
    fun getWorkoutSessionsInDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<WorkoutSessionEntity>>
    
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND isCompleted = 1 ORDER BY startTime DESC LIMIT :limit")
    fun getRecentCompletedWorkouts(userId: Long, limit: Int = 10): Flow<List<WorkoutSessionEntity>>
    
    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteWorkoutSessionById(sessionId: Long)
    
    // Exercise Set operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSet(set: ExerciseSetEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSets(sets: List<ExerciseSetEntity>)
    
    @Update
    suspend fun updateExerciseSet(set: ExerciseSetEntity)
    
    @Delete
    suspend fun deleteExerciseSet(set: ExerciseSetEntity)
    
    @Query("SELECT * FROM exercise_sets WHERE workoutSessionId = :sessionId ORDER BY performedAt ASC")
    fun getSetsForWorkoutSession(sessionId: Long): Flow<List<ExerciseSetEntity>>
    
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId ORDER BY performedAt DESC LIMIT :limit")
    fun getRecentSetsForExercise(exerciseId: Long, limit: Int = 20): Flow<List<ExerciseSetEntity>>
    
    @Query("SELECT * FROM exercise_sets WHERE workoutSessionId = :sessionId AND exerciseId = :exerciseId ORDER BY setNumber ASC")
    fun getSetsForExerciseInSession(sessionId: Long, exerciseId: Long): Flow<List<ExerciseSetEntity>>
    
    @Query("SELECT MAX(weightUsed) FROM exercise_sets WHERE exerciseId = :exerciseId AND isWarmUpSet = 0")
    suspend fun getPersonalRecordForExercise(exerciseId: Long): Double?
    
    @Query("DELETE FROM exercise_sets WHERE workoutSessionId = :sessionId")
    suspend fun deleteAllSetsForSession(sessionId: Long)
    
    /**
     * Estadísticas de entrenamiento
     */
    @Query("SELECT COUNT(*) FROM workout_sessions WHERE userId = :userId AND isCompleted = 1")
    fun getTotalWorkoutCount(userId: Long): Flow<Int>
    
    @Query("SELECT SUM(durationMinutes) FROM workout_sessions WHERE userId = :userId AND isCompleted = 1")
    fun getTotalWorkoutMinutes(userId: Long): Flow<Int?>
    
    @Query("SELECT COUNT(DISTINCT DATE(startTime/1000, 'unixepoch')) FROM workout_sessions WHERE userId = :userId AND isCompleted = 1 AND startTime >= :sinceDate")
    fun getWorkoutDaysSince(userId: Long, sinceDate: Long): Flow<Int>
}
