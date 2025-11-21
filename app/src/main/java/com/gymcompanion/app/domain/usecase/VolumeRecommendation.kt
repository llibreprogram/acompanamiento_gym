package com.gymcompanion.app.domain.usecase

/**
 * Recomendación de volumen de entrenamiento
 */
data class VolumeRecommendation(
    val sets: Int,
    val repsMin: Int,
    val repsMax: Int,
    val rest: Int // segundos
)
