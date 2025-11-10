package com.gymcompanion.app.data.local.dao

import androidx.room.*
import com.gymcompanion.app.data.local.entity.RoutineEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para rutinas de entrenamiento
 */
@Dao
interface RoutineDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long
    
    @Update
    suspend fun updateRoutine(routine: RoutineEntity)
    
    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)
    
    @Query("SELECT * FROM routines WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllRoutinesByUser(userId: Long): Flow<List<RoutineEntity>>
    
    @Query("SELECT * FROM routines WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveRoutinesByUser(userId: Long): Flow<List<RoutineEntity>>
    
    @Query("SELECT * FROM routines WHERE id = :routineId")
    fun getRoutineById(routineId: Long): Flow<RoutineEntity?>
    
    @Query("SELECT * FROM routines WHERE userId = :userId AND difficulty = :difficulty ORDER BY name ASC")
    fun getRoutinesByDifficulty(userId: Long, difficulty: String): Flow<List<RoutineEntity>>
    
    @Query("SELECT * FROM routines WHERE userId = :userId AND focusArea = :focusArea ORDER BY name ASC")
    fun getRoutinesByFocusArea(userId: Long, focusArea: String): Flow<List<RoutineEntity>>
    
    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutineById(routineId: Long)
    
    // Routine Exercise operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(routineExercise: RoutineExerciseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>)
    
    @Delete
    suspend fun deleteRoutineExercise(routineExercise: RoutineExerciseEntity)
    
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getExercisesForRoutine(routineId: Long): Flow<List<RoutineExerciseEntity>>
    
    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteAllExercisesFromRoutine(routineId: Long)
    
    @Query("SELECT COUNT(*) FROM routine_exercises WHERE routineId = :routineId")
    suspend fun getExerciseCountForRoutine(routineId: Long): Int
}
