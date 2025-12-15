package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Complete result of a 3D scene analysis operation.
 *
 * Contains the detected scene structure along with metadata about the analysis.
 *
 * @property scene Detected structural elements and objects (nullable if backend doesn't return it)
 * @property inferenceTime Time taken for ML inference in seconds (defaults to 0.0 if not provided)
 * @property modelVersion Version identifier of the SpatialLM model used (defaults to "unknown" if not provided)
 * @property pointCount Number of points in the analyzed point cloud (defaults to 0 if not provided)
 */
@Serializable
data class AnalysisResult(
    val scene: SceneStructure? = null,
    val inferenceTime: Float = 0.0f,
    val modelVersion: String = "unknown",
    val pointCount: Int = 0
) {
    /**
     * Returns a summary string for logging or display.
     * Handles nullable scene gracefully.
     */
    val summary: String
        get() = buildString {
            append("Model: $modelVersion\n")
            append("Points: $pointCount\n")
            append("Inference: ${inferenceTime}s\n")

            if (scene != null) {
                append("Detected: ${scene.walls.size} walls, ")
                append("${scene.doors.size} doors, ")
                append("${scene.windows.size} windows, ")
                append("${scene.objects.size} objects")
            } else {
                append("Scene data not available")
            }
        }

    /**
     * Checks if analysis completed quickly (under 5 seconds).
     */
    val isFastInference: Boolean
        get() = inferenceTime < 5f

    /**
     * Checks if the analysis result is valid (has scene data).
     */
    val isValid: Boolean
        get() = scene != null
}
