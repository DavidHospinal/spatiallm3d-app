package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a door detected in a 3D scene.
 *
 * Doors are associated with a specific wall and have position, width, and height.
 *
 * @property id Unique identifier for the door (e.g., "door_0")
 * @property wallId Identifier of the wall containing this door
 * @property position Position of the door center
 * @property width Door width in meters (typically 0.8-1.0m)
 * @property height Door height in meters (typically 2.0-2.1m)
 */
@Serializable
data class Door(
    val id: String,
    val wallId: String,
    val position: Point3D,
    val width: Float,
    val height: Float
) {
    /**
     * Calculates the area of the door in square meters.
     */
    val area: Float
        get() = width * height

    /**
     * Checks if door dimensions are within standard residential ranges.
     */
    val isStandardSize: Boolean
        get() = width in 0.7f..1.2f && height in 1.9f..2.3f
}
