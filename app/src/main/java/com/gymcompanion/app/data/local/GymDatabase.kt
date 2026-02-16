package com.gymcompanion.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gymcompanion.app.data.local.dao.*
import com.gymcompanion.app.data.local.entity.*

/**
 * Base de datos principal de la aplicación
 * Contiene todas las entidades y DAOs necesarios para el funcionamiento
 */
@Database(
    entities = [
        UserEntity::class,
        BodyMetricsEntity::class,
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        ExerciseSetEntity::class,
        UserPreferencesEntity::class,
        TrainingPhaseEntity::class
    ],
    version = 7, // Added currentWeek and totalWeeksBeforeDeload to training_phases
    exportSchema = true
)
abstract class GymDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun bodyMetricsDao(): BodyMetricsDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun trainingPhaseDao(): TrainingPhaseDao
    
    companion object {
        const val DATABASE_NAME = "gym_companion_db"
    }
}
