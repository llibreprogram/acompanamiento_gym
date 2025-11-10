package com.gymcompanion.app.domain.repository

import com.gymcompanion.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio para usuarios
 */
interface UserRepository {
    
    suspend fun insertUser(user: UserEntity): Long
    
    suspend fun updateUser(user: UserEntity)
    
    fun getCurrentUser(): Flow<UserEntity?>
    
    fun getUserById(userId: Long): Flow<UserEntity?>
    
    suspend fun deleteUser(user: UserEntity)
}
