package com.gymcompanion.app.data.local.dao

import androidx.room.*
import com.gymcompanion.app.data.local.entity.BodyMetricsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para métricas corporales del usuario
 */
@Dao
interface BodyMetricsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetrics(metrics: BodyMetricsEntity): Long
    
    @Update
    suspend fun updateMetrics(metrics: BodyMetricsEntity)
    
    @Delete
    suspend fun deleteMetrics(metrics: BodyMetricsEntity)
    
    @Query("SELECT * FROM body_metrics WHERE userId = :userId ORDER BY recordedAt DESC")
    fun getAllMetricsByUser(userId: Long): Flow<List<BodyMetricsEntity>>
    
    @Query("SELECT * FROM body_metrics WHERE userId = :userId ORDER BY recordedAt DESC LIMIT 1")
    fun getLatestMetricsByUser(userId: Long): Flow<BodyMetricsEntity?>
    
    @Query("SELECT * FROM body_metrics WHERE userId = :userId AND recordedAt BETWEEN :startDate AND :endDate ORDER BY recordedAt ASC")
    fun getMetricsInDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<BodyMetricsEntity>>
    
    @Query("SELECT * FROM body_metrics WHERE id = :metricsId")
    suspend fun getMetricsById(metricsId: Long): BodyMetricsEntity?
    
    @Query("DELETE FROM body_metrics WHERE userId = :userId")
    suspend fun deleteAllMetricsForUser(userId: Long)
    
    /**
     * Obtiene el peso histórico del usuario
     */
    @Query("SELECT weight, recordedAt FROM body_metrics WHERE userId = :userId ORDER BY recordedAt ASC")
    fun getWeightHistory(userId: Long): Flow<List<WeightRecord>>
    
    /**
     * Obtiene el historial de IMC
     */
    @Query("SELECT bmi, recordedAt FROM body_metrics WHERE userId = :userId ORDER BY recordedAt ASC")
    fun getBMIHistory(userId: Long): Flow<List<BMIRecord>>
}

/**
 * Data classes para queries específicas
 */
data class WeightRecord(
    val weight: Double,
    val recordedAt: Long
)

data class BMIRecord(
    val bmi: Double,
    val recordedAt: Long
)
