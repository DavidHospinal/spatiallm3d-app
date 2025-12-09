package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a wall detected in a 3D scene.
 *
 * Walls are defined by a start point, end point, and height.
 * The coordinate system follows SpatialLM conventions (X-right, Y-forward, Z-up).
 *
 * @property id Unique identifier for the wall (e.g., "wall_0")
 * @property startPoint Starting point of the wall segment
 * @property endPoint Ending point of the wall segment
 * @property height Wall height in meters
 */
@Serializable
data class Wall(
    val id: String,
    val startPoint: Point3D,
    val endPoint: Point3D,
    val height: Float
) {
    /**
     * Calculates the length of the wall in meters.
     */
    val length: Float
        get() = startPoint.distanceTo(endPoint)

    /**
     * Calculates the surface area of the wall in square meters.
     */
    val area: Float
        get() = length * height

    /**
     * Returns the center point of the wall.
     */
    val center: Point3D
        get() = Point3D(
            x = (startPoint.x + endPoint.x) / 2f,
            y = (startPoint.y + endPoint.y) / 2f,
            z = (startPoint.z + endPoint.z) / 2f
        )
}
