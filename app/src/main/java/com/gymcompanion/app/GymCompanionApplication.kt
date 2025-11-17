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
        
        // Programar sincronización periódica de ejercicios
        exerciseSyncManager.schedulePeriodicSync()
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
