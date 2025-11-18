package com.gymcompanion.app

import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.remote.model.ExerciseDBExercise
import com.gymcompanion.app.data.remote.model.ExerciseDBResponse
import com.gymcompanion.app.data.remote.mapper.ExerciseDBMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test unitario para verificar el mapeo de ejercicios de la API a entidades locales
 */
class ExerciseMappingTest {

    @Test
    fun testExerciseMapping() {
        // Crear un ejercicio de ejemplo de la API
        val apiExercise = ExerciseDBExercise(
            exerciseId = "1",
            name = "Push-up",
            bodyParts = listOf("chest"),
            equipments = listOf("body weight"),
            imageUrl = "https://example.com/pushup.gif",
            targetMuscles = listOf("pectorals"),
            secondaryMuscles = listOf("triceps", "shoulders"),
            instructions = listOf("Get in plank position", "Lower your body", "Push back up"),
            exerciseType = "weight_reps",
            videoUrl = null,
            keywords = null,
            overview = null,
            exerciseTips = null,
            variations = null,
            relatedExerciseIds = null
        )

        val apiResponse = ExerciseDBResponse(
            exercises = listOf(apiExercise),
            total = 1,
            page = 1,
            pageSize = 10
        )

        // Mapear a entidades locales
        val localExercises = ExerciseDBMapper.toExerciseEntities(apiResponse.exercises)

        // Verificar el mapeo
        assertEquals("Debería mapearse 1 ejercicio", 1, localExercises.size)

        val localExercise = localExercises[0]
        assertEquals("Nombre debería mapearse correctamente", "Push-up", localExercise.name)
        assertEquals("Grupo muscular debería mapearse correctamente", "Pecho", localExercise.muscleGroup)
        assertEquals("Músculos objetivo deberían mapearse correctamente", "pectorals", localExercise.targetMuscles)
        assertEquals("Equipamiento debería mapearse correctamente", "body weight", localExercise.equipmentNeeded)
        assertEquals("URL de ilustración debería mapearse correctamente", "https://example.com/pushup.gif", localExercise.illustrationPath)
        assertEquals("Instrucciones deberían mapearse correctamente", "[\"Get in plank position\",\"Lower your body\",\"Push back up\"]", localExercise.instructionsSteps)

        println("✅ Test de mapeo exitoso")
    }

    @Test
    fun testMultipleExercisesMapping() {
        // Crear múltiples ejercicios
        val exercises = listOf(
            ExerciseDBExercise(exerciseId = "1", name = "Push-up", bodyParts = listOf("chest"), equipments = listOf("body weight"), imageUrl = "", targetMuscles = listOf("pectorals"), secondaryMuscles = emptyList(), instructions = emptyList(), exerciseType = "weight_reps", videoUrl = null, keywords = null, overview = null, exerciseTips = null, variations = null, relatedExerciseIds = null),
            ExerciseDBExercise(exerciseId = "2", name = "Squat", bodyParts = listOf("legs"), equipments = listOf("barbell"), imageUrl = "", targetMuscles = listOf("quadriceps"), secondaryMuscles = emptyList(), instructions = emptyList(), exerciseType = "weight_reps", videoUrl = null, keywords = null, overview = null, exerciseTips = null, variations = null, relatedExerciseIds = null),
            ExerciseDBExercise(exerciseId = "3", name = "Pull-up", bodyParts = listOf("back"), equipments = listOf("body weight"), imageUrl = "", targetMuscles = listOf("lats"), secondaryMuscles = emptyList(), instructions = emptyList(), exerciseType = "weight_reps", videoUrl = null, keywords = null, overview = null, exerciseTips = null, variations = null, relatedExerciseIds = null)
        )

        val apiResponse = ExerciseDBResponse(
            exercises = exercises,
            total = 3,
            page = 1,
            pageSize = 10
        )
        val localExercises = ExerciseDBMapper.toExerciseEntities(apiResponse.exercises)

        assertEquals("Deberían mapearse 3 ejercicios", 3, localExercises.size)

        // Verificar que todos los ejercicios tienen nombres únicos
        val names = localExercises.map { it.name }
        assertEquals("Nombres deberían ser únicos", 3, names.distinct().size)
        assertTrue("Debería contener Push-up", names.contains("Push-up"))
        assertTrue("Debería contener Squat", names.contains("Squat"))
        assertTrue("Debería contener Pull-up", names.contains("Pull-up"))

        println("✅ Test de mapeo múltiple exitoso")
    }
}