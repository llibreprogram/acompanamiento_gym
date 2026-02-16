package com.gymcompanion.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
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
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("GymCompanionApplication", "onCreate() called")
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
