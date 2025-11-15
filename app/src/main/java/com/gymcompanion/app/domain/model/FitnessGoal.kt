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
