package com.gymcompanion.app.domain.repository

import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.remote.model.ExerciseDBExercise
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para sincronización con ExerciseDB API
 */
interface ExerciseDBRepository {
    
    /**
     * Obtiene ejercicios desde la API (paginado offset-based)
     */
    suspend fun fetchExercisesFromAPI(limit: Int = 100, offset: Int = 0): Result<com.gymcompanion.app.data.remote.model.ExerciseDBResponse>
    
    /**
     * Busca ejercicios en la API
     */
    suspend fun searchExercisesFromAPI(query: String): Result<List<ExerciseDBExercise>>
    
    /**
     * Sincroniza ejercicios de la API a la base de datos local
     * @return número de ejercicios sincronizados
     */
    suspend fun syncExercisesToLocal(): Result<Int>
    
    /**
     * Sincroniza ejercicios de la API a la base de datos local con límite
     * @param limit número máximo de ejercicios a sincronizar
     * @return número de ejercicios sincronizados
     */
    suspend fun syncExercisesToLocal(limit: Int): Result<Int>
    
    /**
     * Obtiene el estado de la sincronización
     */
    fun getSyncStatus(): Flow<SyncStatus>
}

/**
 * Estados de sincronización
 */
sealed class SyncStatus {
    object Idle : SyncStatus()
    data class InProgress(val progress: Int, val total: Int) : SyncStatus()
    data class Success(val exercisesSynced: Int) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
