package com.spatiallm3d.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object for scene analysis requests to the SpatialLM backend API.
 *
 * Maps to the POST /api/v1/analyze endpoint request body.
 *
 * @property pointCloudUrl Optional URL to a hosted .ply file
 * @property detectWalls Enable wall detection
 * @property detectDoors Enable door detection
 * @property detectWindows Enable window detection
 * @property detectObjects Enable object detection
 * @property objectCategories Optional list of object categories to detect
 */
@Serializable
data class AnalysisRequestDto(
    @SerialName("point_cloud_url")
    val pointCloudUrl: String? = null,

    @SerialName("detect_walls")
    val detectWalls: Boolean = true,

    @SerialName("detect_doors")
    val detectDoors: Boolean = true,

    @SerialName("detect_windows")
    val detectWindows: Boolean = true,

    @SerialName("detect_objects")
    val detectObjects: Boolean = true,

    @SerialName("object_categories")
    val objectCategories: List<String>? = null
)
