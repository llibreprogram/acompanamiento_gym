package com.gymcompanion.app.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymcompanion.app.data.local.dao.ExerciseDao
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.data.repository.FreeExerciseDBRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Provider

/**
 * Callback para inicializar la base de datos con datos desde Free Exercise DB
 * Se ejecuta automáticamente cuando se crea la base de datos por primera vez
 */
class DatabaseInitializer @Inject constructor(
    private val exerciseDaoProvider: Provider<ExerciseDao>,
    private val freeExerciseDBRepository: FreeExerciseDBRepository
) : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Inicializar datos en un scope de coroutines
        CoroutineScope(Dispatchers.IO).launch {
            initializeData()
        }
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Forzar actualización de datos al abrir para corregir URLs de imágenes
        // Esto es seguro porque usa OnConflictStrategy.REPLACE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                initializeData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun initializeData() {
        val exerciseDao = exerciseDaoProvider.get()
        
        // Cargar ejercicios desde Free Exercise DB (assets)
        val newExercises = freeExerciseDBRepository.loadExercisesFromAssets()
        
        if (newExercises.isNotEmpty()) {
            // Obtener ejercicios existentes para preservar IDs
            val existingExercises = exerciseDao.getAllExercises().first()
            
            // Agrupar por nombre para manejar duplicados
            val existingMap = existingExercises.groupBy { it.name }
            
            val exercisesToInsert = mutableListOf<ExerciseEntity>()
            
            newExercises.forEach { newExercise ->
                val existingList = existingMap[newExercise.name]
                if (existingList != null && existingList.isNotEmpty()) {
                    // Si existen (incluso duplicados), actualizar TODOS para asegurar que el que se muestra tenga la imagen
                    existingList.forEach { existing ->
                        exercisesToInsert.add(newExercise.copy(id = existing.id))
                    }
                } else {
                    // Si no existe, insertar como nuevo (ID 0)
                    exercisesToInsert.add(newExercise)
                }
            }
            
            exerciseDao.insertExercises(exercisesToInsert)
            println("✅ Base de datos actualizada con ${exercisesToInsert.size} ejercicios")
        } else {
            println("⚠️ No se pudieron cargar ejercicios desde Free Exercise DB")
        }
    }
}
