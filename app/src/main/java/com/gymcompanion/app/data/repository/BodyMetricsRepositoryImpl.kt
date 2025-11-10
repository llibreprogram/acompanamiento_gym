package com.gymcompanion.app.data.repository

import com.gymcompanion.app.data.local.dao.BodyMetricsDao
import com.gymcompanion.app.data.local.entity.BodyMetricsEntity
import com.gymcompanion.app.domain.repository.BodyMetricsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de métricas corporales
 * Actúa como intermediario entre el DAO y la capa de presentación
 */
@Singleton
class BodyMetricsRepositoryImpl @Inject constructor(
    private val bodyMetricsDao: BodyMetricsDao
) : BodyMetricsRepository {
    
    override suspend fun insertMetrics(metrics: BodyMetricsEntity): Long {
        return bodyMetricsDao.insertMetrics(metrics)
    }
    
    override suspend fun updateMetrics(metrics: BodyMetricsEntity) {
        bodyMetricsDao.updateMetrics(metrics)
    }
    
    override fun getLatestMetricsByUser(userId: Long): Flow<BodyMetricsEntity?> {
        return bodyMetricsDao.getLatestMetricsByUser(userId)
    }
    
    override fun getAllMetricsByUser(userId: Long): Flow<List<BodyMetricsEntity>> {
        return bodyMetricsDao.getAllMetricsByUser(userId)
    }
    
    override fun getMetricsInDateRange(
        userId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<BodyMetricsEntity>> {
        return bodyMetricsDao.getMetricsInDateRange(userId, startDate, endDate)
    }
    
    override suspend fun deleteMetrics(metrics: BodyMetricsEntity) {
        bodyMetricsDao.deleteMetrics(metrics)
    }
}
