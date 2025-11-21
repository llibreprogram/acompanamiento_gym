package com.gymcompanion.app.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gymcompanion.app.data.local.dao.ExerciseDao
import com.gymcompanion.app.data.repository.FreeExerciseDBRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    
    private suspend fun initializeData() {
        val exerciseDao = exerciseDaoProvider.get()
        
        // Cargar ejercicios desde Free Exercise DB (assets)
        val exercises = freeExerciseDBRepository.loadExercisesFromAssets()
        
        if (exercises.isNotEmpty()) {
            exerciseDao.insertExercises(exercises)
            println("✅ Base de datos inicializada con ${exercises.size} ejercicios desde Free Exercise DB")
        } else {
            println("⚠️ No se pudieron cargar ejercicios desde Free Exercise DB")
        }
    }
}
