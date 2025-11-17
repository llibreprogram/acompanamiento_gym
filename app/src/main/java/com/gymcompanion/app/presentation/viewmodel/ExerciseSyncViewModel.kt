package com.gymcompanion.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcompanion.app.domain.usecase.ExerciseSyncManager
import com.gymcompanion.app.domain.usecase.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar la sincronización de ejercicios
 */
@HiltViewModel
class ExerciseSyncViewModel @Inject constructor(
    private val syncManager: ExerciseSyncManager
) : ViewModel() {
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    init {
        // Observar el estado general de sincronización
        viewModelScope.launch {
            syncManager.observeGeneralSyncState().collect { state ->
                _syncState.value = state
            }
        }
    }
    
    /**
     * Inicia sincronización manual
     */
    fun syncExercises() {
        viewModelScope.launch {
            syncManager.syncNow().collect { state ->
                _syncState.value = state
            }
        }
    }
    
    /**
     * Cancela la sincronización en curso
     */
    fun cancelSync() {
        syncManager.cancelAllSync()
        _syncState.value = SyncState.Cancelled
    }
    
    /**
     * Reinicia el estado a Idle
     */
    fun resetState() {
        _syncState.value = SyncState.Idle
    }
}

/**
 * UI State para la pantalla de sincronización
 */
data class ExerciseSyncUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val message: String = "",
    val progress: Int = 0,
    val total: Int = 0
) {
    companion object {
        fun fromSyncState(state: SyncState): ExerciseSyncUiState {
            return when (state) {
                is SyncState.Idle -> ExerciseSyncUiState()
                is SyncState.Queued -> ExerciseSyncUiState(
                    isLoading = true,
                    message = "En cola..."
                )
                is SyncState.Starting -> ExerciseSyncUiState(
                    isLoading = true,
                    message = state.message
                )
                is SyncState.InProgress -> ExerciseSyncUiState(
                    isLoading = true,
                    message = state.message,
                    progress = state.exercisesSynced
                )
                is SyncState.Success -> ExerciseSyncUiState(
                    isSuccess = true,
                    message = state.message,
                    total = state.totalExercises
                )
                is SyncState.Error -> ExerciseSyncUiState(
                    isError = true,
                    message = state.message
                )
                is SyncState.Cancelled -> ExerciseSyncUiState(
                    message = "Sincronización cancelada"
                )
            }
        }
    }
}
