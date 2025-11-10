package com.gymcompanion.app.domain.repository

import com.gymcompanion.app.data.local.entity.BodyMetricsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para métricas corporales
 * Siguiendo Clean Architecture, define el contrato entre dominio y datos
 */
interface BodyMetricsRepository {
    
    suspend fun insertMetrics(metrics: BodyMetricsEntity): Long
    
    suspend fun updateMetrics(metrics: BodyMetricsEntity)
    
    fun getLatestMetricsByUser(userId: Long): Flow<BodyMetricsEntity?>
    
    fun getAllMetricsByUser(userId: Long): Flow<List<BodyMetricsEntity>>
    
    fun getMetricsInDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<BodyMetricsEntity>>
    
    suspend fun deleteMetrics(metrics: BodyMetricsEntity)
}
