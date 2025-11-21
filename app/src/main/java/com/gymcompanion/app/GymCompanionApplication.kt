package com.gymcompanion.app

import android.app.Application


import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Clase de aplicación principal
 * Inicializa Hilt para inyección de dependencias y WorkManager
 */
@HiltAndroidApp
class GymCompanionApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("GymCompanionApplication", "onCreate() called")
    }
}
