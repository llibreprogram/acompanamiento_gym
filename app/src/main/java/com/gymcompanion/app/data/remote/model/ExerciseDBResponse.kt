package com.gymcompanion.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta completa de la API ExerciseDB
 */
data class ExerciseDBResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("meta")
    val meta: MetaData,
    @SerializedName("data")
    val data: List<ExerciseDBExercise>
)

/**
 * Metadatos de paginación
 */
data class MetaData(
    @SerializedName("total")
    val total: Int,
    @SerializedName("hasNextPage")
    val hasNextPage: Boolean,
    @SerializedName("hasPreviousPage")
    val hasPreviousPage: Boolean,
    @SerializedName("nextCursor")
    val nextCursor: String?
)
