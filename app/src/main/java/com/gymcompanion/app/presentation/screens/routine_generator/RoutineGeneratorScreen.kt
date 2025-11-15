@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("DEPRECATION")

package com.gymcompanion.app.presentation.screens.routine_generator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymcompanion.app.domain.model.*
import com.gymcompanion.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineGeneratorScreen(
    viewModel: RoutineGeneratorViewModel = hiltViewModel(),
    onRoutineGenerated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    
    // Navigate back when routine is generated successfully
    LaunchedEffect(uiState.isGenerated) {
        if (uiState.isGenerated) {
            snackbarHostState.showSnackbar(
                message = "✅ ¡Rutina generada exitosamente! (${uiState.generatedCount} días)",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(1000) // Dar tiempo para ver el mensaje
            onRoutineGenerated()
            viewModel.resetWizard() // Reset para próxima vez
        }
    }
    
    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Generador Inteligente de Rutinas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GymPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GymBackground)
        ) {
            if (!uiState.isGenerated) {
                // Progress indicator
                LinearProgressIndicator(
                    progress = (currentStep + 1) / 7f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = GymPrimary,
                    trackColor = GymSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Step content - with weight to fill space but not overflow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "step_animation",
                        modifier = Modifier.fillMaxSize()
                    ) { step ->
                    when (step) {
                        0 -> GoalSelectionStep(
                            selectedGoal = uiState.selectedGoal,
                            onGoalSelected = { viewModel.updateGoal(it) }
                        )
                        1 -> LevelSelectionStep(
                            selectedLevel = uiState.selectedLevel,
                            onLevelSelected = { viewModel.updateLevel(it) }
                        )
                        2 -> FrequencySelectionStep(
                            daysPerWeek = uiState.daysPerWeek,
                            sessionDuration = uiState.sessionDuration,
                            consecutiveDays = uiState.consecutiveDays,
                            onDaysChanged = { viewModel.updateDaysPerWeek(it) },
                            onDurationChanged = { viewModel.updateSessionDuration(it) },
                            onConsecutiveDaysChanged = { viewModel.updateConsecutiveDays(it) }
                        )
                        3 -> EquipmentSelectionStep(
                            selectedEquipment = uiState.selectedEquipment,
                            onEquipmentSelected = { viewModel.updateEquipment(it) }
                        )
                        4 -> PhysicalLimitationsStep(
                            selectedLimitations = uiState.physicalLimitations,
                            onLimitationToggle = { viewModel.togglePhysicalLimitation(it) }
                        )
                        5 -> RoutinePreviewStep(
                            uiState = uiState
                        )
                        6 -> SummaryStep(
                            uiState = uiState,
                            onGenerate = { viewModel.generateRoutine() }
                        )
                    }
                    }
                }
                
                // Navigation buttons (sin Spacer weight - siempre visible)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atrás", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Button(
                        onClick = {
                            if (currentStep < 6) {
                                currentStep++
                            } else {
                                // Último paso: generar rutina
                                viewModel.generateRoutine()
                            }
                        },
                        modifier = Modifier
                            .then(if (currentStep == 0) Modifier.fillMaxWidth() else Modifier.weight(1f))
                            .height(56.dp),
                        enabled = isStepValid(currentStep, uiState),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GymPrimary,
                            disabledContainerColor = GymSurfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            if (currentStep < 6) "Siguiente" else "Generar Rutina",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(if (currentStep < 6) Icons.Default.ArrowForward else Icons.Default.CheckCircle, contentDescription = null)
                    }
                }
            } else {
                // Success screen
                SuccessScreen(
                    generatedCount = uiState.generatedCount,
                    onViewRoutines = onRoutineGenerated
                )
            }
            
            // Error message
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = GymError
                ) {
                    Text(error, color = Color.White)
                }
            }
        }
        
        if (uiState.isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = GymPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Generando tu rutina perfecta...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Esto puede tomar unos segundos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GymTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalSelectionStep(
    selectedGoal: FitnessGoal?,
    onGoalSelected: (FitnessGoal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "¿Cuál es tu objetivo principal?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Selecciona tu meta principal para optimizar tu rutina",
            style = MaterialTheme.typography.bodyMedium,
            color = GymTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Mensaje de ayuda
        if (selectedGoal == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "👉 Toca una opción para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "✅ Presiona 'Siguiente' para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        
        val goals = listOf(
            Triple(FitnessGoal.WEIGHT_LOSS, Icons.Default.TrendingDown, "Pérdida de Peso") to "Quema calorías y reduce grasa corporal",
            Triple(FitnessGoal.MUSCLE_GAIN, Icons.Default.FitnessCenter, "Ganancia Muscular") to "Aumenta masa muscular e hipertrofia",
            Triple(FitnessGoal.STRENGTH, Icons.Default.Power, "Aumento de Fuerza") to "Mejora tu fuerza máxima",
            Triple(FitnessGoal.ENDURANCE, Icons.Default.DirectionsRun, "Resistencia") to "Mejora tu capacidad cardiovascular",
            Triple(FitnessGoal.GENERAL_FITNESS, Icons.Default.Favorite, "Fitness General") to "Balance entre todos los aspectos",
            Triple(FitnessGoal.BODY_RECOMPOSITION, Icons.Default.Autorenew, "Recomposición") to "Gana músculo y pierde grasa simultáneamente"
        )
        
        goals.forEach { (goal, description) ->
            GoalCard(
                goal = goal.first,
                icon = goal.second,
                title = goal.third,
                description = description,
                isSelected = selectedGoal == goal.first,
                onClick = { onGoalSelected(goal.first) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun GoalCard(
    goal: FitnessGoal,
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) GymPrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GymPrimaryLight.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) GymPrimary else GymSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else GymTextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GymPrimary else GymTextPrimary
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GymTextSecondary
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GymPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LevelSelectionStep(
    selectedLevel: FitnessLevel?,
    onLevelSelected: (FitnessLevel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "¿Cuál es tu nivel actual?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Esto nos ayuda a ajustar el volumen e intensidad",
            style = MaterialTheme.typography.bodyMedium,
            color = GymTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        val levels = listOf(
            Triple(FitnessLevel.BEGINNER, "Principiante", "0-6 meses de experiencia") to "Nuevo en el entrenamiento o retomando después de mucho tiempo",
            Triple(FitnessLevel.INTERMEDIATE, "Intermedio", "6 meses - 2 años") to "Entrenamiento regular con técnica establecida",
            Triple(FitnessLevel.ADVANCED, "Avanzado", "2+ años") to "Experiencia extensa con rutinas estructuradas"
        )
        
        levels.forEach { (level, description) ->
            LevelCard(
                level = level.first,
                title = level.second,
                subtitle = level.third,
                description = description,
                isSelected = selectedLevel == level.first,
                onClick = { onLevelSelected(level.first) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun LevelCard(
    level: FitnessLevel,
    title: String,
    subtitle: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) GymPrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GymPrimaryLight.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) GymPrimary else GymTextPrimary
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GymTextSecondary
                    )
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GymPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = GymTextSecondary
            )
        }
    }
}

@Composable
fun FrequencySelectionStep(
    daysPerWeek: Int,
    sessionDuration: Int,
    consecutiveDays: Boolean,
    onDaysChanged: (Int) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onConsecutiveDaysChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "¿Con qué frecuencia entrenarás?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Definimos tu calendario de entrenamiento",
            style = MaterialTheme.typography.bodyMedium,
            color = GymTextSecondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Days per week
        Text(
            "Días por semana: $daysPerWeek",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(3, 4, 5, 6).forEach { days ->
                FilterChip(
                    selected = daysPerWeek == days,
                    onClick = { onDaysChanged(days) },
                    label = { 
                        Text(
                            "$days días",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GymPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.height(56.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Session duration
        Text(
            "Duración por sesión: $sessionDuration min",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(45, 60, 75, 90).forEach { duration ->
                FilterChip(
                    selected = sessionDuration == duration,
                    onClick = { onDurationChanged(duration) },
                    label = { 
                        Text(
                            "$duration min",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GymPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.height(56.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Consecutive days
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (consecutiveDays) GymPrimaryLight.copy(alpha = 0.1f) else Color.White
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Entrenar días consecutivos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (consecutiveDays) "Lun-Mar-Mie-Jue" else "Lun-Mie-Vie",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GymTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Switch(
                    checked = consecutiveDays,
                    onCheckedChange = onConsecutiveDaysChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GymPrimary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = GymInfo.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = GymInfo,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Recomendamos 4-5 días por semana para resultados óptimos. Las sesiones de 60-75 minutos son ideales para completar el volumen necesario. ${if (consecutiveDays) "Entrenar días consecutivos requiere mejor recuperación." else "Días alternos permiten mejor recuperación muscular."}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GymTextPrimary
                )
            }
        }
    }
}

@Composable
fun EquipmentSelectionStep(
    selectedEquipment: AvailableEquipment?,
    onEquipmentSelected: (AvailableEquipment) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "¿Qué equipo tienes disponible?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Adaptaremos los ejercicios a tu equipamiento",
            style = MaterialTheme.typography.bodyMedium,
            color = GymTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        val equipment = listOf(
            Triple(AvailableEquipment.FULL_GYM, Icons.Default.FitnessCenter, "Gimnasio Completo") to "Acceso a máquinas, barras, mancuernas, etc.",
            Triple(AvailableEquipment.HOME_BASIC, Icons.Default.Home, "Casa con Equipo") to "Barra, mancuernas, banco",
            Triple(AvailableEquipment.MINIMAL, Icons.Default.Lightbulb, "Equipo Mínimo") to "Solo mancuernas o bandas",
            Triple(AvailableEquipment.BODYWEIGHT_ONLY, Icons.Default.Accessibility, "Solo Peso Corporal") to "Sin equipamiento adicional"
        )
        
        equipment.forEach { (eq, description) ->
            EquipmentCard(
                equipment = eq.first,
                icon = eq.second,
                title = eq.third,
                description = description,
                isSelected = selectedEquipment == eq.first,
                onClick = { onEquipmentSelected(eq.first) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun EquipmentCard(
    equipment: AvailableEquipment,
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) GymPrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GymPrimaryLight.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) GymPrimary else GymSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else GymTextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GymPrimary else GymTextPrimary
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GymTextSecondary
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GymPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryStep(
    uiState: RoutineGeneratorUiState,
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Resumen de tu rutina",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Verifica que todo esté correcto",
            style = MaterialTheme.typography.bodyMedium,
            color = GymTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SummaryRow("Objetivo", uiState.selectedGoal?.name ?: "-")
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                SummaryRow("Nivel", uiState.selectedLevel?.name ?: "-")
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                SummaryRow("Frecuencia", "${uiState.daysPerWeek} días/semana")
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                SummaryRow("Duración", "${uiState.sessionDuration} minutos/sesión")
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                SummaryRow("Equipo", uiState.selectedEquipment?.name ?: "-")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GymPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Generar Mi Rutina Perfecta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = GymTextSecondary
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = GymTextPrimary
        )
    }
}

@Composable
fun SuccessScreen(
    generatedCount: Int,
    onViewRoutines: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GymSuccess,
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "¡Rutina Generada!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            "Se han creado $generatedCount rutinas personalizadas para ti",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = GymTextSecondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onViewRoutines,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GymPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Ver Mis Rutinas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun PhysicalLimitationsStep(
    selectedLimitations: List<PhysicalLimitation>,
    onLimitationToggle: (PhysicalLimitation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "¿Tienes alguna limitación física?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Adaptaremos los ejercicios a tus necesidades (opcional)",
            style = MaterialTheme.typography.bodyMedium,
            color = GymTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        PhysicalLimitation.values().forEach { limitation ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onLimitationToggle(limitation) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedLimitations.contains(limitation)) 
                        GymPrimaryLight.copy(alpha = 0.1f) 
                    else 
                        Color.White
                ),
                border = if (selectedLimitations.contains(limitation))
                    androidx.compose.foundation.BorderStroke(2.dp, GymPrimary)
                else
                    null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            limitation.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Evitará: ${limitation.affectedExercises.take(2).joinToString(", ")}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = GymTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Checkbox(
                        checked = selectedLimitations.contains(limitation),
                        onCheckedChange = { onLimitationToggle(limitation) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GymPrimary
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = GymInfo.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = GymInfo,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Si no tienes ninguna limitación, simplemente presiona 'Siguiente'. Puedes seleccionar múltiples opciones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GymTextPrimary
                )
            }
        }
    }
}

@Composable
fun RoutinePreviewStep(
    uiState: RoutineGeneratorUiState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Vista previa de tu rutina",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Revisa los detalles antes de generar",
            style = MaterialTheme.typography.bodyMedium,
            color = GymTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Goal
        PreviewCard(
            icon = Icons.Default.Flag,
            title = "Objetivo",
            content = when(uiState.selectedGoal) {
                FitnessGoal.WEIGHT_LOSS -> "Pérdida de Peso"
                FitnessGoal.MUSCLE_GAIN -> "Ganancia Muscular"
                FitnessGoal.STRENGTH -> "Aumento de Fuerza"
                FitnessGoal.ENDURANCE -> "Resistencia"
                FitnessGoal.GENERAL_FITNESS -> "Fitness General"
                FitnessGoal.BODY_RECOMPOSITION -> "Recomposición"
                else -> "No especificado"
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Level
        PreviewCard(
            icon = Icons.Default.TrendingUp,
            title = "Nivel",
            content = when(uiState.selectedLevel) {
                FitnessLevel.BEGINNER -> "Principiante"
                FitnessLevel.INTERMEDIATE -> "Intermedio"
                FitnessLevel.ADVANCED -> "Avanzado"
                else -> "No especificado"
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Frequency
        PreviewCard(
            icon = Icons.Default.CalendarToday,
            title = "Frecuencia",
            content = "${uiState.daysPerWeek} días/semana - ${uiState.sessionDuration} min/sesión\n${if (uiState.consecutiveDays) "Días consecutivos" else "Días alternos"}"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Equipment
        PreviewCard(
            icon = Icons.Default.FitnessCenter,
            title = "Equipamiento",
            content = when(uiState.selectedEquipment) {
                AvailableEquipment.GYM_FULL -> "Gimnasio completo"
                AvailableEquipment.HOME_BASIC -> "Casa (básico)"
                AvailableEquipment.BODYWEIGHT_ONLY -> "Solo peso corporal"
                AvailableEquipment.HOME_DUMBBELLS -> "Casa con mancuernas"
                else -> "No especificado"
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Limitations
        if (uiState.physicalLimitations.isNotEmpty()) {
            PreviewCard(
                icon = Icons.Default.HealthAndSafety,
                title = "Limitaciones",
                content = uiState.physicalLimitations.joinToString("\n") { "• ${it.displayName}" }
            )
        }
    }
}

@Composable
fun PreviewCard(
    icon: ImageVector,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GymPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GymTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GymTextPrimary
                )
            }
        }
    }
}

fun isStepValid(step: Int, state: RoutineGeneratorUiState): Boolean {
    return when (step) {
        0 -> state.selectedGoal != null
        1 -> state.selectedLevel != null
        2 -> true // Always valid, has defaults
        3 -> state.selectedEquipment != null
        4 -> true // Limitations always valid (optional)
        5 -> true // Preview always valid
        6 -> true // Summary always valid
        else -> false
    }
}
