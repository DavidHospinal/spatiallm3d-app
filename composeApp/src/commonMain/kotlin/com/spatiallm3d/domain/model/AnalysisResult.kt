package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Complete result of a 3D scene analysis operation.
 *
 * Contains the detected scene structure along with metadata about the analysis.
 *
 * @property scene Detected structural elements and objects
 * @property inferenceTime Time taken for ML inference in seconds
 * @property modelVersion Version identifier of the SpatialLM model used
 * @property pointCount Number of points in the analyzed point cloud
 */
@Serializable
data class AnalysisResult(
    val scene: SceneStructure,
    val inferenceTime: Float,
    val modelVersion: String,
    val pointCount: Int
) {
    /**
     * Returns a summary string for logging or display.
     */
    val summary: String
        get() = buildString {
            append("Model: $modelVersion\n")
            append("Points: $pointCount\n")
            append("Inference: ${inferenceTime}s\n")
            append("Detected: ${scene.walls.size} walls, ")
            append("${scene.doors.size} doors, ")
            append("${scene.windows.size} windows, ")
            append("${scene.objects.size} objects")
        }

    /**
     * Checks if analysis completed quickly (under 5 seconds).
     */
    val isFastInference: Boolean
        get() = inferenceTime < 5f
}
