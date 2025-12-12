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
            val analysisResult = generateMockAnalysisFromPointCloud(pointCloud)
            Result.success(analysisResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateMockAnalysisFromPointCloud(pointCloud: PointCloud): AnalysisResult {
        val pointCount = pointCloud.points.size

        val objects = listOf(
            com.spatiallm3d.domain.model.BoundingBox(
                id = "bbox_0",
                objectClass = "chair",
                position = com.spatiallm3d.domain.model.Point3D(1.2f, 1.8f, 0.5f),
                rotation = 0.0f,
                scale = com.spatiallm3d.domain.model.Point3D(0.5f, 0.5f, 1.0f),
                confidence = 0.92f
            ),
            com.spatiallm3d.domain.model.BoundingBox(
                id = "bbox_1",
                objectClass = "table",
                position = com.spatiallm3d.domain.model.Point3D(2.5f, 2.5f, 0.75f),
                rotation = 0.0f,
                scale = com.spatiallm3d.domain.model.Point3D(1.0f, 1.5f, 0.8f),
                confidence = 0.88f
            )
        )

        val walls = listOf(
            com.spatiallm3d.domain.model.Wall(
                id = "wall_0",
                startPoint = com.spatiallm3d.domain.model.Point3D(0.0f, 0.0f, 0.0f),
                endPoint = com.spatiallm3d.domain.model.Point3D(5.0f, 0.0f, 0.0f),
                height = 2.7f
            ),
            com.spatiallm3d.domain.model.Wall(
                id = "wall_1",
                startPoint = com.spatiallm3d.domain.model.Point3D(0.0f, 0.0f, 0.0f),
                endPoint = com.spatiallm3d.domain.model.Point3D(0.0f, 4.0f, 0.0f),
                height = 2.7f
            )
        )

        val doors = listOf(
            com.spatiallm3d.domain.model.Door(
                id = "door_0",
                wallId = "wall_0",
                position = com.spatiallm3d.domain.model.Point3D(2.5f, 0.0f, 1.0f),
                width = 0.9f,
                height = 2.1f
            )
        )

        val windows = listOf(
            com.spatiallm3d.domain.model.Window(
                id = "window_0",
                wallId = "wall_1",
                position = com.spatiallm3d.domain.model.Point3D(0.0f, 2.0f, 1.5f),
                width = 1.2f,
                height = 1.5f
            )
        )

        val scene = com.spatiallm3d.domain.model.SceneStructure(
            walls = walls,
            doors = doors,
            windows = windows,
            objects = objects
        )

        return AnalysisResult(
            scene = scene,
            inferenceTime = 0.15f,
            modelVersion = "SpatialLM-Local-Mock",
            pointCount = pointCount
        )
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
