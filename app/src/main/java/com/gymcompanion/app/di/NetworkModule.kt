package com.gymcompanion.app.di

import com.gymcompanion.app.data.remote.api.ExerciseDBApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
     * Base URL para ExerciseDB API
     * Para producción, considera usar tu propia instancia o API key
     */
    private const val EXERCISE_DB_BASE_URL = "https://api.exercisedb.dev/api/v2/"
    
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
        
        return OkHttpClient.Builder()
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
