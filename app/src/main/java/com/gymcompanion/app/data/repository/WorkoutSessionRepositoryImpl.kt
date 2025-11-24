package com.gymcompanion.app.data.repository

import com.gymcompanion.app.data.local.dao.WorkoutDao
import com.gymcompanion.app.data.local.entity.WorkoutSessionEntity
import com.gymcompanion.app.data.local.entity.ExerciseSetEntity
import com.gymcompanion.app.data.local.entity.WorkoutSessionWithSets
import com.gymcompanion.app.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

/**
 * Implementación del repositorio de sesiones de entrenamiento
 */
class WorkoutSessionRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutSessionRepository {
    
    override fun getAllSessions(): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.getAllWorkoutSessionsByUser(1L) // TODO: Get actual user ID
    }
    
    override fun getSessionById(sessionId: Long): Flow<WorkoutSessionWithSets?> {
        return workoutDao.getWorkoutSessionById(sessionId).map { session ->
            session?.let {
                WorkoutSessionWithSets(
                    session = it,
                    sets = workoutDao.getSetsForWorkoutSession(sessionId).first()
                )
            }
        }
    }
    
    override fun getSessionsByRoutine(routineId: Long): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.getAllWorkoutSessionsByUser(1L).map { sessions ->
            sessions.filter { it.routineId == routineId }
        }
    }
    
    override fun getSessionsByDateRange(startDate: Long, endDate: Long): Flow<List<WorkoutSessionEntity>> {
        return workoutDao.getWorkoutSessionsInDateRange(1L, startDate, endDate)
    }
    
    override fun getLastSession(): Flow<WorkoutSessionEntity?> {
        return workoutDao.getRecentCompletedWorkouts(1L, 1)
            .map { it.firstOrNull() }
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
        
        return workoutDao.getWorkoutSessionsInDateRange(1L, startOfWeek, endOfWeek)
    }
    
    override suspend fun insertSession(session: WorkoutSessionEntity): Long {
        return workoutDao.insertWorkoutSession(session)
    }
    
    override suspend fun updateSession(session: WorkoutSessionEntity) {
        workoutDao.updateWorkoutSession(session)
    }
    
    override suspend fun deleteSession(session: WorkoutSessionEntity) {
        workoutDao.deleteWorkoutSession(session)
    }
    
    override suspend fun insertExerciseSet(exerciseSet: ExerciseSetEntity) {
        workoutDao.insertExerciseSet(exerciseSet)
    }
    
    override suspend fun updateExerciseSet(exerciseSet: ExerciseSetEntity) {
        workoutDao.updateExerciseSet(exerciseSet)
    }
    
    override suspend fun deleteExerciseSet(exerciseSet: ExerciseSetEntity) {
        workoutDao.deleteExerciseSet(exerciseSet)
    }
    
    override fun getSessionSets(sessionId: Long): Flow<List<ExerciseSetEntity>> {
        return workoutDao.getSetsForWorkoutSession(sessionId)
    }
    
    override fun getExerciseSetsInSession(sessionId: Long, exerciseId: Long): Flow<List<ExerciseSetEntity>> {
        return workoutDao.getSetsForExerciseInSession(sessionId, exerciseId)
    }
    
    override suspend fun completeSession(sessionId: Long, endTime: Long) {
        val session = workoutDao.getWorkoutSessionById(sessionId).first()
        session?.let {
            val durationMinutes = ((endTime - it.startTime) / 60000).toInt()
            workoutDao.updateWorkoutSession(
                it.copy(
                    endTime = endTime,
                    durationMinutes = durationMinutes,
                    isCompleted = true
                )
            )
        }
    }
    
    override suspend fun calculateSessionVolume(sessionId: Long): Double {
        val sets = getSessionSets(sessionId).first()
        return sets.sumOf { (it.weightUsed ?: 0.0) * it.repsCompleted }
    }

    override suspend fun getExerciseHistory(userId: Long, exerciseId: Long, limit: Int): List<WorkoutSessionWithSets> {
        val sessions = workoutDao.getSessionsByExercise(exerciseId, limit).first()
        return sessions.map { session ->
            val sets = workoutDao.getSetsForWorkoutSession(session.id).first()
            WorkoutSessionWithSets(session, sets)
        }
    }
}
