package com.gymcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa una serie individual realizada durante un entrenamiento
 * Permite seguimiento detallado del rendimiento ejercicio por ejercicio
 */
@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutSessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutSessionId"]),
        Index(value = ["exerciseId"])
    ]
)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutSessionId: Long,
    val exerciseId: Long,
    
    // Información de la serie
    val setNumber: Int, // 1, 2, 3...
    val repsCompleted: Int,
    val weightUsed: Double? = null, // en kg, null para peso corporal
    
    // Tiempo
    val performedAt: Long = System.currentTimeMillis(),
    val restTimeTaken: Int? = null, // Descanso real tomado en segundos
    
    // Datos adicionales
    val rpe: Int? = null, // Rate of Perceived Exertion (1-10)
    val isDropSet: Boolean = false,
    val isWarmUpSet: Boolean = false,
    val notes: String? = null
)
