package com.gymcompanion.app.data.repository

import com.gymcompanion.app.data.local.dao.ExerciseDao
import com.gymcompanion.app.data.remote.api.ExerciseDBApiService
import com.gymcompanion.app.data.remote.mapper.ExerciseDBMapper
import com.gymcompanion.app.data.remote.model.ExerciseDBExercise
import com.gymcompanion.app.domain.repository.ExerciseDBRepository
import com.gymcompanion.app.domain.repository.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de ExerciseDB
 * Maneja la sincronización entre API y base de datos local
 */
@Singleton
class ExerciseDBRepositoryImpl @Inject constructor(
    private val apiService: ExerciseDBApiService,
    private val exerciseDao: ExerciseDao
) : ExerciseDBRepository {
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    
    override fun getSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()
    
    override suspend fun fetchExercisesFromAPI(page: Int, pageSize: Int): Result<List<ExerciseDBExercise>> {
        return try {
            val response = apiService.getAllExercises(page, pageSize)
            
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchExercisesFromAPI(query: String): Result<List<ExerciseDBExercise>> {
        return try {
            val response = apiService.searchExercises(query)
            
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun syncExercisesToLocal(): Result<Int> {
        return try {
            _syncStatus.value = SyncStatus.InProgress(0, 100)
            
            var totalSynced = 0
            var currentPage = 1
            val pageSize = 100
            var hasMore = true
            
            while (hasMore) {
                val result = fetchExercisesFromAPI(currentPage, pageSize)
                
                result.fold(
                    onSuccess = { exercises ->
                        if (exercises.isEmpty()) {
                            hasMore = false
                        } else {
                            // Convertir y guardar en base de datos local
                            val localExercises = ExerciseDBMapper.toExerciseEntities(exercises)
                            exerciseDao.insertExercises(localExercises)
                            
                            totalSynced += exercises.size
                            _syncStatus.value = SyncStatus.InProgress(totalSynced, totalSynced + pageSize)
                            
                            currentPage++
                            
                            // Limitar a 10 páginas por ahora (1000 ejercicios)
                            // Puedes ajustar esto según necesites
                            if (currentPage > 10) {
                                hasMore = false
                            }
                        }
                    },
                    onFailure = { exception ->
                        _syncStatus.value = SyncStatus.Error(exception.message ?: "Error desconocido")
                        return Result.failure(exception)
                    }
                )
            }
            
            _syncStatus.value = SyncStatus.Success(totalSynced)
            Result.success(totalSynced)
            
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: "Error en sincronización")
            Result.failure(e)
        }
    }
}
