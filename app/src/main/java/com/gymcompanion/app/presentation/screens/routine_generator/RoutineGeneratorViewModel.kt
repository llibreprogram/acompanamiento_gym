package com.gymcompanion.app.presentation.screens.routine_generator

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
    
    fun generateRoutine() {
        val state = _uiState.value
        
        if (state.selectedGoal == null || state.selectedLevel == null || state.selectedEquipment == null) {
            _uiState.value = state.copy(error = "Por favor completa todos los campos")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isGenerating = true, error = null)
            
            try {
                val request = RoutineGenerationRequest(
                    goal = state.selectedGoal,
                    level = state.selectedLevel,
                    daysPerWeek = state.daysPerWeek,
                    sessionDuration = state.sessionDuration,
                    equipment = state.selectedEquipment
                )
                
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
}

data class RoutineGeneratorUiState(
    val currentStep: Int = 0,
    val selectedGoal: FitnessGoal? = null,
    val selectedLevel: FitnessLevel? = null,
    val daysPerWeek: Int = 4,
    val sessionDuration: Int = 60,
    val selectedEquipment: AvailableEquipment? = null,
    val isGenerating: Boolean = false,
    val isGenerated: Boolean = false,
    val generatedCount: Int = 0,
    val error: String? = null
)
