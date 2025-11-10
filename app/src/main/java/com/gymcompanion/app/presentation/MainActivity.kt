package com.gymcompanion.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gymcompanion.app.presentation.navigation.GymCompanionNavigation
import com.gymcompanion.app.presentation.theme.GymCompanionTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de la aplicación
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GymCompanionNavigation()
                }
            }
        }
    }
}
