package com.spatiallm3d.presentation.viewmodel

import com.spatiallm3d.domain.model.AnalysisResult
import com.spatiallm3d.domain.repository.MlRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing 3D scene analysis state and operations.
 *
 * Provides a reactive state flow for UI observation and handles business logic
 * for analyzing scenes using the ML repository.
 *
 * @property mlRepository Repository for ML operations
 */
class SceneViewModel(
    private val mlRepository: MlRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _sceneState = MutableStateFlow<SceneState>(SceneState.Idle)
    val sceneState: StateFlow<SceneState> = _sceneState.asStateFlow()

    /**
     * Analyzes a 3D scene from a remote point cloud URL.
     *
     * Updates state to Loading, then Success or Error based on result.
     *
     * @param pointCloudUrl URL to the .ply file
     * @param detectWalls Enable wall detection (default: true)
     * @param detectDoors Enable door detection (default: true)
     * @param detectWindows Enable window detection (default: true)
     * @param detectObjects Enable object detection (default: true)
     * @param objectCategories Optional list of object categories to detect
     */
    fun analyzeScene(
        pointCloudUrl: String,
        detectWalls: Boolean = true,
        detectDoors: Boolean = true,
        detectWindows: Boolean = true,
        detectObjects: Boolean = true,
        objectCategories: List<String>? = null
    ) {
        viewModelScope.launch {
            _sceneState.value = SceneState.Loading

            val result = mlRepository.analyzeSceneFromUrl(
                pointCloudUrl = pointCloudUrl,
                detectWalls = detectWalls,
                detectDoors = detectDoors,
                detectWindows = detectWindows,
                detectObjects = detectObjects,
                objectCategories = objectCategories
            )

            _sceneState.value = result.fold(
                onSuccess = { SceneState.Success(it) },
                onFailure = { SceneState.Error(it.message ?: "Unknown error occurred") }
            )
        }
    }

    /**
     * Resets the scene state to Idle.
     *
     * Call this when navigating away from the analysis screen or starting a new analysis.
     */
    fun resetState() {
        _sceneState.value = SceneState.Idle
    }

    /**
     * Analyzes a locally parsed PointCloud.
     *
     * @param pointCloud Parsed point cloud data
     */
    fun analyzeLocalPointCloud(
        pointCloud: com.spatiallm3d.domain.model.PointCloud
    ) {
        viewModelScope.launch {
            _sceneState.value = SceneState.Loading

            val result = mlRepository.analyzeScene(
                pointCloud = pointCloud
            )

            _sceneState.value = result.fold(
                onSuccess = { SceneState.Success(it) },
                onFailure = { SceneState.Error(it.message ?: "Unknown error occurred") }
            )
        }
    }

    /**
     * Returns the current analysis result if state is Success, null otherwise.
     */
    fun getCurrentResult(): AnalysisResult? {
        return (_sceneState.value as? SceneState.Success)?.result
    }
}
