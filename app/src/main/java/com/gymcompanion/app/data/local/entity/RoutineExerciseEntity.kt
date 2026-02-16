package com.gymcompanion.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


/**
 * Entidad de relación entre rutina y ejercicio
 * Define qué ejercicios pertenecen a cada rutina y sus parámetros específicos
 */
@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
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
        Index(value = ["routineId"]),
        Index(value = ["exerciseId"])
    ]
)
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val exerciseId: Long,
    
    // Orden en la rutina
    val orderIndex: Int,
    
    // Parámetros planificados
    val plannedSets: Int,
    val plannedReps: String, // Puede ser "12" o "8-12" para rangos
    val plannedWeight: Double? = null, // en lbs, null para peso corporal
    val restTimeSeconds: Int = 60, // Tiempo de descanso entre series
    
    // Notas específicas para este ejercicio en esta rutina
    val notes: String? = null
)
