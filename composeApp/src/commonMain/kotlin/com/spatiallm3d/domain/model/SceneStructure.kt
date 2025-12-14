package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the complete structural analysis of a 3D scene.
 *
 * Contains all detected architectural elements (walls, doors, windows)
 * and objects (furniture, equipment) in the scene.
 *
 * @property walls List of detected walls
 * @property doors List of detected doors
 * @property windows List of detected windows
 * @property objects List of detected objects with bounding boxes
 */
@Serializable
data class SceneStructure(
    val walls: List<Wall>,
    val doors: List<Door>,
    val windows: List<Window>,
    val objects: List<BoundingBox>
) {
    /**
     * Total number of detected elements.
     */
    val totalElements: Int
        get() = walls.size + doors.size + windows.size + objects.size

    /**
     * Checks if the scene has any detected architectural elements.
     */
    val hasArchitecture: Boolean
        get() = walls.isNotEmpty() || doors.isNotEmpty() || windows.isNotEmpty()

    /**
     * Checks if the scene has any detected objects.
     */
    val hasObjects: Boolean
        get() = objects.isNotEmpty()

    /**
     * Returns objects grouped by their classification.
     */
    fun objectsByClass(): Map<String, List<BoundingBox>> {
        return objects.groupBy { it.objectClass }
    }

    /**
     * Returns high-confidence objects only (confidence >= 85%).
     */
    fun highConfidenceObjects(): List<BoundingBox> {
        return objects.filter { it.isHighConfidence }
    }
}
