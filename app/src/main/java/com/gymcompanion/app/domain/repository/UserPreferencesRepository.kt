package com.gymcompanion.app.domain.repository

import com.gymcompanion.app.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getPreferences(userId: Long): Flow<UserPreferencesEntity?>
    suspend fun getPreferencesOnce(userId: Long): UserPreferencesEntity?
    suspend fun updateWeightUnit(userId: Long, unit: String)
    suspend fun updateHeightUnit(userId: Long, unit: String)
    suspend fun updateDistanceUnit(userId: Long, unit: String)
    suspend fun updateTheme(userId: Long, theme: String)
    suspend fun updateLanguage(userId: Long, language: String)
    suspend fun initializeDefaultPreferences(userId: Long)
}
