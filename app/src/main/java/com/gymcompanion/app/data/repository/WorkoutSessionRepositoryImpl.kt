package com.gymcompanion.app.data.repository

import com.gymcompanion.app.data.local.dao.WorkoutDao
import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.data.local.entity.WorkoutSessionWithSets
import com.gymcompanion.app.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

/**
 * Implementación del repositorio de sesiones de entrenamiento
 */
class WorkoutSessionRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutSessionRepository {
    
    override fun getAllSessions(): Flow<List<WorkoutSessionEntity>> {
        return workoutSessionDao.getAllSessions()
    }
    
    override fun getSessionById(sessionId: Long): Flow<WorkoutSessionWithSets?> {
        return workoutSessionDao.getSessionWithSets(sessionId)
    }
    
    override fun getSessionsByRoutine(routineId: Long): Flow<List<WorkoutSessionEntity>> {
        return workoutSessionDao.getSessionsByRoutine(routineId)
    }
    
    override fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutSessionEntity>> {
        return workoutSessionDao.getSessionsByDateRange(startDate, endDate)
    }
    
    override fun getLastSession(): Flow<WorkoutSessionEntity?> {
        return workoutSessionDao.getLastSession()
    }
    
    override fun getSessionsThisWeek(): Flow<List<WorkoutSessionEntity>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis
        
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
        val endOfWeek = calendar.timeInMillis
        
        return workoutSessionDao.getSessionsByDateRange(startOfWeek, endOfWeek)
    }
    
    override suspend fun insertSession(session: WorkoutSessionEntity): Long {
        return workoutSessionDao.insertSession(session)
    }
    
    override suspend fun updateSession(session: WorkoutSessionEntity) {
        workoutSessionDao.updateSession(session)
    }
    
    override suspend fun deleteSession(session: WorkoutSessionEntity) {
        workoutSessionDao.deleteSession(session)
    }
    
    override suspend fun insertExerciseSet(exerciseSet: ExerciseSetEntity) {
        workoutSessionDao.insertExerciseSet(exerciseSet)
    }
    
    override suspend fun updateExerciseSet(exerciseSet: ExerciseSetEntity) {
        workoutSessionDao.updateExerciseSet(exerciseSet)
    }
    
    override suspend fun deleteExerciseSet(exerciseSet: ExerciseSetEntity) {
        workoutSessionDao.deleteExerciseSet(exerciseSet)
    }
    
    override fun getSessionSets(sessionId: Long): Flow<List<ExerciseSetEntity>> {
        return workoutSessionDao.getSessionSets(sessionId)
    }
    
    override fun getExerciseSetsInSession(sessionId: Long, exerciseId: Long): Flow<List<ExerciseSetEntity>> {
        return workoutSessionDao.getExerciseSetsInSession(sessionId, exerciseId)
    }
    
    override suspend fun completeSession(sessionId: Long, endTime: Long) {
        workoutSessionDao.completeSession(sessionId, endTime)
    }
    
    override suspend fun calculateSessionVolume(sessionId: Long): Double {
        val sets = getSessionSets(sessionId).first()
        return sets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
    }
}
