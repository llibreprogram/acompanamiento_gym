package com.gymcompanion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymcompanion.app.data.local.entity.TrainingPhaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingPhaseDao {
    @Query("SELECT * FROM training_phases WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun getCurrentPhase(userId: Long): Flow<TrainingPhaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase(phase: TrainingPhaseEntity): Long
}
