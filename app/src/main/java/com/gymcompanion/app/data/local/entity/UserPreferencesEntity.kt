package com.gymcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val userId: Long,
    val weightUnit: String = "kg", // "kg" or "lb"
    val heightUnit: String = "cm", // "cm" or "ft"
    val distanceUnit: String = "km", // "km" or "mi"
    val theme: String = "system", // "light", "dark", "system"
    val language: String = "es" // "es", "en"
)
