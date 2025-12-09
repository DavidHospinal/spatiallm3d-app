package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a detected object in 3D space with bounding box parameters.
 *
 * Objects are detected by the SpatialLM model and classified into categories
 * (e.g., sofa, table, bed, chair).
 *
 * @property id Unique identifier for the bounding box (e.g., "bbox_0")
 * @property objectClass Classification label (e.g., "sofa", "table")
 * @property position Center position of the object
 * @property rotation Rotation around Z-axis in radians
 * @property scale Dimensions of the bounding box (x=width, y=depth, z=height)
 * @property confidence ML model confidence score (0.0 to 1.0)
 */
@Serializable
data class BoundingBox(
    val id: String,
    val objectClass: String,
    val position: Point3D,
    val rotation: Float,
    val scale: Point3D,
    val confidence: Float
) {
    /**
     * Calculates the volume of the bounding box in cubic meters.
     */
    val volume: Float
        get() = scale.x * scale.y * scale.z

    /**
     * Checks if the detection confidence is above threshold (85%).
     */
    val isHighConfidence: Boolean
        get() = confidence >= 0.85f

    /**
     * Returns a human-readable label with confidence percentage.
     */
    val displayLabel: String
        get() = "$objectClass (${(confidence * 100).toInt()}%)"
}
