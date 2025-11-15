package com.gymcompanion.app.domain.model

/**
 * Objetivos de fitness del usuario
 */
enum class FitnessGoal {
    WEIGHT_LOSS,        // Pérdida de peso
    MUSCLE_GAIN,        // Ganancia muscular
    STRENGTH,           // Aumento de fuerza
    ENDURANCE,          // Resistencia
    GENERAL_FITNESS,    // Fitness general
    BODY_RECOMPOSITION  // Recomposición corporal
}

enum class FitnessLevel {
    BEGINNER,      // Principiante (0-6 meses)
    INTERMEDIATE,  // Intermedio (6-24 meses)
    ADVANCED       // Avanzado (2+ años)
}

enum class AvailableEquipment {
    FULL_GYM,          // Gimnasio completo
    HOME_BASIC,        // Casa con pesas y barra
    BODYWEIGHT_ONLY,   // Solo peso corporal
    MINIMAL            // Equipamiento mínimo
}

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
