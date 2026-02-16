package com.gymcompanion.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones de base de datos para preservar datos del usuario
 */
object DatabaseMigrations {
    
    /**
     * Migración de versión 2 a 3
     * Añade tabla training_phases
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Crear tabla training_phases
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `training_phases` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `userId` INTEGER NOT NULL,
                    `name` TEXT NOT NULL,
                    `startDate` INTEGER NOT NULL,
                    `endDate` INTEGER,
                    `focusType` TEXT NOT NULL,
                    `notes` TEXT,
                    FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Crear índice para userId
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS `index_training_phases_userId` 
                ON `training_phases` (`userId`)
            """.trimIndent())
        }
    }
    
    /**
     * Migración de versión 3 a 4
     * Actualiza UserEntity con nuevos campos
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Añadir nuevos campos a la tabla users si no existen
            try {
                database.execSQL("ALTER TABLE users ADD COLUMN gender TEXT")
            } catch (e: Exception) {
                // Column already exists, ignore
            }
            
            try {
                database.execSQL("ALTER TABLE users ADD COLUMN birthDate INTEGER")
            } catch (e: Exception) {
                // Column already exists, ignore
            }
            
            try {
                database.execSQL("ALTER TABLE users ADD COLUMN activityLevel TEXT")
            } catch (e: Exception) {
                // Column already exists, ignore
            }
        }
    }
    
    /**
     * Migración de versión 4 a 5
     * Actualiza foreign key constraint en routine_exercises para prevenir pérdida de datos
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Room no permite modificar foreign keys directamente
            // Necesitamos recrear la tabla con el nuevo constraint
            
            // 1. Crear tabla temporal con los nuevos constraints
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `routine_exercises_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `routineId` INTEGER NOT NULL,
                    `exerciseId` INTEGER NOT NULL,
                    `orderIndex` INTEGER NOT NULL,
                    `plannedSets` INTEGER NOT NULL,
                    `plannedReps` TEXT NOT NULL,
                    `plannedWeight` REAL,
                    `restTimeSeconds` INTEGER NOT NULL DEFAULT 60,
                    `notes` TEXT,
                    FOREIGN KEY(`routineId`) REFERENCES `routines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
            """.trimIndent())
            
            // 2. Copiar datos de la tabla antigua a la nueva
            database.execSQL("""
                INSERT INTO `routine_exercises_new` 
                SELECT * FROM `routine_exercises`
            """.trimIndent())
            
            // 3. Eliminar tabla antigua
            database.execSQL("DROP TABLE `routine_exercises`")
            
            // 4. Renombrar tabla nueva
            database.execSQL("ALTER TABLE `routine_exercises_new` RENAME TO `routine_exercises`")
            
            // 5. Recrear índices
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS `index_routine_exercises_routineId` 
                ON `routine_exercises` (`routineId`)
            """.trimIndent())
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS `index_routine_exercises_exerciseId` 
                ON `routine_exercises` (`exerciseId`)
            """.trimIndent())
        }
    }
    
    /**
     * Lista de todas las migraciones disponibles
     */


    /**
     * Migración de versión 5 a 6
     * Elimina campo activityLevel de UserEntity ya que no se utiliza
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // --- FIX USERS TABLE ---
            // 1. Crear tabla temporal sin activityLevel
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `users_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `email` TEXT,
                    `dateOfBirth` INTEGER NOT NULL,
                    `gender` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `weight` REAL,
                    `height` REAL,
                    `experienceLevel` TEXT,
                    `goal` TEXT,
                    `restrictions` TEXT,
                    `preferences` TEXT
                )
            """.trimIndent())

            // 2. Copiar datos
            database.execSQL("""
                INSERT INTO `users_new` (id, name, email, dateOfBirth, gender, createdAt, updatedAt, weight, height, experienceLevel, goal, restrictions, preferences)
                SELECT id, name, email, dateOfBirth, gender, createdAt, updatedAt, weight, height, experienceLevel, goal, restrictions, preferences FROM `users`
            """)

            // 3. Eliminar tabla antigua
            database.execSQL("DROP TABLE `users`")

            // 4. Renombrar tabla nueva
            database.execSQL("ALTER TABLE `users_new` RENAME TO `users`")

            // --- FIX ROUTINE_EXERCISES TABLE ---
            // Fixes default value mismatch (60 -> undefined) and FK mismatch (RESTRICT -> CASCADE)
            
            // 1. Create new table matching Entity definition
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `routine_exercises_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `routineId` INTEGER NOT NULL,
                    `exerciseId` INTEGER NOT NULL,
                    `orderIndex` INTEGER NOT NULL,
                    `plannedSets` INTEGER NOT NULL,
                    `plannedReps` TEXT NOT NULL,
                    `plannedWeight` REAL,
                    `restTimeSeconds` INTEGER NOT NULL,
                    `notes` TEXT,
                    FOREIGN KEY(`routineId`) REFERENCES `routines`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 2. Copy data
            database.execSQL("""
                INSERT INTO `routine_exercises_new` (id, routineId, exerciseId, orderIndex, plannedSets, plannedReps, plannedWeight, restTimeSeconds, notes)
                SELECT id, routineId, exerciseId, orderIndex, plannedSets, plannedReps, plannedWeight, restTimeSeconds, notes FROM `routine_exercises`
            """)
            
            // 3. Drop old table
            database.execSQL("DROP TABLE `routine_exercises`")
            
            // 4. Rename new table
            database.execSQL("ALTER TABLE `routine_exercises_new` RENAME TO `routine_exercises`")
            
            // 5. Recreate indices
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS `index_routine_exercises_routineId` 
                ON `routine_exercises` (`routineId`)
            """.trimIndent())
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS `index_routine_exercises_exerciseId` 
                ON `routine_exercises` (`exerciseId`)
            """.trimIndent())
        }
    }
    
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add new columns to training_phases for deload tracking
            db.execSQL("ALTER TABLE training_phases ADD COLUMN currentWeek INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE training_phases ADD COLUMN totalWeeksBeforeDeload INTEGER NOT NULL DEFAULT 4")
        }
    }
    
    // Actualizar lista de migraciones
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7
    )
}
