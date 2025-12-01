package com.gymcompanion.app.domain.usecase

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.gymcompanion.app.domain.model.DailySteps
import com.gymcompanion.app.domain.model.HeartRateData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for Health Connect integration
 * Handles reading and writing health data from/to Health Connect
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    companion object {
        private const val TAG = "HealthConnectManager"
        
        // Required permissions
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class)
        )
    }

    /**
     * Check if Health Connect is available on this device
     */
    suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Health Connect availability", e)
            false
        }
    }

    /**
     * Check if we have all required permissions
     */
    suspend fun hasAllPermissions(): Boolean = withContext(Dispatchers.IO) {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            PERMISSIONS.all { it in granted }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            false
        }
    }

    /**
     * Read today's step count
     */
    suspend fun getTodaySteps(): DailySteps? = withContext(Dispatchers.IO) {
        try {
            val now = LocalDateTime.now()
            val startOfDay = now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = Instant.now()

            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )

            val response = healthConnectClient.readRecords(request)
            val totalSteps = response.records.sumOf { it.count }

            if (totalSteps > 0) {
                DailySteps(
                    count = totalSteps,
                    date = startOfDay
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading steps", e)
            null
        }
    }

    /**
     * Read the most recent weight record
     */
    suspend fun getLatestWeight(): com.gymcompanion.app.domain.model.WeightRecord? = withContext(Dispatchers.IO) {
        try {
            val now = Instant.now()
            val thirtyDaysAgo = now.minusSeconds(30L * 24 * 60 * 60)

            val request = ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(thirtyDaysAgo, now)
            )

            val response = healthConnectClient.readRecords(request)
            val latestRecord = response.records.maxByOrNull { it.time }

            latestRecord?.let {
                com.gymcompanion.app.domain.model.WeightRecord(
                    weightKg = it.weight.inKilograms,
                    timestamp = it.time
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading weight", e)
            null
        }
    }

    /**
     * Read average heart rate for today
     */
    suspend fun getTodayAverageHeartRate(): HeartRateData? = withContext(Dispatchers.IO) {
        try {
            val now = LocalDateTime.now()
            val startOfDay = now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = Instant.now()

            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )

            val response = healthConnectClient.readRecords(request)
            
            if (response.records.isEmpty()) return@withContext null

            // Calculate average from all samples
            val allSamples = response.records.flatMap { it.samples }
            if (allSamples.isEmpty()) return@withContext null

            val averageBpm = allSamples.map { it.beatsPerMinute }.average().toLong()

            HeartRateData(
                beatsPerMinute = averageBpm,
                timestamp = endOfDay
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error reading heart rate", e)
            null
        }
    }

    /**
     * Get aggregated health data
     */
    suspend fun getHealthData(): com.gymcompanion.app.domain.model.HealthData = withContext(Dispatchers.IO) {
        com.gymcompanion.app.domain.model.HealthData(
            steps = getTodaySteps(),
            weight = getLatestWeight(),
            heartRate = getTodayAverageHeartRate(),
            lastSyncTime = Instant.now()
        )
    }
}
