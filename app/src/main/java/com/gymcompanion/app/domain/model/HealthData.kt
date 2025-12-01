package com.gymcompanion.app.domain.model

import java.time.Instant

/**
 * Data class for daily steps count
 */
data class DailySteps(
    val count: Long,
    val date: Instant
)

/**
 * Data class for weight records
 */
data class WeightRecord(
    val weightKg: Double,
    val timestamp: Instant
)

/**
 * Data class for heart rate data
 */
data class HeartRateData(
    val beatsPerMinute: Long,
    val timestamp: Instant
)

/**
 * Aggregated health data for display
 */
data class HealthData(
    val steps: DailySteps? = null,
    val weight: WeightRecord? = null,
    val heartRate: HeartRateData? = null,
    val lastSyncTime: Instant? = null
)
