package com.gymcompanion.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class que representa una sesión de entrenamiento con todas sus series
 * Utiliza las capacidades de relación de Room para obtener datos relacionados
 */
data class WorkoutSessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutSessionId"
    )
    val sets: List<ExerciseSetEntity>
)
