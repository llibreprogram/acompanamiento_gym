package com.gymcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un ejercicio individual
 * Incluye información sobre técnica, músculo objetivo, y dificultad
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Información básica
    val name: String,
    val description: String,
    val muscleGroup: String, // "chest", "back", "legs", "shoulders", "arms", "core", "full_body"
    val targetMuscles: String, // Lista separada por comas: "pectoralis_major,deltoid_anterior"
    
    // Clasificación
    val difficulty: String, // "beginner", "intermediate", "advanced"
    val equipmentNeeded: String, // "barbell", "dumbbell", "machine", "bodyweight", "cable", etc.
    val exerciseType: String, // "compound", "isolation"
    
    // Instrucciones técnicas
    val instructionsSteps: String, // JSON array de pasos: ["Paso 1", "Paso 2", ...]
    val commonMistakes: String, // JSON array de errores comunes
    val safetyTips: String, // JSON array de consejos de seguridad
    
    // Medios visuales (rutas a recursos locales o URLs)
    val illustrationPath: String?, // Ruta a ilustración principal
    val illustrationPath2: String? = null, // Segunda ilustración (posición final)
    val videoPath: String? = null, // Opcional: ruta a video demostrativo
    
    // Variaciones por nivel
    val beginnerVariation: String? = null, // Descripción de variación más fácil
    val advancedVariation: String? = null, // Descripción de variación más difícil
    
    // Metadata
    val isCustom: Boolean = false, // true si fue creado por el usuario
    val createdAt: Long = System.currentTimeMillis(),
    
    // Género recomendado (opcional, para personalización futura)
    val suitableForGender: String? = null, // "male", "female", "all"
    // Etiquetas para preferencias (opcional, para IA)
    val tags: String? = null // Lista separada por comas: "hipertrofia,cardio,funcional"
)

/**
 * Enumeración de grupos musculares principales
 */
enum class MuscleGroup(val displayName: String) {
    CHEST("Pecho"),
    BACK("Espalda"),
    LEGS("Piernas"),
    SHOULDERS("Hombros"),
    ARMS("Brazos"),
    CORE("Core/Abdomen"),
    FULL_BODY("Cuerpo Completo")
}

/**
 * Enumeración de niveles de dificultad
 */
enum class Difficulty(val displayName: String) {
    BEGINNER("Principiante"),
    INTERMEDIATE("Intermedio"),
    ADVANCED("Avanzado")
}
