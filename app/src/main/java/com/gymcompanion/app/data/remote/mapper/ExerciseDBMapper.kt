package com.gymcompanion.app.data.remote.mapper

import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.remote.model.ExerciseDBExercise
import com.google.gson.Gson

/**
 * Mapper para convertir ejercicios de ExerciseDB API a entidades locales
 */
object ExerciseDBMapper {
    
    private val gson = Gson()
    
    /**
     * Convierte un ejercicio de ExerciseDB a nuestra entidad local
     */
    fun toExerciseEntity(exerciseDB: ExerciseDBExercise): ExerciseEntity {
        return ExerciseEntity(
            id = 0, // Auto-generado por Room
            name = exerciseDB.name,
            description = exerciseDB.overview ?: "Sin descripción disponible",
            muscleGroup = mapBodyPartsToMuscleGroup(exerciseDB.bodyParts),
            targetMuscles = exerciseDB.targetMuscles.joinToString(","),
            difficulty = inferDifficulty(exerciseDB),
            equipmentNeeded = mapEquipment(exerciseDB.equipments),
            exerciseType = mapExerciseType(exerciseDB.equipments, exerciseDB.bodyParts),
            instructionsSteps = gson.toJson(exerciseDB.instructions ?: emptyList()),
            commonMistakes = gson.toJson(exerciseDB.exerciseTips?.take(3) ?: emptyList()),
            safetyTips = gson.toJson(exerciseDB.exerciseTips?.drop(3) ?: emptyList()),
            illustrationPath = exerciseDB.imageUrl,
            illustrationPath2 = null,
            videoPath = exerciseDB.videoUrl,
            beginnerVariation = exerciseDB.variations?.firstOrNull(),
            advancedVariation = exerciseDB.variations?.lastOrNull(),
            isCustom = false,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Convierte múltiples ejercicios
     */
    fun toExerciseEntities(exercisesDB: List<ExerciseDBExercise>): List<ExerciseEntity> {
        return exercisesDB.map { toExerciseEntity(it) }
    }
    
    /**
     * Mapea partes del cuerpo de ExerciseDB a nuestros grupos musculares
     */
    private fun mapBodyPartsToMuscleGroup(bodyParts: List<String>): String {
        // ExerciseDB usa nombres en inglés como "Chest", "Back", "Legs", etc.
        val firstPart = bodyParts.firstOrNull()?.lowercase() ?: "full_body"
        
        return when {
            firstPart.contains("chest") || firstPart.contains("pec") -> "Pecho"
            firstPart.contains("back") || firstPart.contains("lat") -> "Espalda"
            firstPart.contains("leg") || firstPart.contains("quad") || 
                firstPart.contains("hamstring") || firstPart.contains("calf") -> "Piernas"
            firstPart.contains("shoulder") || firstPart.contains("delt") -> "Hombros"
            firstPart.contains("arm") || firstPart.contains("bicep") || 
                firstPart.contains("tricep") || firstPart.contains("forearm") -> "Brazos"
            firstPart.contains("core") || firstPart.contains("ab") || 
                firstPart.contains("oblique") -> "Core"
            else -> "Cuerpo Completo"
        }
    }
    
    /**
     * Mapea equipamiento de ExerciseDB a nuestro formato
     */
    private fun mapEquipment(equipments: List<String>): String {
        if (equipments.isEmpty()) return "Peso Corporal"
        
        val firstEquipment = equipments.first().lowercase()
        
        return when {
            firstEquipment.contains("barbell") -> "Barra"
            firstEquipment.contains("dumbbell") -> "Mancuernas"
            firstEquipment.contains("machine") || firstEquipment.contains("cable") -> "Máquina"
            firstEquipment.contains("kettlebell") -> "Kettlebell"
            firstEquipment.contains("band") -> "Banda elástica"
            firstEquipment.contains("bodyweight") || firstEquipment == "none" -> "Peso Corporal"
            else -> equipments.first()
        }
    }
    
    /**
     * Determina si el ejercicio es compuesto o de aislamiento
     */
    private fun mapExerciseType(equipments: List<String>, bodyParts: List<String>): String {
        // Ejercicios que involucran múltiples articulaciones son compuestos
        val isCompound = bodyParts.size > 1 || 
            equipments.any { it.lowercase().contains("barbell") } ||
            equipments.any { it.lowercase().contains("machine") }
        
        return if (isCompound) "Compuesto" else "Aislamiento"
    }
    
    /**
     * Infiere la dificultad basándose en el equipamiento y complejidad
     */
    private fun inferDifficulty(exerciseDB: ExerciseDBExercise): String {
        val equipment = exerciseDB.equipments.firstOrNull()?.lowercase() ?: ""
        val name = exerciseDB.name.lowercase()
        
        return when {
            // Ejercicios avanzados
            name.contains("olympic") || name.contains("snatch") || 
                name.contains("clean") || name.contains("pistol") -> "Avanzado"
            
            // Ejercicios de nivel intermedio
            equipment.contains("barbell") || equipment.contains("cable") -> "Intermedio"
            
            // Ejercicios principiantes
            equipment.contains("machine") || equipment.contains("bodyweight") -> "Principiante"
            
            // Default a intermedio
            else -> "Intermedio"
        }
    }
}
