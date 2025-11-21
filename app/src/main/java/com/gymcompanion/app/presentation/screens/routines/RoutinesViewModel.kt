package com.gymcompanion.app.presentation.screens.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.data.local.entity.RoutineEntity
import com.gymcompanion.app.data.local.entity.RoutineExerciseEntity
import com.gymcompanion.app.data.local.entity.RoutineWithExercises
import com.gymcompanion.app.domain.repository.RoutineRepository
import com.gymcompanion.app.domain.repository.ExerciseRepository
import com.gymcompanion.app.domain.usecase.RoutineGeneratorUseCase
import com.gymcompanion.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la gestión de rutinas
 */
@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineGenerator: RoutineGeneratorUseCase
) : ViewModel() {
    
    // Lista de todas las rutinas
    val allRoutines: StateFlow<List<RoutineWithExercises>> = routineRepository.getAllRoutines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Rutinas activas
    val activeRoutines: StateFlow<List<RoutineWithExercises>> = routineRepository.getActiveRoutines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Estado de UI
    private val _uiState = MutableStateFlow<RoutinesUiState>(RoutinesUiState.Success)
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()
    
    // Rutina seleccionada para edición
    private val _selectedRoutine = MutableStateFlow<RoutineWithExercises?>(null)
    val selectedRoutine: StateFlow<RoutineWithExercises?> = _selectedRoutine.asStateFlow()
    
    /**
     * Crea una nueva rutina con ejercicios generados automáticamente
     */
    fun createRoutine(
        name: String,
        description: String,
        daysOfWeek: List<String>,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                // Crear una solicitud de generación de rutina basada en los días seleccionados
                val request = RoutineGenerationRequest(
                    daysPerWeek = daysOfWeek.size,
                    sessionDuration = 60,
                    goal = FitnessGoal.GENERAL_FITNESS,
                    level = FitnessLevel.INTERMEDIATE,
                    equipment = AvailableEquipment.FULL_GYM,
                    consecutiveDays = false,
                    physicalLimitations = emptyList(),
                    age = null,
                    gender = null,
                    weight = null,
                    height = null,
                    experienceLevel = null,
                    preferences = null,
                    restrictions = null,
                    specificDays = daysOfWeek
                )
                
                // Generar rutinas con ejercicios automáticamente
                // El generador ya crea las rutinas en la base de datos con ejercicios
                routineGenerator.generateRoutine(request, userId = 1L)
                
                _uiState.value = RoutinesUiState.Success
            } catch (e: Exception) {
                _uiState.value = RoutinesUiState.Error(e.message ?: "Error al crear rutina")
            }
        }
    }
    
    /**
     * Actualiza una rutina existente
     */
    fun updateRoutine(
        routineId: Long,
        name: String,
        description: String,
        daysOfWeek: List<String>,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            try {
                val routine = RoutineEntity(
                    id = routineId,
                    userId = 1L, // TODO: Get actual user ID
                    name = name,
                    description = description,
                    daysOfWeek = daysOfWeek.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]"),
                    duration = 60, // TODO: Calculate from exercises
                    focusArea = "general_fitness",
                    difficulty = "intermediate",
                    isActive = isActive,
                    createdAt = System.currentTimeMillis()
                )
                routineRepository.updateRoutine(routine)
                _uiState.value = RoutinesUiState.Success
            } catch (e: Exception) {
                _uiState.value = RoutinesUiState.Error(e.message ?: "Error al actualizar rutina")
            }
        }
    }
    
    /**
     * Elimina una rutina
     */
    fun deleteRoutine(routine: RoutineWithExercises) {
        viewModelScope.launch {
            try {
                routineRepository.deleteRoutine(routine.routine)
                _uiState.value = RoutinesUiState.Success
            } catch (e: Exception) {
                _uiState.value = RoutinesUiState.Error(e.message ?: "Error al eliminar rutina")
            }
        }
    }
    
    /**
     * Agrega un ejercicio a una rutina
     */
    fun addExerciseToRoutine(
        routineId: Long,
        exerciseId: Long,
        sets: Int,
        repsMin: Int?,
        repsMax: Int?,
        restSeconds: Int,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                // Obtener el orden actual más alto
                val exercises = routineRepository.getRoutineExercises(routineId).first()
                val maxOrder = exercises.maxOfOrNull { it.orderIndex } ?: -1
                
                val routineExercise = RoutineExerciseEntity(
                    routineId = routineId,
                    exerciseId = exerciseId,
                    orderIndex = maxOrder + 1,
                    plannedSets = sets,
                    plannedReps = if (repsMin == repsMax) "$repsMin" else "$repsMin-$repsMax",
                    restTimeSeconds = restSeconds,
                    notes = notes
                )
                routineRepository.addExerciseToRoutine(routineExercise)
            } catch (e: Exception) {
                _uiState.value = RoutinesUiState.Error(e.message ?: "Error al agregar ejercicio")
            }
        }
    }
    
    /**
     * Elimina un ejercicio de una rutina
     */
    fun removeExerciseFromRoutine(routineExercise: RoutineExerciseEntity) {
        viewModelScope.launch {
            try {
                routineRepository.removeExerciseFromRoutine(routineExercise)
            } catch (e: Exception) {
                _uiState.value = RoutinesUiState.Error(e.message ?: "Error al eliminar ejercicio")
            }
        }
    }
    
    /**
     * Carga una rutina para edición
     */
    fun loadRoutineForEdit(routineId: Long) {
        viewModelScope.launch {
            routineRepository.getRoutineById(routineId).collect { routine ->
                _selectedRoutine.value = routine
            }
        }
    }
    
    /**
     * Obtiene las rutinas para un día específico
     */
    fun getRoutinesForDay(dayOfWeek: String): Flow<List<RoutineWithExercises>> {
        return routineRepository.getRoutinesForDay(dayOfWeek)
    }
    
    /**
     * Obtiene el día de la semana actual en español
     */
    fun getCurrentDayOfWeek(): String {
        val calendar = java.util.Calendar.getInstance()
        return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Lunes"
            java.util.Calendar.TUESDAY -> "Martes"
            java.util.Calendar.WEDNESDAY -> "Miércoles"
            java.util.Calendar.THURSDAY -> "Jueves"
            java.util.Calendar.FRIDAY -> "Viernes"
            java.util.Calendar.SATURDAY -> "Sábado"
            java.util.Calendar.SUNDAY -> "Domingo"
            else -> ""
        }
    }
}

/**
 * Estados de UI para rutinas
 */
sealed class RoutinesUiState {
    object Loading : RoutinesUiState()
    object Success : RoutinesUiState()
    data class Error(val message: String) : RoutinesUiState()
}
