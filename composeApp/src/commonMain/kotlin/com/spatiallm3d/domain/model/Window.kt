package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a window detected in a 3D scene.
 *
 * Windows are associated with a specific wall and have position, width, and height.
 *
 * @property id Unique identifier for the window (e.g., "window_0")
 * @property wallId Identifier of the wall containing this window
 * @property position Position of the window center
 * @property width Window width in meters
 * @property height Window height in meters
 */
@Serializable
data class Window(
    val id: String,
    val wallId: String,
    val position: Point3D,
    val width: Float,
    val height: Float
) {
    /**
     * Calculates the area of the window in square meters.
     */
    val area: Float
        get() = width * height

    /**
     * Returns the estimated natural light contribution (0.0 to 1.0).
     * Larger windows provide more natural light.
     */
    val lightContribution: Float
        get() = (area / 3f).coerceIn(0f, 1f)
}
