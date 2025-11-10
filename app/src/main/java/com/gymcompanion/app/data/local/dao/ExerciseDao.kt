package com.gymcompanion.app.data.local.dao

import androidx.room.*
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para ejercicios
 */
@Dao
interface ExerciseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
    
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)
    
    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
    
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    fun getExerciseById(exerciseId: Long): Flow<ExerciseEntity?>
    
    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE difficulty = :difficulty ORDER BY name ASC")
    fun getExercisesByDifficulty(difficulty: String): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE equipmentNeeded = :equipment ORDER BY name ASC")
    fun getExercisesByEquipment(equipment: String): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE isCustom = :isCustom ORDER BY name ASC")
    fun getCustomExercises(isCustom: Boolean = true): Flow<List<ExerciseEntity>>
    
    @Query("SELECT DISTINCT muscleGroup FROM exercises ORDER BY muscleGroup ASC")
    fun getAllMuscleGroups(): Flow<List<String>>
    
    @Query("SELECT DISTINCT equipmentNeeded FROM exercises ORDER BY equipmentNeeded ASC")
    fun getAllEquipmentTypes(): Flow<List<String>>
    
    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExerciseById(exerciseId: Long)
}
