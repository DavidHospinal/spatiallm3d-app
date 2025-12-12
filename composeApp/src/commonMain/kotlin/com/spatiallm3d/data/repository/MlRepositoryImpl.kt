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
            com.spatiallm3d.domain.model.DetectedObject(
                id = "chair_01",
                category = "Chair",
                confidence = 0.92f,
                position = com.spatiallm3d.domain.model.Point3D(1.2f, 0.5f, 1.8f),
                boundingBox = com.spatiallm3d.domain.model.BoundingBox(
                    minX = 1.0f, maxX = 1.4f,
                    minY = 0.0f, maxY = 1.0f,
                    minZ = 1.5f, maxZ = 2.1f
                )
            ),
            com.spatiallm3d.domain.model.DetectedObject(
                id = "table_01",
                category = "Table",
                confidence = 0.88f,
                position = com.spatiallm3d.domain.model.Point3D(2.5f, 0.75f, 2.5f),
                boundingBox = com.spatiallm3d.domain.model.BoundingBox(
                    minX = 2.0f, maxX = 3.0f,
                    minY = 0.0f, maxY = 0.8f,
                    minZ = 2.0f, maxZ = 3.0f
                )
            )
        )

        val walls = listOf(
            com.spatiallm3d.domain.model.Wall(
                id = "wall_north",
                start = com.spatiallm3d.domain.model.Point3D(0.0f, 0.0f, 0.0f),
                end = com.spatiallm3d.domain.model.Point3D(5.0f, 0.0f, 0.0f),
                height = 2.7f,
                thickness = 0.15f
            ),
            com.spatiallm3d.domain.model.Wall(
                id = "wall_south",
                start = com.spatiallm3d.domain.model.Point3D(0.0f, 0.0f, 4.0f),
                end = com.spatiallm3d.domain.model.Point3D(5.0f, 0.0f, 4.0f),
                height = 2.7f,
                thickness = 0.15f
            )
        )

        val doors = listOf(
            com.spatiallm3d.domain.model.Door(
                id = "door_main",
                position = com.spatiallm3d.domain.model.Point3D(2.5f, 0.0f, 0.0f),
                width = 0.9f,
                height = 2.1f,
                isAccessible = true,
                clearanceWidth = 0.9f
            )
        )

        val windows = listOf(
            com.spatiallm3d.domain.model.Window(
                id = "window_01",
                position = com.spatiallm3d.domain.model.Point3D(1.0f, 1.2f, 0.0f),
                width = 1.2f,
                height = 1.5f
            )
        )

        return AnalysisResult(
            sceneId = "local_${pointCount}_points",
            objects = objects,
            walls = walls,
            doors = doors,
            windows = windows,
            accessibilityScore = 75,
            safetyScore = 82,
            timestamp = 0L,
            processingTimeMs = 150L
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
