package com.gymcompanion.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.gymcompanion.app.domain.usecase.ExerciseSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Clase de aplicación principal
 * Inicializa Hilt para inyección de dependencias y WorkManager
 */
@HiltAndroidApp
class GymCompanionApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var exerciseSyncManager: ExerciseSyncManager
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("GymCompanionApplication", "onCreate() called")
        
        // Programar sincronización periódica de ejercicios
        exerciseSyncManager.schedulePeriodicSync()
        android.util.Log.d("GymCompanionApplication", "Periodic sync scheduled")
        
        // TEMPORAL: Activar sincronización inmediata para pruebas
        // TODO: Remover después de las pruebas
        android.os.Handler(mainLooper).postDelayed({
            android.util.Log.d("GymCompanionApplication", "Starting immediate sync")
            exerciseSyncManager.syncNow()
        }, 2000) // Esperar 2 segundos para que la app se inicialice
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
