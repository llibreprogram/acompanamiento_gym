package com.gymcompanion.app.di

import com.gymcompanion.app.data.repository.BodyMetricsRepositoryImpl
import com.gymcompanion.app.data.repository.ExerciseRepositoryImpl
import com.gymcompanion.app.data.repository.RoutineRepositoryImpl
import com.gymcompanion.app.data.repository.UserRepositoryImpl
import com.gymcompanion.app.data.repository.WorkoutSessionRepositoryImpl
import com.gymcompanion.app.data.repository.UserPreferencesRepositoryImpl
import com.gymcompanion.app.data.repository.ExerciseDBRepositoryImpl
import com.gymcompanion.app.domain.repository.BodyMetricsRepository
import com.gymcompanion.app.domain.repository.ExerciseRepository
import com.gymcompanion.app.domain.repository.RoutineRepository
import com.gymcompanion.app.domain.repository.UserRepository
import com.gymcompanion.app.domain.repository.WorkoutSessionRepository
import com.gymcompanion.app.domain.repository.UserPreferencesRepository
import com.gymcompanion.app.domain.repository.ExerciseDBRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para proporcionar las implementaciones de los repositorios
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindBodyMetricsRepository(
        bodyMetricsRepositoryImpl: BodyMetricsRepositoryImpl
    ): BodyMetricsRepository
    
    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): ExerciseRepository
    
    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        routineRepositoryImpl: RoutineRepositoryImpl
    ): RoutineRepository
    
    @Binds
    @Singleton
    abstract fun bindWorkoutSessionRepository(
        workoutSessionRepositoryImpl: WorkoutSessionRepositoryImpl
    ): WorkoutSessionRepository
    
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
    
    @Binds
    @Singleton
    abstract fun bindExerciseDBRepository(
        exerciseDBRepositoryImpl: ExerciseDBRepositoryImpl
    ): ExerciseDBRepository
}
