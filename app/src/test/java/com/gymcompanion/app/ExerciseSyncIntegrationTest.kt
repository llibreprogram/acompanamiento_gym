package com.gymcompanion.app

import com.gymcompanion.app.data.local.dao.ExerciseDao
import com.gymcompanion.app.data.remote.api.ExerciseDBApiService
import com.gymcompanion.app.data.remote.model.ExerciseDBExercise
import retrofit2.Response
import com.gymcompanion.app.data.repository.ExerciseDBRepositoryImpl
import com.gymcompanion.app.domain.repository.ExerciseDBRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Test unitario para verificar la sincronización completa de ejercicios
 */
class ExerciseSyncIntegrationTest {

    @Mock
    private lateinit var apiService: ExerciseDBApiService

    @Mock
    private lateinit var exerciseDao: ExerciseDao

    private lateinit var repository: ExerciseDBRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = ExerciseDBRepositoryImpl(apiService, exerciseDao)
    }

    @Test
    fun testFullSyncProcess() = runBlocking {
        // Crear datos de prueba de la API
        val mockExercises = listOf(
            ExerciseDBExercise(
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
            ),
            ExerciseDBExercise(
                exerciseId = "2",
                name = "Squat",
                bodyParts = listOf("legs"),
                equipments = listOf("barbell"),
                imageUrl = "https://example.com/squat.gif",
                targetMuscles = listOf("quadriceps"),
                secondaryMuscles = listOf("glutes", "hamstrings"),
                instructions = listOf("Stand with feet shoulder-width", "Lower your body", "Stand back up"),
                exerciseType = "weight_reps",
                videoUrl = null,
                keywords = null,
                overview = null,
                exerciseTips = null,
                variations = null,
                relatedExerciseIds = null
            ),
            ExerciseDBExercise(
                exerciseId = "3",
                name = "Pull-up",
                bodyParts = listOf("back"),
                equipments = listOf("body weight"),
                imageUrl = "https://example.com/pullup.gif",
                targetMuscles = listOf("lats"),
                secondaryMuscles = listOf("biceps", "rhomboids"),
                instructions = listOf("Hang from bar", "Pull your body up", "Lower back down"),
                exerciseType = "weight_reps",
                videoUrl = null,
                keywords = null,
                overview = null,
                exerciseTips = null,
                variations = null,
                relatedExerciseIds = null
            )
        )

        // Configurar mocks
        whenever(apiService.getAllExercises(1, 100)).thenReturn(Response.success(mockExercises))
        whenever(apiService.getAllExercises(2, 100)).thenReturn(Response.success(emptyList())) // No more pages

        // Ejecutar sincronización
        val result = repository.syncExercisesToLocal()

        // Verificar resultado
        assertTrue("La sincronización debería ser exitosa", result.isSuccess)
        val syncedCount = result.getOrNull()
        assertEquals("Deberían sincronizarse 3 ejercicios", 3, syncedCount)

        println("✅ Test de sincronización completa exitoso: $syncedCount ejercicios sincronizados")
    }

    @Test
    fun testSyncWithApiError() = runBlocking {
        // Configurar mock para simular error de API
        whenever(apiService.getAllExercises(any(), any()))
            .thenThrow(RuntimeException("Network error"))

        // Ejecutar sincronización
        val result = repository.syncExercisesToLocal()

        // Verificar que falló
        assertTrue("La sincronización debería fallar con error de red", result.isFailure)
        val exception = result.exceptionOrNull()
        assertEquals("El mensaje de error debería ser correcto", "Network error", exception?.message)

        println("✅ Test de manejo de errores exitoso")
    }

    @Test
    fun testSyncWithEmptyResponse() = runBlocking {
        // Configurar mock para devolver respuesta vacía
        whenever(apiService.getAllExercises(any(), any()))
            .thenReturn(Response.success(emptyList()))

        // Ejecutar sincronización
        val result = repository.syncExercisesToLocal()

        // Verificar resultado
        assertTrue("La sincronización debería ser exitosa con 0 ejercicios", result.isSuccess)
        val syncedCount = result.getOrNull()
        assertEquals("No deberían sincronizarse ejercicios", 0, syncedCount)

        println("✅ Test de respuesta vacía exitoso")
    }
}