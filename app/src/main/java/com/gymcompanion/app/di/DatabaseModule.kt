package com.gymcompanion.app.di

import android.content.Context
import androidx.room.Room
import com.gymcompanion.app.data.local.DatabaseInitializer
import com.gymcompanion.app.data.local.GymDatabase
import com.gymcompanion.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proveer dependencias de base de datos
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideGymDatabase(
        @ApplicationContext context: Context,
        databaseInitializer: DatabaseInitializer
    ): GymDatabase {
        return Room.databaseBuilder(
            context,
            GymDatabase::class.java,
            GymDatabase.DATABASE_NAME
        )
            .addCallback(databaseInitializer)
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: GymDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    @Singleton
    fun provideBodyMetricsDao(database: GymDatabase): BodyMetricsDao {
        return database.bodyMetricsDao()
    }
    
    @Provides
    @Singleton
    fun provideExerciseDao(database: GymDatabase): ExerciseDao {
        return database.exerciseDao()
    }
    
    @Provides
    @Singleton
    fun provideRoutineDao(database: GymDatabase): RoutineDao {
        return database.routineDao()
    }
    
    @Provides
    @Singleton
    fun provideWorkoutDao(database: GymDatabase): WorkoutDao {
        return database.workoutDao()
    }
    
    @Provides
    @Singleton
    fun provideUserPreferencesDao(database: GymDatabase): UserPreferencesDao {
        return database.userPreferencesDao()
    }

    @Provides
    @Singleton
    fun provideTrainingPhaseDao(database: GymDatabase): TrainingPhaseDao {
        return database.trainingPhaseDao()
    }
}
