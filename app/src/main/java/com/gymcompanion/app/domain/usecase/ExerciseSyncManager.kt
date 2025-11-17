package com.gymcompanion.app.domain.usecase

import android.content.Context
import androidx.work.*
import com.gymcompanion.app.worker.ExerciseSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para controlar la sincronización de ejercicios
 * Configura WorkManager y expone estados de sincronización
 */
@Singleton
class ExerciseSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    
    companion object {
        private const val SYNC_WORK_TAG = "exercise_sync"
        private const val PERIODIC_SYNC_WORK_NAME = "periodic_exercise_sync"
        
        // Sincronización cada 7 días
        private const val SYNC_INTERVAL_DAYS = 7L
    }
    
    /**
     * Programa sincronización periódica automática
     * Se ejecuta cada 7 días solo con WiFi y suficiente batería
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Solo WiFi
            .setRequiresBatteryNotLow(true) // Batería suficiente
            .setRequiresStorageNotLow(true) // Espacio suficiente
            .build()
        
        val periodicSyncRequest = PeriodicWorkRequestBuilder<ExerciseSyncWorker>(
            repeatInterval = SYNC_INTERVAL_DAYS,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag(SYNC_WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }
    
    /**
     * Ejecuta sincronización manual inmediata
     */
    fun syncNow(): Flow<SyncState> {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Cualquier red
            .build()
        
        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<ExerciseSyncWorker>()
            .setConstraints(constraints)
            .addTag(SYNC_WORK_TAG)
            .build()
        
        workManager.enqueueUniqueWork(
            ExerciseSyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeSyncRequest
        )
        
        return observeSyncState(oneTimeSyncRequest.id)
    }
    
    /**
     * Cancela todas las sincronizaciones
     */
    fun cancelAllSync() {
        workManager.cancelAllWorkByTag(SYNC_WORK_TAG)
    }
    
    /**
     * Cancela sincronización periódica
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }
    
    /**
     * Observa el estado de una sincronización específica
     */
    private fun observeSyncState(workId: java.util.UUID): Flow<SyncState> {
        return workManager.getWorkInfoByIdFlow(workId).map { workInfo ->
            when {
                workInfo == null -> SyncState.Idle
                
                workInfo.state == WorkInfo.State.ENQUEUED -> 
                    SyncState.Queued
                
                workInfo.state == WorkInfo.State.RUNNING -> {
                    val progress = workInfo.progress
                    val status = progress.getString(ExerciseSyncWorker.STATUS_KEY)
                    val message = progress.getString(ExerciseSyncWorker.MESSAGE_KEY) ?: ""
                    val total = progress.getInt(ExerciseSyncWorker.TOTAL_KEY, 0)
                    
                    when (status) {
                        ExerciseSyncWorker.STATUS_STARTING -> 
                            SyncState.Starting(message)
                        ExerciseSyncWorker.STATUS_IN_PROGRESS -> 
                            SyncState.InProgress(message, total)
                        else -> 
                            SyncState.InProgress("Sincronizando...", 0)
                    }
                }
                
                workInfo.state == WorkInfo.State.SUCCEEDED -> {
                    val outputData = workInfo.outputData
                    val total = outputData.getInt(ExerciseSyncWorker.TOTAL_KEY, 0)
                    val message = outputData.getString(ExerciseSyncWorker.MESSAGE_KEY) ?: 
                        "Sincronizados $total ejercicios"
                    SyncState.Success(total, message)
                }
                
                workInfo.state == WorkInfo.State.FAILED -> {
                    val message = workInfo.outputData.getString(ExerciseSyncWorker.MESSAGE_KEY) 
                        ?: "Error en sincronización"
                    SyncState.Error(message)
                }
                
                workInfo.state == WorkInfo.State.CANCELLED -> 
                    SyncState.Cancelled
                
                else -> SyncState.Idle
            }
        }
    }
    
    /**
     * Observa el estado general de sincronización
     */
    fun observeGeneralSyncState(): Flow<SyncState> {
        return workManager.getWorkInfosByTagFlow(SYNC_WORK_TAG).map { workInfoList ->
            val activeWork = workInfoList.firstOrNull { 
                it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
            }
            
            when {
                activeWork == null -> SyncState.Idle
                activeWork.state == WorkInfo.State.ENQUEUED -> SyncState.Queued
                activeWork.state == WorkInfo.State.RUNNING -> {
                    val progress = activeWork.progress
                    val message = progress.getString(ExerciseSyncWorker.MESSAGE_KEY) ?: "Sincronizando..."
                    val total = progress.getInt(ExerciseSyncWorker.TOTAL_KEY, 0)
                    SyncState.InProgress(message, total)
                }
                else -> SyncState.Idle
            }
        }
    }
}

/**
 * Estados posibles de sincronización
 */
sealed class SyncState {
    object Idle : SyncState()
    object Queued : SyncState()
    data class Starting(val message: String) : SyncState()
    data class InProgress(val message: String, val exercisesSynced: Int) : SyncState()
    data class Success(val totalExercises: Int, val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
    object Cancelled : SyncState()
}
