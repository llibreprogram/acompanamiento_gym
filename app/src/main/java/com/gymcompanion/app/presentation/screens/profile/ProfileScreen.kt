package com.gymcompanion.app.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

/**
 * Pantalla de perfil del usuario
 * Muestra y permite editar datos corporales y preferencias
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val latestMetrics by viewModel.latestMetrics.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showBodyMetricsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (latestMetrics == null) {
                            "Completa tu perfil para obtener recomendaciones personalizadas"
                        } else {
                            "Usuario: ${currentUser?.name ?: "Desconocido"}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showBodyMetricsDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is ProfileUiState.Saving
                    ) {
                        if (uiState is ProfileUiState.Saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (latestMetrics == null) "Configurar Datos Corporales" else "Actualizar Datos")
                    }
                }
            }
            
            // Body metrics summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Datos Corporales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    latestMetrics?.let { metrics ->
                        MetricRow("Peso", "${metrics.weight} kg")
                        MetricRow("Altura", "${metrics.height} cm")
                        MetricRow("IMC", String.format("%.1f", metrics.bmi))
                        MetricRow("% Grasa", metrics.bodyFatPercentage?.let { "${it}%" } ?: "--")
                        MetricRow("Nivel", when(metrics.experienceLevel) {
                            "beginner" -> "Principiante"
                            "intermediate" -> "Intermedio"
                            "advanced" -> "Avanzado"
                            else -> "No especificado"
                        })
                        MetricRow("Edad", "${currentUser?.calculateAge() ?: "--"} años")
                    } ?: run {
                        MetricRow("Peso", "-- kg")
                        MetricRow("Altura", "-- cm")
                        MetricRow("IMC", "--")
                        MetricRow("% Grasa", "-- %")
                        MetricRow("Nivel", "No especificado")
                    }
                }
            }
            
            // Show error snackbar if needed
            LaunchedEffect(uiState) {
                if (uiState is ProfileUiState.Error) {
                    snackbarHostState.showSnackbar(
                        message = (uiState as ProfileUiState.Error).message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
    
    // Dialog de métricas corporales
    if (showBodyMetricsDialog) {
        BodyMetricsDialog(
            currentMetrics = latestMetrics,
            onDismiss = { showBodyMetricsDialog = false },
            onSave = { weight, height, level, bodyFat, chest, waist, hips, thigh, arm, calf, notes ->
                viewModel.saveBodyMetrics(
                    weight, height, level, bodyFat, chest, waist, hips, thigh, arm, calf, notes
                )
                showBodyMetricsDialog = false
            }
        )
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
