package com.gymcompanion.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Data class que representa una rutina con sus ejercicios asociados
 * Utiliza las capacidades de relación de Room para obtener datos relacionados
 */
data class RoutineWithExercises(
    @Embedded val routine: RoutineEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId",
        entity = RoutineExerciseEntity::class
    )
    val routineExercises: List<RoutineExerciseWithExercise>
)

/**
 * Data class auxiliar que representa un ejercicio de rutina con el ejercicio completo
 */
data class RoutineExerciseWithExercise(
    @Embedded val routineExercise: RoutineExerciseEntity,
    
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)
