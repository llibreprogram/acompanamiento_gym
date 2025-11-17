package com.gymcompanion.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.gymcompanion.app.domain.repository.ExerciseDBRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker para sincronizar ejercicios de ExerciseDB API a la base de datos local
 * 
 * Se ejecuta en background periódicamente o bajo demanda
 * Reporta progreso en tiempo real
 */
@HiltWorker
class ExerciseSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val exerciseDBRepository: ExerciseDBRepository
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val WORK_NAME = "exercise_sync_work"
        const val PROGRESS_KEY = "progress"
        const val TOTAL_KEY = "total"
        const val STATUS_KEY = "status"
        const val MESSAGE_KEY = "message"
        
        // Estados de sincronización
        const val STATUS_STARTING = "starting"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_SUCCESS = "success"
        const val STATUS_ERROR = "error"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Reportar inicio
            setProgress(workDataOf(
                STATUS_KEY to STATUS_STARTING,
                MESSAGE_KEY to "Iniciando sincronización..."
            ))
            
            // Realizar la sincronización
            val result = exerciseDBRepository.syncExercisesToLocal()
            
            result.fold(
                onSuccess = { exercisesSynced ->
                    setProgress(workDataOf(
                        STATUS_KEY to STATUS_SUCCESS,
                        TOTAL_KEY to exercisesSynced,
                        MESSAGE_KEY to "Sincronizados $exercisesSynced ejercicios"
                    ))
                    // Solo pares clave-valor simples
                    return@withContext Result.success(workDataOf(
                        TOTAL_KEY to exercisesSynced,
                        MESSAGE_KEY to "Sincronización completada exitosamente"
                    ))
                },
                onFailure = { exception ->
                    setProgress(workDataOf(
                        STATUS_KEY to STATUS_ERROR,
                        MESSAGE_KEY to "Error: ${exception.message}"
                    ))
                    if (exception is java.io.IOException || 
                        exception.message?.contains("timeout", ignoreCase = true) == true) {
                        return@withContext Result.retry()
                    } else {
                        // Sintaxis correcta para workDataOf
                        return@withContext Result.failure(workDataOf(
                            MESSAGE_KEY to (exception.message ?: "Error desconocido")
                        ))
                    }
                }
            )
        } catch (e: Exception) {
            setProgress(workDataOf(
                STATUS_KEY to STATUS_ERROR,
                MESSAGE_KEY to "Error inesperado: ${e.message}"
            ))
            
            return Result.failure(workDataOf(
                MESSAGE_KEY to (e.message ?: "Error inesperado")
            ))
        }
    }
}
