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
    val consecutiveDays: Boolean = false, // ¿Entrenar días consecutivos?
    val physicalLimitations: List<PhysicalLimitation> = emptyList(), // Limitaciones físicas
    val focusAreas: List<String> = emptyList(), // Áreas específicas a trabajar
    val injuries: List<String> = emptyList(), // Lesiones o limitaciones (deprecated, usar physicalLimitations)
    val age: Int? = null, // Edad calculada
    val gender: String? = null, // "male", "female", "other"
    val weight: Float? = null, // Peso en kg
    val height: Float? = null, // Altura en cm
    val experienceLevel: String? = null, // "beginner", "intermediate", "advanced"
    val preferences: String? = null, // Preferencias de entrenamiento, tipo de rutina
    val restrictions: String? = null // Restricciones médicas, lesiones, etc.
)

/**
 * Limitaciones físicas del usuario
 */
enum class PhysicalLimitation(val displayName: String, val affectedExercises: List<String>) {
    KNEE_PROBLEMS("Problemas de rodilla", listOf("sentadilla", "estocada", "squat", "lunge", "leg press")),
    SHOULDER_PROBLEMS("Problemas de hombro", listOf("press militar", "shoulder press", "elevaciones laterales", "lateral raise")),
    LOWER_BACK_PROBLEMS("Problemas de espalda baja", listOf("peso muerto", "deadlift", "remo", "buenos días", "good morning")),
    UPPER_BACK_PROBLEMS("Problemas de espalda alta", listOf("remo", "pullover", "dominadas", "pull up")),
    WRIST_PROBLEMS("Problemas de muñeca", listOf("flexiones", "push up", "press de banca", "bench press")),
    ELBOW_PROBLEMS("Problemas de codo", listOf("curl", "tríceps", "tricep", "extensiones")),
    ANKLE_PROBLEMS("Problemas de tobillo", listOf("sentadilla", "estocada", "squat", "pantorrilla", "calf raise")),
    NECK_PROBLEMS("Problemas de cuello", listOf("press militar", "encogimientos", "shrug", "shoulder press")),
    HIP_PROBLEMS("Problemas de cadera", listOf("sentadilla", "peso muerto", "squat", "deadlift", "estocada"))
}
