package com.gymcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa una sesión de entrenamiento completada
 */
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["routineId"]),
        Index(value = ["startTime"])
    ]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val routineId: Long? = null, // null si es sesión libre
    
    val startTime: Long,
    val endTime: Long? = null, // null si aún está en progreso
    val durationMinutes: Int? = null, // Calculado al finalizar
    
    // Datos de la sesión
    val totalExercises: Int = 0,
    val completedExercises: Int = 0,
    val totalSets: Int = 0,
    val completedSets: Int = 0,
    
    // Sensación subjetiva de esfuerzo (RPE: Rate of Perceived Exertion)
    val rpe: Int? = null, // Escala 1-10
    
    val notes: String? = null,
    val isCompleted: Boolean = false
)
