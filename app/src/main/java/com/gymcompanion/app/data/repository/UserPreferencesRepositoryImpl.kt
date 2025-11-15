package com.gymcompanion.app.data.repository

import com.gymcompanion.app.data.local.dao.UserPreferencesDao
import com.gymcompanion.app.data.local.entity.UserPreferencesEntity
import com.gymcompanion.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao
) : UserPreferencesRepository {
    
    override fun getPreferences(userId: Long): Flow<UserPreferencesEntity?> {
        return userPreferencesDao.getPreferences(userId)
    }
    
    override suspend fun getPreferencesOnce(userId: Long): UserPreferencesEntity? {
        return userPreferencesDao.getPreferencesOnce(userId)
    }
    
    override suspend fun updateWeightUnit(userId: Long, unit: String) {
        // Asegurar que existen preferencias
        initializeDefaultPreferences(userId)
        userPreferencesDao.updateWeightUnit(userId, unit)
    }
    
    override suspend fun updateHeightUnit(userId: Long, unit: String) {
        // Asegurar que existen preferencias
        initializeDefaultPreferences(userId)
        userPreferencesDao.updateHeightUnit(userId, unit)
    }
    
    override suspend fun updateDistanceUnit(userId: Long, unit: String) {
        // Asegurar que existen preferencias
        initializeDefaultPreferences(userId)
        userPreferencesDao.updateDistanceUnit(userId, unit)
    }
    
    override suspend fun updateTheme(userId: Long, theme: String) {
        userPreferencesDao.updateTheme(userId, theme)
    }
    
    override suspend fun updateLanguage(userId: Long, language: String) {
        userPreferencesDao.updateLanguage(userId, language)
    }
    
    override suspend fun initializeDefaultPreferences(userId: Long) {
        val existing = userPreferencesDao.getPreferencesOnce(userId)
        if (existing == null) {
            userPreferencesDao.insertPreferences(
                UserPreferencesEntity(
                    userId = userId,
                    weightUnit = "kg",
                    heightUnit = "cm",
                    distanceUnit = "km",
                    theme = "system",
                    language = "es"
                )
            )
        }
    }
}
