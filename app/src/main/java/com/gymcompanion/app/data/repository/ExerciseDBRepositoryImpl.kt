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
            val response = apiService.getAllExercises()
            if (response.isSuccessful) {
                val exercises = response.body() ?: emptyList()
                Result.success(exercises)
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
            _syncStatus.value = SyncStatus.InProgress(0, 1)

            val result = fetchExercisesFromAPI(1, 1)
            result.fold(
                onSuccess = { exercises ->
                    val localExercises = ExerciseDBMapper.toExerciseEntities(exercises)
                    exerciseDao.insertExercises(localExercises)
                    _syncStatus.value = SyncStatus.InProgress(localExercises.size, localExercises.size)
                    _syncStatus.value = SyncStatus.Success(localExercises.size)
                    Result.success(localExercises.size)
                },
                onFailure = { exception ->
                    _syncStatus.value = SyncStatus.Error(exception.message ?: "Error desconocido")
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: "Error en sincronización")
            Result.failure(e)
        }
    }
}
