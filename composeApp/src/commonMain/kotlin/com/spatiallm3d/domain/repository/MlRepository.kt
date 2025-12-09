package com.spatiallm3d.domain.repository

import com.spatiallm3d.domain.model.AnalysisResult
import com.spatiallm3d.domain.model.PointCloud

/**
 * Repository interface for machine learning operations.
 *
 * Defines the contract for analyzing 3D scenes using the SpatialLM backend.
 * Implementations should handle network communication and data mapping.
 */
interface MlRepository {

    /**
     * Analyzes a 3D scene from a point cloud and returns detected structures.
     *
     * @param pointCloud Input point cloud data
     * @param detectWalls Enable wall detection
     * @param detectDoors Enable door detection
     * @param detectWindows Enable window detection
     * @param detectObjects Enable object detection
     * @param objectCategories Optional list of object categories to detect
     * @return AnalysisResult with detected scene structure
     * @throws Exception if network request fails or parsing errors occur
     */
    suspend fun analyzeScene(
        pointCloud: PointCloud,
        detectWalls: Boolean = true,
        detectDoors: Boolean = true,
        detectWindows: Boolean = true,
        detectObjects: Boolean = true,
        objectCategories: List<String>? = null
    ): Result<AnalysisResult>

    /**
     * Analyzes a 3D scene from a remote URL pointing to a .ply file.
     *
     * @param pointCloudUrl URL to the .ply file
     * @param detectWalls Enable wall detection
     * @param detectDoors Enable door detection
     * @param detectWindows Enable window detection
     * @param detectObjects Enable object detection
     * @param objectCategories Optional list of object categories to detect
     * @return AnalysisResult with detected scene structure
     * @throws Exception if network request fails or parsing errors occur
     */
    suspend fun analyzeSceneFromUrl(
        pointCloudUrl: String,
        detectWalls: Boolean = true,
        detectDoors: Boolean = true,
        detectWindows: Boolean = true,
        detectObjects: Boolean = true,
        objectCategories: List<String>? = null
    ): Result<AnalysisResult>
}
