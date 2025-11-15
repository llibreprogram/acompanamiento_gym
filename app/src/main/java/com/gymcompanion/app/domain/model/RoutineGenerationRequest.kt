package com.gymcompanion.app.domain.model

/**
 * Datos para generar rutina personalizada
 */
data class RoutineGenerationRequest(
    val goal: FitnessGoal,
    val level: FitnessLevel,
    val daysPerWeek: Int,  // 3, 4, 5, 6 días
    val sessionDuration: Int, // minutos por sesión
    val equipment: AvailableEquipment,
    val focusAreas: List<String> = emptyList(), // Áreas específicas a trabajar
    val injuries: List<String> = emptyList() // Lesiones o limitaciones
)
