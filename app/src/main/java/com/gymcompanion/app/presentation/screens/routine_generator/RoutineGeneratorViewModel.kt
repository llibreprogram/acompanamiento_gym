package com.gymcompanion.app.presentation.screens.routine_generator

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.domain.model.*
import com.gymcompanion.app.domain.usecase.RoutineGeneratorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineGeneratorViewModel @Inject constructor(
    private val routineGeneratorUseCase: RoutineGeneratorUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RoutineGeneratorUiState())
    val uiState: StateFlow<RoutineGeneratorUiState> = _uiState.asStateFlow()
    
    fun updateGoal(goal: FitnessGoal) {
        _uiState.value = _uiState.value.copy(selectedGoal = goal)
    }
    
    fun updateLevel(level: FitnessLevel) {
        _uiState.value = _uiState.value.copy(selectedLevel = level)
    }
    
    fun updateDaysPerWeek(days: Int) {
        _uiState.value = _uiState.value.copy(daysPerWeek = days)
    }
    
    fun updateSessionDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(sessionDuration = minutes)
    }
    
    fun updateEquipment(equipment: AvailableEquipment) {
        _uiState.value = _uiState.value.copy(selectedEquipment = equipment)
    }
    
    fun updateConsecutiveDays(consecutive: Boolean) {
        _uiState.value = _uiState.value.copy(consecutiveDays = consecutive)
    }
    
    fun togglePhysicalLimitation(limitation: PhysicalLimitation) {
        val current = _uiState.value.physicalLimitations.toMutableList()
        if (current.contains(limitation)) {
            current.remove(limitation)
        } else {
            current.add(limitation)
        }
        _uiState.value = _uiState.value.copy(physicalLimitations = current)
    }
    
    fun generateRoutine() {
        val state = _uiState.value
        
        if (state.selectedGoal == null || state.selectedLevel == null || state.selectedEquipment == null) {
            _uiState.value = state.copy(error = "Por favor completa todos los campos")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isGenerating = true, error = null)
            
            try {
                // Obtener datos del perfil (simulado, reemplazar por acceso real al ProfileViewModel)
                val userProfile = getUserProfile()
                val request = RoutineGenerationRequest(
                    goal = state.selectedGoal,
                    level = state.selectedLevel,
                    daysPerWeek = state.daysPerWeek,
                    sessionDuration = state.sessionDuration,
                    equipment = state.selectedEquipment,
                    consecutiveDays = state.consecutiveDays,
                    physicalLimitations = state.physicalLimitations,
                    age = userProfile.age,
                    gender = userProfile.gender,
                    weight = userProfile.weight,
                    height = userProfile.height,
                    experienceLevel = userProfile.experienceLevel,
                    preferences = userProfile.preferences,
                    restrictions = userProfile.restrictions
                )
                
                Log.d("RoutineGeneratorViewModel", "Generando rutina con perfil: age=${userProfile.age}, gender=${userProfile.gender}, weight=${userProfile.weight}, height=${userProfile.height}, experienceLevel=${userProfile.experienceLevel}, preferences=${userProfile.preferences}, restrictions=${userProfile.restrictions}")
                
                val generatedRoutines = routineGeneratorUseCase.generateRoutine(request, userId = 1L)
                
                _uiState.value = state.copy(
                    isGenerating = false,
                    isGenerated = true,
                    generatedCount = generatedRoutines.size
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isGenerating = false,
                    error = "Error al generar rutina: ${e.message}"
                )
            }
        }
    }
    
    fun resetWizard() {
        _uiState.value = RoutineGeneratorUiState()
    }

    // Simulación de obtención de perfil de usuario
    private fun getUserProfile(): UserProfileData {
        // TODO: Reemplazar con acceso real a ProfileViewModel o repositorio
        return UserProfileData(
            age = 25,
            gender = "male",
            weight = 75f,
            height = 175f,
            experienceLevel = "intermediate",
            preferences = "hipertrofia",
            restrictions = null
        )
    }

    data class UserProfileData(
        val age: Int?,
        val gender: String?,
        val weight: Float?,
        val height: Float?,
        val experienceLevel: String?,
        val preferences: String?,
        val restrictions: String?
    )
}

data class RoutineGeneratorUiState(
    val currentStep: Int = 0,
    val selectedGoal: FitnessGoal? = null,
    val selectedLevel: FitnessLevel? = null,
    val daysPerWeek: Int = 4,
    val sessionDuration: Int = 60,
    val selectedEquipment: AvailableEquipment? = null,
    val consecutiveDays: Boolean = false,
    val physicalLimitations: List<PhysicalLimitation> = emptyList(),
    val isGenerating: Boolean = false,
    val isGenerated: Boolean = false,
    val generatedCount: Int = 0,
    val error: String? = null
)
