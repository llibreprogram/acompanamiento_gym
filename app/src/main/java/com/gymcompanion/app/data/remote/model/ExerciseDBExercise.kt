package com.gymcompanion.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos de ExerciseDB API V2
 * https://exercisedb.dev
 */
data class ExerciseDBExercise(
    @SerializedName("exerciseId")
    val exerciseId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("imageUrl")
    val imageUrl: String?,
    
    @SerializedName("equipments")
    val equipments: List<String>,
    
    @SerializedName("bodyParts")
    val bodyParts: List<String>,
    
    @SerializedName("exerciseType")
    val exerciseType: String, // "weight_reps", "time_based", "distance_based"
    
    @SerializedName("targetMuscles")
    val targetMuscles: List<String>,
    
    @SerializedName("secondaryMuscles")
    val secondaryMuscles: List<String>?,
    
    @SerializedName("videoUrl")
    val videoUrl: String?,
    
    @SerializedName("keywords")
    val keywords: List<String>?,
    
    @SerializedName("overview")
    val overview: String?,
    
    @SerializedName("instructions")
    val instructions: List<String>?,
    
    @SerializedName("exerciseTips")
    val exerciseTips: List<String>?,
    
    @SerializedName("variations")
    val variations: List<String>?,
    
    @SerializedName("relatedExerciseIds")
    val relatedExerciseIds: List<String>?
)

