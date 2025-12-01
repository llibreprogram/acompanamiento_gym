package com.gymcompanion.app.domain.usecase

import com.gymcompanion.app.data.local.dao.TrainingPhaseDao
import com.gymcompanion.app.data.local.entity.TrainingPhaseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PeriodizationManager @Inject constructor(
    private val trainingPhaseDao: TrainingPhaseDao
) {

    data class VolumeSettings(
        val sets: Int,
        val reps: String, // e.g., "8-12"
        val restSeconds: Int,
        val intensity: String // Description
    )

    fun getCurrentPhase(userId: Long): Flow<TrainingPhaseEntity?> {
        return trainingPhaseDao.getCurrentPhase(userId)
    }

    fun getVolumeForPhase(phaseName: String): VolumeSettings {
        return when (phaseName) {
            "Strength" -> VolumeSettings(5, "3-5", 180, "High Intensity (85-95% 1RM)")
            "Power" -> VolumeSettings(4, "1-3", 240, "Max Intensity (90-100% 1RM)")
            "Deload" -> VolumeSettings(2, "10-12", 90, "Low Intensity (50% 1RM)")
            else -> VolumeSettings(3, "8-12", 120, "Moderate Intensity (70-80% 1RM)") // Hypertrophy (Default)
        }
    }

    suspend fun checkAndSwitchPhase(userId: Long, currentPhase: TrainingPhaseEntity?) {
        // Simple logic: If phase > 4 weeks, switch to next.
        // This would require more complex logic and user interaction in a real app.
        // For now, we just provide the tools to read the phase.
    }
}
