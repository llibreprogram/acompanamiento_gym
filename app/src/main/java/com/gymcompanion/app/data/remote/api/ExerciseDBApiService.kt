
package com.gymcompanion.app.data.remote.api

import com.gymcompanion.app.data.remote.model.ExerciseDBExercise
import com.gymcompanion.app.data.remote.model.ExerciseDBResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API Service para ExerciseDB
 * Documentación: https://docs.exercisedb.dev
 */
interface ExerciseDBApiService {

    /**
     * Obtener todos los ejercicios (con paginación offset-based)
     */
    @GET("exercises")
    suspend fun getAllExercises(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<ExerciseDBResponse>

    /**
     * Obtener un ejercicio por ID
     */
    @GET("exercises/{id}")
    suspend fun getExerciseById(
        @Path("id") exerciseId: String
    ): Response<ExerciseDBExercise>

    /**
     * Buscar ejercicios por nombre
     */
    @GET("exercises/search")
    suspend fun searchExercises(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<List<ExerciseDBExercise>>

    /**
     * Filtrar ejercicios por parte del cuerpo
     */
    @GET("exercises/bodyPart/{bodyPart}")
    suspend fun getExercisesByBodyPart(
        @Path("bodyPart") bodyPart: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<List<ExerciseDBExercise>>

    /**
     * Filtrar ejercicios por equipamiento
     */
    @GET("exercises/equipment/{equipment}")
    suspend fun getExercisesByEquipment(
        @Path("equipment") equipment: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<List<ExerciseDBExercise>>

    /**
     * Filtrar ejercicios por músculo objetivo
     */
    @GET("exercises/target/{targetMuscle}")
    suspend fun getExercisesByTargetMuscle(
        @Path("targetMuscle") targetMuscle: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): Response<List<ExerciseDBExercise>>
}
