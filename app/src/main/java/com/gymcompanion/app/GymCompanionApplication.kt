package com.gymcompanion.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase de aplicación principal
 * Inicializa Hilt para inyección de dependencias
 */
@HiltAndroidApp
class GymCompanionApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Aquí se pueden inicializar bibliotecas adicionales si es necesario
    }
}
