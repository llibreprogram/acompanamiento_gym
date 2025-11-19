@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("DEPRECATION")

package com.gymcompanion.app.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gymcompanion.app.presentation.components.GoalsSection
import com.gymcompanion.app.presentation.components.ProfileHeader

/**
 * Pantalla de perfil del usuario
 * Muestra y permite editar datos corporales y preferencias
 */
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val latestMetrics by viewModel.latestMetrics.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    
    // Estados locales para unidades (se actualizan con LaunchedEffect)
    var weightUnit by remember { mutableStateOf("kg") }
    var heightUnit by remember { mutableStateOf("cm") }
    var distanceUnit by remember { mutableStateOf("km") }
    
    // Actualizar estados locales cuando cambian las preferencias
    LaunchedEffect(userPreferences) {
        userPreferences?.let { prefs ->
            android.util.Log.d("ProfileScreen", "Preferencias actualizadas: weight=${prefs.weightUnit}, height=${prefs.heightUnit}, distance=${prefs.distanceUnit}")
            weightUnit = prefs.weightUnit
            heightUnit = prefs.heightUnit
            distanceUnit = prefs.distanceUnit
        } ?: android.util.Log.d("ProfileScreen", "userPreferences es null")
    }
    
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile header with avatar and stats
            ProfileHeader(
                userName = currentUser?.name,
                userAge = currentUser?.calculateAge(),
                userGender = currentUser?.gender,
                workoutCount = 24, // TODO: Get from viewModel
                totalVolume = 12500L, // TODO: Get from viewModel
                modifier = Modifier.fillMaxWidth()
            )

            // Goals section
            GoalsSection(
                currentGoal = currentUser?.goal,
                targetWeight = null, // TODO: Add target weight to user profile
                currentWeight = latestMetrics?.weight?.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

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
                    
                    // Datos personales
                    MetricRow("Género", when(currentUser?.gender) {
                        "male" -> "♂️ Hombre"
                        "female" -> "♀️ Mujer"
                        "other" -> "Otro"
                        else -> "No especificado"
                    })
                    MetricRow("Edad", "${currentUser?.calculateAge() ?: "--"} años")
                    
                    // Métricas corporales
                    latestMetrics?.let { metrics ->
                        // Convertir peso
                        val displayWeight = if (weightUnit == "lb") {
                            String.format("%.1f lb", metrics.weight * 2.20462)
                        } else {
                            String.format("%.1f kg", metrics.weight)
                        }
                        
                        // Convertir altura
                        val displayHeight = if (heightUnit == "ft") {
                            val totalInches = metrics.height / 2.54
                            val feet = (totalInches / 12).toInt()
                            val inches = (totalInches % 12).toInt()
                            "$feet'$inches\""
                        } else {
                            String.format("%.0f cm", metrics.height)
                        }
                        
                        MetricRow("Peso", displayWeight)
                        MetricRow("Altura", displayHeight)
                        MetricRow("IMC", String.format("%.1f", metrics.bmi))
                        MetricRow("% Grasa", metrics.bodyFatPercentage?.let { "${it}%" } ?: "--")
                        MetricRow("Nivel", when(metrics.experienceLevel) {
                            "beginner" -> "Principiante"
                            "intermediate" -> "Intermedio"
                            "advanced" -> "Avanzado"
                            else -> "No especificado"
                        })
                    } ?: run {
                        MetricRow("Peso", "-- $weightUnit")
                        MetricRow("Altura", "-- $heightUnit")
                        MetricRow("IMC", "--")
                        MetricRow("% Grasa", "-- %")
                        MetricRow("Nivel", "No especificado")
                    }
                }
            }
            
            // Health Metrics Card (TMB, Calorías)
            if (currentUser != null && latestMetrics != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "🔥 Métricas de Salud Personalizadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val user = currentUser
                        val metrics = latestMetrics
                        
                        if (user != null && metrics != null) {
                            val age = user.calculateAge()
                            val bmr = com.gymcompanion.app.domain.util.HealthCalculator.calculateBMR(
                                weight = metrics.weight,
                                height = metrics.height,
                                age = age,
                                gender = user.gender
                            )
                            
                            val tdee = com.gymcompanion.app.domain.util.HealthCalculator.calculateTDEE(
                                bmr = bmr,
                                activityLevel = com.gymcompanion.app.domain.util.HealthCalculator.ActivityLevel.MODERATELY_ACTIVE
                            )
                            
                            val targetCalories = com.gymcompanion.app.domain.util.HealthCalculator.calculateTargetCalories(
                                tdee = tdee,
                                goal = com.gymcompanion.app.domain.util.HealthCalculator.FitnessGoal.MAINTENANCE
                            )
                            
                            val maxHeartRate = com.gymcompanion.app.domain.util.HealthCalculator.calculateMaxHeartRate(age)
                            
                            MetricRow("TMB (Basal)", "${bmr.toInt()} kcal/día")
                            MetricRow("TDEE (Activo)", "${tdee.toInt()} kcal/día")
                            MetricRow("Calorías Objetivo", "${targetCalories.toInt()} kcal/día")
                            MetricRow("FC Máxima", "$maxHeartRate bpm")
                            
                            Text(
                                text = "💡 Basado en actividad moderada. Ajusta según tu nivel de ejercicio.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                text = "Completa tu perfil y datos corporales para ver tus métricas de salud personalizadas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Unit Preferences Card - Siempre visible
            if (currentUser != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "⚙️ Unidades de Medida",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Weight unit switcher
                        PreferenceSwitchRow(
                            label = "Peso",
                            option1 = "kg",
                            option2 = "lb",
                            selectedOption = weightUnit,
                            onOptionSelected = { 
                                viewModel.updateWeightUnit(it)
                            }
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Height unit switcher
                        PreferenceSwitchRow(
                            label = "Altura",
                            option1 = "cm",
                            option2 = "ft",
                            selectedOption = heightUnit,
                            onOptionSelected = { 
                                viewModel.updateHeightUnit(it)
                            }
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Distance unit switcher
                        PreferenceSwitchRow(
                            label = "Distancia",
                            option1 = "km",
                            option2 = "mi",
                            selectedOption = distanceUnit,
                            onOptionSelected = { 
                                viewModel.updateDistanceUnit(it)
                            }
                        )
                        
                        // Debug info
                        if (userPreferences == null) {
                            Text(
                                text = "Inicializando preferencias...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Text(
                                text = "✅ Unidades: ${weightUnit}, ${heightUnit}, ${distanceUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Exercise Database Sync Card
            ExerciseSyncCard()
            
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
        val user = currentUser
        val metrics = latestMetrics
        
        BodyMetricsDialog(
            currentMetrics = metrics,
            currentUser = user,
            onDismiss = { showBodyMetricsDialog = false },
            onSaveComplete = { name, gender, dateOfBirth, weight, height, level, bodyFat, chest, waist, hips, thigh, arm, calf, notes ->
                viewModel.saveCompleteProfile(
                    name, gender, dateOfBirth,
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

@Composable
fun PreferenceSwitchRow(
    label: String,
    option1: String,
    option2: String,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedOption == option1,
                onClick = { onOptionSelected(option1) },
                label = { Text(option1) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = selectedOption == option2,
                onClick = { onOptionSelected(option2) },
                label = { Text(option2) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
fun ExerciseSyncCard(
    viewModel: com.gymcompanion.app.presentation.viewmodel.ExerciseSyncViewModel = hiltViewModel()
) {
    val syncState by viewModel.syncState.collectAsState()
    val uiState = com.gymcompanion.app.presentation.viewmodel.ExerciseSyncUiState.fromSyncState(syncState)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💪 Base de Datos de Ejercicios",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sincroniza con ExerciseDB (5,000+ ejercicios)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Estado de sincronización
            when {
                uiState.isLoading -> {
                    Column {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        if (uiState.progress > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ejercicios sincronizados: ${uiState.progress}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                uiState.isSuccess -> {
                    Text(
                        text = "✅ ${uiState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                uiState.isError -> {
                    Text(
                        text = "❌ ${uiState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Text(
                        text = "Sincronización automática cada 7 días",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isLoading) {
                    Button(
                        onClick = { viewModel.cancelSync() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancelar")
                    }
                } else {
                    Button(
                        onClick = { viewModel.syncExercises() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sincronizar")
                    }
                    
                    if (uiState.isSuccess || uiState.isError) {
                        OutlinedButton(
                            onClick = { viewModel.resetState() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
