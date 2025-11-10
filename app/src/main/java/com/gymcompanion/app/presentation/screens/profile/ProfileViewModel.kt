package com.gymcompanion.app.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.data.local.entity.BodyMetricsEntity
import com.gymcompanion.app.data.local.entity.UserEntity
import com.gymcompanion.app.domain.repository.BodyMetricsRepository
import com.gymcompanion.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de perfil y gestión de datos corporales
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val bodyMetricsRepository: BodyMetricsRepository
) : ViewModel() {
    
    // Estado del usuario actual
    val currentUser: StateFlow<UserEntity?> = userRepository.getCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Métricas corporales más recientes
    val latestMetrics: StateFlow<BodyMetricsEntity?> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                bodyMetricsRepository.getLatestMetricsByUser(user.id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Estado de UI
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Loading
                
                // Crear usuario por defecto si no existe
                currentUser.first { it != null }?.let {
                    _uiState.value = ProfileUiState.Success
                } ?: run {
                    createDefaultUser()
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    private suspend fun createDefaultUser() {
        val defaultUser = UserEntity(
            name = "Usuario",
            email = null,
            dateOfBirth = System.currentTimeMillis() - (25 * 365 * 24 * 60 * 60 * 1000L), // 25 años atrás
            gender = "other"
        )
        userRepository.insertUser(defaultUser)
        _uiState.value = ProfileUiState.Success
    }
    
    /**
     * Guarda las métricas corporales del usuario
     */
    fun saveBodyMetrics(
        weight: Double,
        height: Double,
        experienceLevel: String,
        bodyFatPercentage: Double?,
        chestMeasurement: Double?,
        waistMeasurement: Double?,
        hipsMeasurement: Double?,
        thighMeasurement: Double?,
        armMeasurement: Double?,
        calfMeasurement: Double?,
        notes: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Saving
                
                val user = currentUser.value
                if (user == null) {
                    _uiState.value = ProfileUiState.Error("Usuario no encontrado")
                    return@launch
                }
                
                // Calcular IMC
                val bmi = BodyMetricsEntity.calculateBMI(weight, height)
                
                val metrics = BodyMetricsEntity(
                    userId = user.id,
                    weight = weight,
                    height = height,
                    experienceLevel = experienceLevel,
                    bmi = bmi,
                    bodyFatPercentage = bodyFatPercentage,
                    chestMeasurement = chestMeasurement,
                    waistMeasurement = waistMeasurement,
                    hipsMeasurement = hipsMeasurement,
                    thighMeasurement = thighMeasurement,
                    armMeasurement = armMeasurement,
                    calfMeasurement = calfMeasurement,
                    notes = notes
                )
                
                bodyMetricsRepository.insertMetrics(metrics)
                _uiState.value = ProfileUiState.Success
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error al guardar datos")
            }
        }
    }
    
    /**
     * Actualiza el nombre del usuario
     */
    fun updateUserName(name: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val updatedUser = user.copy(
                    name = name,
                    updatedAt = System.currentTimeMillis()
                )
                userRepository.updateUser(updatedUser)
            }
        }
    }
}

/**
 * Estados de la UI del perfil
 */
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    object Success : ProfileUiState()
    object Saving : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
