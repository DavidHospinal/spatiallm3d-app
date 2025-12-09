package com.spatiallm3d.presentation.viewmodel

import com.spatiallm3d.domain.model.AnalysisResult

/**
 * Represents the state of a 3D scene analysis operation.
 *
 * This sealed interface ensures type-safe state management in the ViewModel.
 * UI components can react to state changes using when expressions.
 */
sealed interface SceneState {
    /**
     * Initial state before any analysis has been requested.
     */
    data object Idle : SceneState

    /**
     * Analysis is in progress.
     * UI should show loading indicators during this state.
     */
    data object Loading : SceneState

    /**
     * Analysis completed successfully.
     *
     * @property result The complete analysis result with detected scene structure
     */
    data class Success(val result: AnalysisResult) : SceneState

    /**
     * Analysis failed with an error.
     *
     * @property message Human-readable error message for display
     */
    data class Error(val message: String) : SceneState
}

/**
 * Extension functions for SceneState for convenience.
 */
val SceneState.isLoading: Boolean
    get() = this is SceneState.Loading

val SceneState.isSuccess: Boolean
    get() = this is SceneState.Success

val SceneState.isError: Boolean
    get() = this is SceneState.Error

val SceneState.isIdle: Boolean
    get() = this is SceneState.Idle
