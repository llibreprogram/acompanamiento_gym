package com.gymcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad que representa al usuario de la aplicación
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String? = null,
    val dateOfBirth: Long, // Timestamp para calcular edad dinámica
    val gender: String, // "male", "female", "other"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val weight: Float? = null, // Peso en kg
    val height: Float? = null, // Altura en cm
    val experienceLevel: String? = null, // "beginner", "intermediate", "advanced"
    val goal: String? = null, // "muscle_gain", "fat_loss", "maintenance", etc.
    val restrictions: String? = null, // Lesiones, condiciones médicas, etc.
    val preferences: String? = null, // Preferencias de entrenamiento, tipo de rutina, etc.
    val activityLevel: String? = null // "sedentary", "light", "moderate", "active", "very_active"
) {
    /**
     * Calcula la edad actual del usuario basándose en la fecha de nacimiento
     */
    fun calculateAge(): Int {
        val birthYear = Date(dateOfBirth).year + 1900
        val currentYear = Date().year + 1900
        return currentYear - birthYear
    }
}
