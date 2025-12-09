package com.spatiallm3d.data.remote.mapper

import com.spatiallm3d.data.remote.dto.*
import com.spatiallm3d.domain.model.*

/**
 * Mapper for converting between data transfer objects and domain models.
 *
 * Follows the separation of concerns principle by keeping DTOs and domain models independent.
 */
object SceneMapper {

    /**
     * Converts AnalysisResponseDto to domain AnalysisResult.
     */
    fun AnalysisResponseDto.toDomain(): AnalysisResult {
        return AnalysisResult(
            scene = scene.toDomain(),
            inferenceTime = inferenceTime,
            modelVersion = modelVersion,
            pointCount = pointCount
        )
    }

    /**
     * Converts SceneStructureDto to domain SceneStructure.
     */
    fun SceneStructureDto.toDomain(): SceneStructure {
        return SceneStructure(
            walls = walls.map { it.toDomain() },
            doors = doors.map { it.toDomain() },
            windows = windows.map { it.toDomain() },
            objects = objects.map { it.toDomain() }
        )
    }

    /**
     * Converts WallDto to domain Wall.
     */
    fun WallDto.toDomain(): Wall {
        return Wall(
            id = id,
            startPoint = Point3D(startX, startY, startZ),
            endPoint = Point3D(endX, endY, endZ),
            height = height
        )
    }

    /**
     * Converts DoorDto to domain Door.
     */
    fun DoorDto.toDomain(): Door {
        return Door(
            id = id,
            wallId = wallId,
            position = Point3D(positionX, positionY, positionZ),
            width = width,
            height = height
        )
    }

    /**
     * Converts WindowDto to domain Window.
     */
    fun WindowDto.toDomain(): Window {
        return Window(
            id = id,
            wallId = wallId,
            position = Point3D(positionX, positionY, positionZ),
            width = width,
            height = height
        )
    }

    /**
     * Converts BoundingBoxDto to domain BoundingBox.
     */
    fun BoundingBoxDto.toDomain(): BoundingBox {
        return BoundingBox(
            id = id,
            objectClass = objectClass,
            position = Point3D(positionX, positionY, positionZ),
            rotation = rotationZ,
            scale = Point3D(scaleX, scaleY, scaleZ),
            confidence = confidence
        )
    }
}
