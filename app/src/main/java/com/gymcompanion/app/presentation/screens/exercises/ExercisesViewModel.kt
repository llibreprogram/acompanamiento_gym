package com.gymcompanion.app.presentation.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.data.local.entity.ExerciseEntity
import com.gymcompanion.app.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de ejercicios
 */
@HiltViewModel
class ExercisesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    
    // Query de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Filtros
    private val _selectedMuscleGroup = MutableStateFlow<String?>(null)
    val selectedMuscleGroup: StateFlow<String?> = _selectedMuscleGroup.asStateFlow()
    
    private val _selectedDifficulty = MutableStateFlow<String?>(null)
    val selectedDifficulty: StateFlow<String?> = _selectedDifficulty.asStateFlow()
    
    // Lista de todos los ejercicios
    private val allExercises: StateFlow<List<ExerciseEntity>> = exerciseRepository.getAllExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Lista filtrada de ejercicios
    @OptIn(FlowPreview::class)
    val filteredExercises: StateFlow<List<ExerciseEntity>> = combine(
        allExercises,
        _searchQuery.debounce(300), // Debounce para búsqueda
        _selectedMuscleGroup,
        _selectedDifficulty
    ) { exercises, query, muscleGroup, difficulty ->
        exercises
            .filter { exercise ->
                // Filtro de búsqueda
                if (query.isNotBlank()) {
                    exercise.name.contains(query, ignoreCase = true) ||
                    exercise.description.contains(query, ignoreCase = true)
                } else true
            }
            .filter { exercise ->
                // Filtro de grupo muscular
                muscleGroup == null || exercise.muscleGroup == muscleGroup
            }
            .filter { exercise ->
                // Filtro de dificultad
                difficulty == null || exercise.difficulty == difficulty
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Grupos musculares disponibles
    val muscleGroups: StateFlow<List<String>> = exerciseRepository.getAllMuscleGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Estado de UI
    private val _uiState = MutableStateFlow<ExercisesUiState>(ExercisesUiState.Loading)
    val uiState: StateFlow<ExercisesUiState> = _uiState.asStateFlow()
    
    init {
        observeExercises()
    }
    
    private fun observeExercises() {
        viewModelScope.launch {
            allExercises.collect { exercises ->
                _uiState.value = if (exercises.isEmpty()) {
                    ExercisesUiState.Empty
                } else {
                    ExercisesUiState.Success
                }
            }
        }
    }
    
    /**
     * Actualiza la query de búsqueda
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Selecciona un grupo muscular para filtrar
     */
    fun onMuscleGroupSelected(muscleGroup: String?) {
        _selectedMuscleGroup.value = muscleGroup
    }
    
    /**
     * Selecciona una dificultad para filtrar
     */
    fun onDifficultySelected(difficulty: String?) {
        _selectedDifficulty.value = difficulty
    }
    
    /**
     * Limpia todos los filtros
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedMuscleGroup.value = null
        _selectedDifficulty.value = null
    }
    
    /**
     * Obtiene el color para un grupo muscular
     */
    fun getMuscleGroupColor(muscleGroup: String): Long {
        return when (muscleGroup) {
            "chest" -> 0xFFE57373
            "back" -> 0xFF64B5F6
            "legs" -> 0xFF81C784
            "shoulders" -> 0xFFFFB74D
            "arms" -> 0xFFBA68C8
            "core" -> 0xFFFFD54F
            else -> 0xFF4DD0E1 // full_body
        }
    }
}

/**
 * Estados de la UI de ejercicios
 */
sealed class ExercisesUiState {
    object Loading : ExercisesUiState()
    object Success : ExercisesUiState()
    object Empty : ExercisesUiState()
    data class Error(val message: String) : ExercisesUiState()
}
