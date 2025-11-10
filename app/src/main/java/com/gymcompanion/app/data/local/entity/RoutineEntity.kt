package com.gymcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa una rutina de entrenamiento
 * Una rutina es una colección de ejercicios planificados
 */
@Entity(
    tableName = "routines",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    
    val name: String,
    val description: String? = null,
    
    // Programación
    val daysOfWeek: String, // JSON array: [1,3,5] para Lunes, Miércoles, Viernes
    val duration: Int, // Duración estimada en minutos
    
    // Clasificación
    val focusArea: String, // "strength", "hypertrophy", "endurance", "weight_loss", "general_fitness"
    val difficulty: String, // "beginner", "intermediate", "advanced"
    
    // Estado
    val isActive: Boolean = true,
    val isAIGenerated: Boolean = false, // true si fue generada por IA
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
