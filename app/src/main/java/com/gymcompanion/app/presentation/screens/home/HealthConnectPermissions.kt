package com.gymcompanion.app.presentation.screens.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient

/**
 * Helper for opening Health Connect
 * Note: Permission requests in debug builds may show "App update needed" error.
 * This is a known limitation of Health Connect with debug signatures.
 */
@Composable
fun rememberHealthConnectPermissionsLauncher(
    onPermissionsGranted: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    
    return remember {
        {
            android.util.Log.d("HealthConnect", "Opening Health Connect app")
            try {
                // Try to open Health Connect app directly
                val packageManager = context.packageManager
                val healthConnectIntent = packageManager.getLaunchIntentForPackage("com.google.android.apps.healthdata")
                
                if (healthConnectIntent != null) {
                    android.util.Log.d("HealthConnect", "Launching Health Connect app")
                    context.startActivity(healthConnectIntent)
                    
                    // Show a toast to guide the user
                    android.widget.Toast.makeText(
                        context,
                        "Abre 'Permisos de apps' en Health Connect y autoriza Gym Companion",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } else {
                    android.util.Log.d("HealthConnect", "Health Connect not installed")
                    android.widget.Toast.makeText(
                        context,
                        "Health Connect no está instalado. Instalando...",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    openHealthConnectInPlayStore(context)
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "Error opening Health Connect", e)
                android.widget.Toast.makeText(
                    context,
                    "Error al abrir Health Connect",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

/**
 * Check if Health Connect is installed
 */
fun isHealthConnectInstalled(context: Context): Boolean {
    return try {
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    } catch (e: Exception) {
        false
    }
}

/**
 * Open Health Connect app in Play Store
 */
fun openHealthConnectInPlayStore(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            setPackage("com.android.vending")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to browser if Play Store is not available
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
        }
        context.startActivity(intent)
    }
}
