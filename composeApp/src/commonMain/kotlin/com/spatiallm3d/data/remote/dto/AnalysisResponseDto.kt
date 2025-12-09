package com.spatiallm3d.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResponseDto(
    @SerialName("scene")
    val scene: SceneStructureDto,

    @SerialName("inference_time")
    val inferenceTime: Float,

    @SerialName("model_version")
    val modelVersion: String,

    @SerialName("point_count")
    val pointCount: Int
)

@Serializable
data class SceneStructureDto(
    @SerialName("walls")
    val walls: List<WallDto>,

    @SerialName("doors")
    val doors: List<DoorDto>,

    @SerialName("windows")
    val windows: List<WindowDto>,

    @SerialName("objects")
    val objects: List<BoundingBoxDto>
)

@Serializable
data class WallDto(
    @SerialName("id")
    val id: String,

    @SerialName("start_x")
    val startX: Float,

    @SerialName("start_y")
    val startY: Float,

    @SerialName("start_z")
    val startZ: Float,

    @SerialName("end_x")
    val endX: Float,

    @SerialName("end_y")
    val endY: Float,

    @SerialName("end_z")
    val endZ: Float,

    @SerialName("height")
    val height: Float
)

@Serializable
data class DoorDto(
    @SerialName("id")
    val id: String,

    @SerialName("wall_id")
    val wallId: String,

    @SerialName("position_x")
    val positionX: Float,

    @SerialName("position_y")
    val positionY: Float,

    @SerialName("position_z")
    val positionZ: Float,

    @SerialName("width")
    val width: Float,

    @SerialName("height")
    val height: Float
)

@Serializable
data class WindowDto(
    @SerialName("id")
    val id: String,

    @SerialName("wall_id")
    val wallId: String,

    @SerialName("position_x")
    val positionX: Float,

    @SerialName("position_y")
    val positionY: Float,

    @SerialName("position_z")
    val positionZ: Float,

    @SerialName("width")
    val width: Float,

    @SerialName("height")
    val height: Float
)

@Serializable
data class BoundingBoxDto(
    @SerialName("id")
    val id: String,

    @SerialName("object_class")
    val objectClass: String,

    @SerialName("position_x")
    val positionX: Float,

    @SerialName("position_y")
    val positionY: Float,

    @SerialName("position_z")
    val positionZ: Float,

    @SerialName("rotation_z")
    val rotationZ: Float,

    @SerialName("scale_x")
    val scaleX: Float,

    @SerialName("scale_y")
    val scaleY: Float,

    @SerialName("scale_z")
    val scaleZ: Float,

    @SerialName("confidence")
    val confidence: Float
)
