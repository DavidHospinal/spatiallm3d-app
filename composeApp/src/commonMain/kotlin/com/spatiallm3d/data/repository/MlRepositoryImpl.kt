package com.spatiallm3d.data.repository

import com.spatiallm3d.data.remote.client.SpatialLMClient
import com.spatiallm3d.data.remote.dto.AnalysisRequestDto
import com.spatiallm3d.data.remote.mapper.SceneMapper.toDomain
import com.spatiallm3d.domain.model.AnalysisResult
import com.spatiallm3d.domain.model.PointCloud
import com.spatiallm3d.domain.repository.MlRepository

/**
 * Implementation of MlRepository using the SpatialLM backend API.
 *
 * Handles network communication, error handling, and data transformation
 * between DTOs and domain models.
 *
 * @property client HTTP client for API communication
 */
class MlRepositoryImpl(
    private val client: SpatialLMClient
) : MlRepository {

    override suspend fun analyzeScene(
        pointCloud: PointCloud,
        detectWalls: Boolean,
        detectDoors: Boolean,
        detectWindows: Boolean,
        detectObjects: Boolean,
        objectCategories: List<String>?
    ): Result<AnalysisResult> {
        return try {
            Result.failure(
                UnsupportedOperationException(
                    "Direct point cloud upload not yet implemented. Use analyzeSceneFromUrl instead."
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun analyzeSceneFromUrl(
        pointCloudUrl: String,
        detectWalls: Boolean,
        detectDoors: Boolean,
        detectWindows: Boolean,
        detectObjects: Boolean,
        objectCategories: List<String>?
    ): Result<AnalysisResult> {
        return try {
            val request = AnalysisRequestDto(
                pointCloudUrl = pointCloudUrl,
                detectWalls = detectWalls,
                detectDoors = detectDoors,
                detectWindows = detectWindows,
                detectObjects = detectObjects,
                objectCategories = objectCategories
            )

            val response = client.analyzeScene(request)
            val analysisResult = response.toDomain()

            Result.success(analysisResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
