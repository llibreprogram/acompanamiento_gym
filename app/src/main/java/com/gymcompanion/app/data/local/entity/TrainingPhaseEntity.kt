package com.gymcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_phases")
data class TrainingPhaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val phaseName: String, // "Hypertrophy", "Strength", "Power", "Deload"
    val startDate: Long = System.currentTimeMillis(),
    val durationWeeks: Int = 4,
    val isActive: Boolean = true,
    val currentWeek: Int = 1, // Semana actual dentro de la fase
    val totalWeeksBeforeDeload: Int = 4 // Semanas antes del deload
)
