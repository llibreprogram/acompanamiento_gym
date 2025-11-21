package com.gymcompanion.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.remote.model.FreeExerciseDBExercise
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para cargar ejercicios desde Free Exercise DB (assets locales)
 */
@Singleton
class FreeExerciseDBRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    companion object {
        // Base URL para las imágenes del repositorio Free Exercise DB en GitHub
        private const val GITHUB_IMAGES_BASE_URL = 
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
    }
    
    /**
     * Carga todos los ejercicios desde el archivo JSON en assets
     */
    suspend fun loadExercisesFromAssets(): List<ExerciseEntity> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("free_exercise_db.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<FreeExerciseDBExercise>>() {}.type
            val exercises: List<FreeExerciseDBExercise> = gson.fromJson(jsonString, type)
            
            // Convertir a ExerciseEntity
            exercises.map { it.toExerciseEntity() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Mapea FreeExerciseDBExercise a ExerciseEntity
     */
    private fun FreeExerciseDBExercise.toExerciseEntity(): ExerciseEntity {
        // Mapear grupo muscular principal
        val muscleGroup = mapMuscleGroupFromPrimary(primaryMuscles.firstOrNull() ?: "")
        
        // Mapear dificultad
        val difficulty = when (level.lowercase()) {
            "beginner" -> "beginner"
            "intermediate" -> "intermediate"
            "expert" -> "advanced"
            else -> "intermediate"
        }
        
        // Mapear tipo de ejercicio
        val exerciseType = when (mechanic?.lowercase()) {
            "compound" -> "compound"
            "isolation" -> "isolation"
            else -> if (category == "strength") "compound" else "other"
        }
        
        // Mapear equipo
        val equipmentNeeded = when (equipment?.lowercase()) {
            "body only" -> "bodyweight"
            "barbell" -> "barbell"
            "dumbbell" -> "dumbbell"
            "cable" -> "cable"
            "machine" -> "machine"
            else -> equipment ?: "bodyweight"
        }
        
        // Construir músculos objetivo
        val targetMuscles = (primaryMuscles + (secondaryMuscles ?: emptyList())).joinToString(",")
        
        // Construir instrucciones como JSON array
        val instructionsJson = gson.toJson(instructions)
        
        return ExerciseEntity(
            name = name,
            description = "Ejercicio de ${category}: ${primaryMuscles.joinToString(", ")}",
            muscleGroup = muscleGroup,
            targetMuscles = targetMuscles,
            difficulty = difficulty,
            equipmentNeeded = equipmentNeeded,
            exerciseType = exerciseType,
            instructionsSteps = instructionsJson,
            commonMistakes = "[]", // No disponible en Free Exercise DB
            safetyTips = "[]", // No disponible
            illustrationPath = images?.getOrNull(0)?.let { "$GITHUB_IMAGES_BASE_URL$it" } ?: "",
            illustrationPath2 = images?.getOrNull(1)?.let { "$GITHUB_IMAGES_BASE_URL$it" },
            beginnerVariation = null,
            advancedVariation = null
        )
    }
    
    /**
     * Mapea el músculo primario al grupo muscular de nuestra app (en español)
     */
    private fun mapMuscleGroupFromPrimary(muscle: String): String {
        return when (muscle.lowercase()) {
            "chest", "pectorals" -> "Pecho"
            "back", "lats", "middle back", "lower back", "traps" -> "Espalda"
            "shoulders", "deltoids" -> "Hombros"
            "biceps" -> "Bíceps"
            "triceps" -> "Tríceps"
            "forearms" -> "Brazos"
            "quadriceps", "hamstrings", "glutes", "calves", "adductors", "abductors" -> "Piernas"
            "abdominals", "obliques", "abs" -> "Core"
            else -> "Otro"
        }
    }
}
