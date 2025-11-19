package com.gymcompanion.app.di

import com.gymcompanion.app.data.remote.api.ExerciseDBApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Módulo de Hilt para proveer dependencias de red (Retrofit, OkHttp)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * Base URL para ExerciseDB API (RapidAPI)
     */
    private const val EXERCISE_DB_BASE_URL = "https://exercisedb-api-v1-dataset1.p.rapidapi.com/api/v1/"
    
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ExerciseDBRetrofit
    
    /**
     * Provee un cliente HTTP configurado
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Interceptor para headers de RapidAPI
        val rapidApiInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-RapidAPI-Key", "c3b499a878mshc79e4d31000b3bbp160079jsn34e8c9720a8b")
                .addHeader("X-RapidAPI-Host", "exercisedb-api-v1-dataset1.p.rapidapi.com")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(rapidApiInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Provee Retrofit configurado para ExerciseDB API
     */
    @Provides
    @Singleton
    @ExerciseDBRetrofit
    fun provideExerciseDBRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(EXERCISE_DB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Provee el servicio de API de ExerciseDB
     */
    @Provides
    @Singleton
    fun provideExerciseDBApiService(
        @ExerciseDBRetrofit retrofit: Retrofit
    ): ExerciseDBApiService {
        return retrofit.create(ExerciseDBApiService::class.java)
    }
}
