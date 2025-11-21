package com.gymcompanion.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Free Exercise DB
 * https://github.com/yuhonas/free-exercise-db
 */
data class FreeExerciseDBExercise(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("force")
    val force: String?, // "pull", "push", "static"
    
    @SerializedName("level")
    val level: String, // "beginner", "intermediate", "expert"
    
    @SerializedName("mechanic")
    val mechanic: String?, // "compound", "isolation"
    
    @SerializedName("equipment")
    val equipment: String?, // "body only", "barbell", "dumbbell", etc.
    
    @SerializedName("primaryMuscles")
    val primaryMuscles: List<String>,
    
    @SerializedName("secondaryMuscles")
    val secondaryMuscles: List<String>?,
    
    @SerializedName("instructions")
    val instructions: List<String>,
    
    @SerializedName("category")
    val category: String, // "strength", "stretching", "plyometrics", etc.
    
    @SerializedName("images")
    val images: List<String>?
)
