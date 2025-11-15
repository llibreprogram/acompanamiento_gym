package com.gymcompanion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymcompanion.app.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun getPreferences(userId: Long): Flow<UserPreferencesEntity?>
    
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    suspend fun getPreferencesOnce(userId: Long): UserPreferencesEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: UserPreferencesEntity)
    
    @Update
    suspend fun updatePreferences(preferences: UserPreferencesEntity)
    
    @Query("UPDATE user_preferences SET weightUnit = :unit WHERE userId = :userId")
    suspend fun updateWeightUnit(userId: Long, unit: String)
    
    @Query("UPDATE user_preferences SET heightUnit = :unit WHERE userId = :userId")
    suspend fun updateHeightUnit(userId: Long, unit: String)
    
    @Query("UPDATE user_preferences SET distanceUnit = :unit WHERE userId = :userId")
    suspend fun updateDistanceUnit(userId: Long, unit: String)
    
    @Query("UPDATE user_preferences SET theme = :theme WHERE userId = :userId")
    suspend fun updateTheme(userId: Long, theme: String)
    
    @Query("UPDATE user_preferences SET language = :language WHERE userId = :userId")
    suspend fun updateLanguage(userId: Long, language: String)
}
