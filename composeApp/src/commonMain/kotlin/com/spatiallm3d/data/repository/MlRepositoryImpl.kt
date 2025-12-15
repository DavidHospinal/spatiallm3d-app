package com.spatiallm3d.data.repository

import com.spatiallm3d.data.remote.client.SpatialLMClient
import com.spatiallm3d.data.remote.dto.AnalysisRequestDto
import com.spatiallm3d.data.remote.mapper.SceneMapper.toDomain
import com.spatiallm3d.data.source.DemoDataSource
import com.spatiallm3d.domain.model.AnalysisResult
import com.spatiallm3d.domain.model.DemoScene
import com.spatiallm3d.domain.model.PointCloud
import com.spatiallm3d.domain.repository.MlRepository

/**
 * Implementation of MlRepository with hybrid Demo/Backend mode support.
 *
 * Supports two operating modes:
 * - DEMO: Load pre-loaded scenes from local assets (offline)
 * - BACKEND: Call Render API for real-time processing (online)
 *
 * @property client HTTP client for API communication
 * @property demoDataSource Data source for loading demo scenes
 * @property dataMode Operating mode (DEMO or BACKEND)
 */
class MlRepositoryImpl(
    private val client: SpatialLMClient,
    private val demoDataSource: DemoDataSource? = null,
    private val dataMode: DataMode = DataMode.DEMO  // Default: Demo Mode for MVP
) : MlRepository {

    // Cache de escenas demo (cargadas una vez)
    private var demoScenes: List<DemoScene>? = null

    override suspend fun analyzeScene(
        pointCloud: PointCloud,
        detectWalls: Boolean,
        detectDoors: Boolean,
        detectWindows: Boolean,
        detectObjects: Boolean,
        objectCategories: List<String>?
    ): Result<AnalysisResult> {
        return when (dataMode) {
            DataMode.DEMO -> analyzeDemoScene(pointCloud.filename)
            DataMode.BACKEND -> analyzeBackendScene(pointCloud)
        }
    }

    /**
     * Modo DEMO: Cargar desde assets locales
     */
    private suspend fun analyzeDemoScene(filename: String?): Result<AnalysisResult> {
        return try {
            if (demoDataSource == null) {
                println("MlRepository: ⚠️ Demo mode enabled but DemoDataSource not provided")
                println("  Falling back to local mock generation")
                return Result.success(generateMockAnalysisFromPointCloud(PointCloud(emptyList(), PointCloud.SourceType.FILE_UPLOAD)))
            }

            // Cargar escenas si no están en cache
            if (demoScenes == null) {
                demoScenes = demoDataSource.loadDemoScenes()
                println("MlRepository: Loaded ${demoScenes?.size} demo scenes")
            }

            // Buscar escena por filename
            val sceneId = filename?.removeSuffix(".ply")
            val scene = demoScenes?.find { it.id == sceneId }

            if (scene != null) {
                println("MlRepository: Loading demo scene: ${scene.name}")
                val analysis = demoDataSource.loadSceneAnalysis(scene)

                println("MlRepository: ✅ Demo scene loaded")
                println("  - Walls: ${analysis.scene?.walls?.size ?: 0}")
                println("  - Doors: ${analysis.scene?.doors?.size ?: 0}")
                println("  - Windows: ${analysis.scene?.windows?.size ?: 0}")
                println("  - Objects: ${analysis.scene?.objects?.size ?: 0}")

                Result.success(analysis)
            } else {
                println("MlRepository: ⚠️ Scene not found: $sceneId")
                Result.failure(SceneNotFoundException("Scene not found: $sceneId"))
            }

        } catch (e: Exception) {
            println("MlRepository: ❌ Error loading demo scene: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Modo BACKEND: Llamar a Render API (código existente)
     */
    private suspend fun analyzeBackendScene(pointCloud: PointCloud): Result<AnalysisResult> {
        return try {
            // Extract scene ID from filename if available
            val sceneId = pointCloud.filename?.removeSuffix(".ply")

            if (sceneId != null) {
                println("MlRepository: Attempting backend lookup for scene: $sceneId")

                try {
                    // Try to fetch pre-computed results from Render backend
                    val response = client.getPrecomputedResult(sceneId)
                    val analysisResult = response.toDomain()

                    println("MlRepository: ✅ Loaded pre-computed data from backend")
                    println("  - Walls: ${analysisResult.scene?.walls?.size ?: 0}")
                    println("  - Doors: ${analysisResult.scene?.doors?.size ?: 0}")
                    println("  - Windows: ${analysisResult.scene?.windows?.size ?: 0}")
                    println("  - Objects: ${analysisResult.scene?.objects?.size ?: 0}")

                    return Result.success(analysisResult)

                } catch (e: Exception) {
                    println("MlRepository: ⚠️ Backend lookup failed: ${e.message}")
                    println("  Falling back to local mock generation")
                }
            } else {
                println("MlRepository: No scene ID available, using local mock")
            }

            // Fallback: Generate mock from PointCloud
            val analysisResult = generateMockAnalysisFromPointCloud(pointCloud)
            Result.success(analysisResult)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateMockAnalysisFromPointCloud(pointCloud: PointCloud): AnalysisResult {
        val pointCount = pointCloud.points.size
        println("MlRepository: Generating mock analysis for $pointCount points")

        if (pointCloud.points.isEmpty()) {
            println("MlRepository: PointCloud is empty, returning empty analysis")
            return createEmptyAnalysis()
        }

        val minX = pointCloud.points.minOf { it.x }
        val maxX = pointCloud.points.maxOf { it.x }
        val minY = pointCloud.points.minOf { it.y }
        val maxY = pointCloud.points.maxOf { it.y }
        val minZ = pointCloud.points.minOf { it.z }
        val maxZ = pointCloud.points.maxOf { it.z }

        println("MlRepository: PointCloud bounding box - X[$minX, $maxX], Y[$minY, $maxY], Z[$minZ, $maxZ]")

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f
        val centerZ = (minZ + maxZ) / 2f

        val roomWidth = maxX - minX
        val roomDepth = maxY - minY
        val roomHeight = maxZ - minZ

        println("MlRepository: Room dimensions - width=$roomWidth, depth=$roomDepth, height=$roomHeight")
        println("MlRepository: Center point - ($centerX, $centerY, $centerZ)")

        val objects = buildList {
            add(com.spatiallm3d.domain.model.BoundingBox(
                id = "bbox_0",
                objectClass = "sofa",
                position = com.spatiallm3d.domain.model.Point3D(minX + roomWidth * 0.2f, minY + roomDepth * 0.3f, minZ + 0.4f),
                rotation = 0.0f,
                scale = com.spatiallm3d.domain.model.Point3D(0.9f, 1.8f, 0.8f),
                confidence = 0.93f
            ))
            add(com.spatiallm3d.domain.model.BoundingBox(
                id = "bbox_1",
                objectClass = "dining_table",
                position = com.spatiallm3d.domain.model.Point3D(centerX, centerY, minZ + 0.75f),
                rotation = 0.0f,
                scale = com.spatiallm3d.domain.model.Point3D(1.5f, 1.0f, 0.75f),
                confidence = 0.91f
            ))
            add(com.spatiallm3d.domain.model.BoundingBox(
                id = "bbox_2",
                objectClass = "chair",
                position = com.spatiallm3d.domain.model.Point3D(centerX + 0.8f, centerY, minZ + 0.5f),
                rotation = 0.0f,
                scale = com.spatiallm3d.domain.model.Point3D(0.5f, 0.5f, 0.95f),
                confidence = 0.89f
            ))
            add(com.spatiallm3d.domain.model.BoundingBox(
                id = "bbox_3",
                objectClass = "chair",
                position = com.spatiallm3d.domain.model.Point3D(centerX - 0.8f, centerY, minZ + 0.5f),
                rotation = 0.0f,
                scale = com.spatiallm3d.domain.model.Point3D(0.5f, 0.5f, 0.95f),
                confidence = 0.87f
            ))
            add(com.spatiallm3d.domain.model.BoundingBox(
                id = "bbox_4",
                objectClass = "cabinet",
                position = com.spatiallm3d.domain.model.Point3D(maxX - 0.5f, minY + roomDepth * 0.7f, minZ + 0.9f),
                rotation = 0.0f,
                scale = com.spatiallm3d.domain.model.Point3D(0.6f, 1.2f, 1.8f),
                confidence = 0.85f
            ))
        }

        val wallHeight = roomHeight.coerceAtLeast(2.5f)

        val walls = buildList {
            add(com.spatiallm3d.domain.model.Wall(
                id = "wall_0",
                startPoint = com.spatiallm3d.domain.model.Point3D(minX, minY, minZ),
                endPoint = com.spatiallm3d.domain.model.Point3D(maxX, minY, minZ),
                height = wallHeight
            ))
            add(com.spatiallm3d.domain.model.Wall(
                id = "wall_1",
                startPoint = com.spatiallm3d.domain.model.Point3D(maxX, minY, minZ),
                endPoint = com.spatiallm3d.domain.model.Point3D(maxX, maxY, minZ),
                height = wallHeight
            ))
            add(com.spatiallm3d.domain.model.Wall(
                id = "wall_2",
                startPoint = com.spatiallm3d.domain.model.Point3D(maxX, maxY, minZ),
                endPoint = com.spatiallm3d.domain.model.Point3D(minX, maxY, minZ),
                height = wallHeight
            ))
            add(com.spatiallm3d.domain.model.Wall(
                id = "wall_3",
                startPoint = com.spatiallm3d.domain.model.Point3D(minX, maxY, minZ),
                endPoint = com.spatiallm3d.domain.model.Point3D(minX, minY, minZ),
                height = wallHeight
            ))
        }

        println("MlRepository: Generated ${walls.size} walls with height=$wallHeight")
        walls.forEachIndexed { i, wall ->
            println("  Wall $i: start=${wall.startPoint}, end=${wall.endPoint}")
        }

        val doors = buildList {
            add(com.spatiallm3d.domain.model.Door(
                id = "door_0",
                wallId = "wall_0",
                position = com.spatiallm3d.domain.model.Point3D(centerX, minY, minZ + 1.0f),
                width = 0.85f,
                height = 2.1f
            ))
            if (roomWidth > 4.0f) {
                add(com.spatiallm3d.domain.model.Door(
                    id = "door_1",
                    wallId = "wall_2",
                    position = com.spatiallm3d.domain.model.Point3D(centerX + roomWidth * 0.2f, maxY, minZ + 1.0f),
                    width = 0.9f,
                    height = 2.1f
                ))
            }
        }

        val windows = buildList {
            add(com.spatiallm3d.domain.model.Window(
                id = "window_0",
                wallId = "wall_1",
                position = com.spatiallm3d.domain.model.Point3D(maxX, centerY, minZ + 1.5f),
                width = 1.2f,
                height = 1.4f
            ))
            if (roomDepth > 3.0f) {
                add(com.spatiallm3d.domain.model.Window(
                    id = "window_1",
                    wallId = "wall_3",
                    position = com.spatiallm3d.domain.model.Point3D(minX, centerY + roomDepth * 0.2f, minZ + 1.5f),
                    width = 1.0f,
                    height = 1.2f
                ))
            }
        }

        val scene = com.spatiallm3d.domain.model.SceneStructure(
            walls = walls,
            doors = doors,
            windows = windows,
            objects = objects
        )

        println("MlRepository: Generated scene with ${walls.size} walls, ${doors.size} doors, ${windows.size} windows, ${objects.size} objects")

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

    private fun createEmptyAnalysis(): AnalysisResult {
        val emptyScene = com.spatiallm3d.domain.model.SceneStructure(
            walls = emptyList(),
            doors = emptyList(),
            windows = emptyList(),
            objects = emptyList()
        )

        return AnalysisResult(
            scene = emptyScene,
            inferenceTime = 0.0f,
            modelVersion = "SpatialLM-Local-Mock",
            pointCount = 0
        )
    }

    /**
     * Obtener lista de escenas demo disponibles
     */
    suspend fun getDemoScenes(): List<DemoScene> {
        if (demoScenes == null && demoDataSource != null) {
            demoScenes = demoDataSource.loadDemoScenes()
        }
        return demoScenes ?: emptyList()
    }
}

/**
 * Enum para controlar el modo de datos del repositorio
 */
enum class DataMode {
    DEMO,       // Cargar desde assets locales (offline)
    BACKEND     // Llamar a Render API (online)
}

/**
 * Excepción cuando no se encuentra una escena demo
 */
class SceneNotFoundException(message: String) : Exception(message)

/**
 * Excepción para escenas inválidas
 */
class InvalidSceneException(message: String) : Exception(message)
