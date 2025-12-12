package com.spatiallm3d.presentation.visualization

import androidx.compose.ui.geometry.Offset
import com.spatiallm3d.domain.model.Point3D
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Isometric projection utilities for 3D to 2D conversion.
 *
 * Uses standard isometric angles (30 degrees) for clean visualization.
 */
object IsometricProjection {

    private const val ISO_ANGLE = PI / 6.0 // 30 degrees
    private val COS_ANGLE = cos(ISO_ANGLE).toFloat()
    private val SIN_ANGLE = sin(ISO_ANGLE).toFloat()

    /**
     * Projects a 3D point to 2D isometric coordinates.
     *
     * @param point 3D point to project
     * @param scale Scaling factor for display
     * @param offsetX Horizontal offset for centering
     * @param offsetY Vertical offset for centering
     * @return 2D offset for rendering
     */
    fun project(
        point: Point3D,
        scale: Float = 10f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ): Offset {
        // Isometric projection formula
        val x = (point.x - point.z) * COS_ANGLE * scale
        val y = (point.x + point.z) * SIN_ANGLE * scale - point.y * scale

        return Offset(
            x = x + offsetX,
            y = y + offsetY
        )
    }

    /**
     * Projects multiple 3D points to 2D.
     *
     * @param points List of 3D points
     * @param scale Scaling factor
     * @param offsetX Horizontal offset
     * @param offsetY Vertical offset
     * @return List of 2D offsets
     */
    fun projectAll(
        points: List<Point3D>,
        scale: Float = 10f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ): List<Offset> {
        return points.map { project(it, scale, offsetX, offsetY) }
    }

    /**
     * Calculates optimal scale and offset for a set of points to fit in canvas.
     *
     * @param points 3D points to fit
     * @param canvasWidth Canvas width in pixels
     * @param canvasHeight Canvas height in pixels
     * @param padding Padding around edges
     * @return Pair of (scale, centerOffset)
     */
    fun calculateViewport(
        points: List<Point3D>,
        canvasWidth: Float,
        canvasHeight: Float,
        padding: Float = 50f
    ): Pair<Float, Offset> {
        if (points.isEmpty()) {
            return Pair(1f, Offset(canvasWidth / 2, canvasHeight / 2))
        }

        // Find bounding box in 3D
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val minZ = points.minOf { it.z }
        val maxZ = points.maxOf { it.z }

        // Project corners to 2D to find screen bounds
        val corners = listOf(
            Point3D(minX, minY, minZ),
            Point3D(maxX, minY, minZ),
            Point3D(minX, maxY, minZ),
            Point3D(maxX, maxY, minZ),
            Point3D(minX, minY, maxZ),
            Point3D(maxX, minY, maxZ),
            Point3D(minX, maxY, maxZ),
            Point3D(maxX, maxY, maxZ)
        )

        val projected = projectAll(corners, scale = 1f)

        val screenMinX = projected.minOf { it.x }
        val screenMaxX = projected.maxOf { it.x }
        val screenMinY = projected.minOf { it.y }
        val screenMaxY = projected.maxOf { it.y }

        val sceneWidth = screenMaxX - screenMinX
        val sceneHeight = screenMaxY - screenMinY

        // Calculate scale to fit with padding
        val scaleX = (canvasWidth - 2 * padding) / sceneWidth
        val scaleY = (canvasHeight - 2 * padding) / sceneHeight
        val scale = minOf(scaleX, scaleY)

        // Calculate center offset
        val sceneCenterX = (screenMinX + screenMaxX) / 2
        val sceneCenterY = (screenMinY + screenMaxY) / 2

        val offsetX = canvasWidth / 2 - sceneCenterX * scale
        val offsetY = canvasHeight / 2 - sceneCenterY * scale

        return Pair(scale, Offset(offsetX, offsetY))
    }
}
