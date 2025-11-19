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
    
    override suspend fun fetchExercisesFromAPI(limit: Int, cursor: String?): Result<com.gymcompanion.app.data.remote.model.ExerciseDBResponse> {
        return try {
            val response = apiService.getAllExercises(limit, cursor)
            if (response.isSuccessful) {
                val exerciseResponse = response.body()
                if (exerciseResponse?.success == true) {
                    Result.success(exerciseResponse)
                } else {
                    Result.failure(Exception("API Error: Response not successful"))
                }
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
        return syncExercisesToLocal(10000) // Sincronizar todos los ejercicios disponibles
    }

    override suspend fun syncExercisesToLocal(limit: Int): Result<Int> {
        return try {
            _syncStatus.value = SyncStatus.InProgress(0, limit)

            val allExercises = mutableListOf<ExerciseDBExercise>()
            var currentCursor: String? = null
            val pageLimit = 100 // Tamaño de página fijo
            var hasMore = true

            while (hasMore && allExercises.size < limit) {
                val result = fetchExercisesFromAPI(pageLimit, currentCursor)
                result.fold(
                    onSuccess = { response ->
                        val exercises = response.data
                        if (exercises.isNotEmpty()) {
                            allExercises.addAll(exercises)
                            _syncStatus.value = SyncStatus.InProgress(allExercises.size.coerceAtMost(limit), limit)
                            if (response.meta.hasNextPage) {
                                currentCursor = response.meta.nextCursor
                            } else {
                                hasMore = false
                            }
                        } else {
                            hasMore = false
                        }
                    },
                    onFailure = { exception ->
                        _syncStatus.value = SyncStatus.Error(exception.message ?: "Error desconocido")
                        return Result.failure(exception)
                    }
                )
            }

            val limitedExercises = allExercises.take(limit)
            val localExercises = ExerciseDBMapper.toExerciseEntities(limitedExercises)
            exerciseDao.insertExercises(localExercises)
            _syncStatus.value = SyncStatus.Success(localExercises.size)
            Result.success(localExercises.size)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: "Error en sincronización")
            Result.failure(e)
        }
    }
}
