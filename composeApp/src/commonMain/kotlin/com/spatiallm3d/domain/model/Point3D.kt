package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a point in 3D space with coordinates (x, y, z).
 *
 * This is the fundamental building block for point cloud data.
 * Coordinates are in meters following the SpatialLM coordinate system.
 */
@Serializable
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    /**
     * Calculates Euclidean distance to another point.
     */
    fun distanceTo(other: Point3D): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }

    companion object {
        val ORIGIN = Point3D(0f, 0f, 0f)
    }
}
