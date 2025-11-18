package com.gymcompanion.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.gymcompanion.app.data.repository.ExerciseDBRepositoryImpl
import androidx.room.Room
import com.gymcompanion.app.data.local.GymDatabase
import com.gymcompanion.app.data.remote.api.ExerciseDBApiService
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Worker para sincronizar ejercicios de ExerciseDB API a la base de datos local
 *
 * Se ejecuta en background periódicamente o bajo demanda
 * Reporta progreso en tiempo real
 *
 * TEMPORAL: Sin Hilt para probar funcionalidad básica
 */
class ExerciseSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository by lazy {
        val database = Room.databaseBuilder(
            applicationContext,
            GymDatabase::class.java,
            GymDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
        val apiService = createApiService()
        ExerciseDBRepositoryImpl(apiService, database.exerciseDao())
    }

    private fun createApiService(): ExerciseDBApiService {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Interceptor para headers de RapidAPI
        val rapidApiInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-RapidAPI-Key", "c3b499a878mshc79e4d31000b3bbp160079jsn34e8c9720a8b")
                .addHeader("X-RapidAPI-Host", "exercisedb-api-v1-dataset1.p.rapidapi.com")
                .build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(rapidApiInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://exercisedb-api-v1-dataset1.p.rapidapi.com/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ExerciseDBApiService::class.java)
    }

    companion object {
        const val WORK_NAME = "exercise_sync_work"
        const val PROGRESS_KEY = "progress"
        const val TOTAL_KEY = "total"
        const val STATUS_KEY = "status"
        const val MESSAGE_KEY = "message"

        // Estados de sincronización
        const val STATUS_STARTING = "starting"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_SUCCESS = "success"
        const val STATUS_ERROR = "error"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        android.util.Log.d("ExerciseSyncWorker", "doWork() started")
        try {
            // Reportar inicio
            setProgress(workDataOf(
                STATUS_KEY to STATUS_STARTING,
                MESSAGE_KEY to "Iniciando sincronización..."
            ))
            android.util.Log.d("ExerciseSyncWorker", "Progress set to STARTING")

            // Realizar la sincronización
            val result = repository.syncExercisesToLocal()
            android.util.Log.d("ExerciseSyncWorker", "Repository sync completed with result: $result")

            result.fold(
                onSuccess = { exercisesSynced ->
                    android.util.Log.d("ExerciseSyncWorker", "Sync successful, exercises synced: $exercisesSynced")
                    setProgress(workDataOf(
                        STATUS_KEY to STATUS_SUCCESS,
                        TOTAL_KEY to exercisesSynced,
                        MESSAGE_KEY to "Sincronizados $exercisesSynced ejercicios"
                    ))
                    return@withContext Result.success(workDataOf(
                        TOTAL_KEY to exercisesSynced,
                        MESSAGE_KEY to "Sincronización completada exitosamente"
                    ))
                },
                onFailure = { exception ->
                    android.util.Log.e("ExerciseSyncWorker", "Sync failed with exception: ${exception.message}", exception)
                    setProgress(workDataOf(
                        STATUS_KEY to STATUS_ERROR,
                        MESSAGE_KEY to "Error: ${exception.message}"
                    ))
                    if (exception is java.io.IOException ||
                        exception.message?.contains("timeout", ignoreCase = true) == true) {
                        return@withContext Result.retry()
                    } else {
                        return@withContext Result.failure(workDataOf(
                            MESSAGE_KEY to (exception.message ?: "Error desconocido")
                        ))
                    }
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("ExerciseSyncWorker", "Unexpected error in doWork(): ${e.message}", e)
            setProgress(workDataOf(
                STATUS_KEY to STATUS_ERROR,
                MESSAGE_KEY to "Error inesperado: ${e.message}"
            ))

            return@withContext Result.failure(workDataOf(
                MESSAGE_KEY to (e.message ?: "Error inesperado")
            ))
        }
    }
}