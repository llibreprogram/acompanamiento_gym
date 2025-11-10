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
    val updatedAt: Long = System.currentTimeMillis()
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
