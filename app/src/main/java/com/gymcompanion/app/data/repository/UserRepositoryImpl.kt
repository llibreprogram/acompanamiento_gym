package com.gymcompanion.app.data.repository

import com.gymcompanion.app.data.local.dao.UserDao
import com.gymcompanion.app.data.local.entity.UserEntity
import com.gymcompanion.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de usuarios
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {
    
    override suspend fun insertUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }
    
    override suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
    
    override fun getCurrentUser(): Flow<UserEntity?> {
        return userDao.getCurrentUser()
    }
    
    override fun getUserById(userId: Long): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }
    
    override suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }
}
