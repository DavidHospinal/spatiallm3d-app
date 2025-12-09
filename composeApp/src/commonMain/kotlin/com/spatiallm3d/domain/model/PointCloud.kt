package com.spatiallm3d.domain.model

/**
 * Represents a collection of 3D points captured from an AR session or uploaded file.
 *
 * Point clouds are the input data for the SpatialLM ML model.
 *
 * @property points List of 3D points
 * @property sourceType Origin of the point cloud (AR_CAPTURE, FILE_UPLOAD, DEMO)
 * @property timestamp When the point cloud was created (Unix epoch milliseconds)
 */
data class PointCloud(
    val points: List<Point3D>,
    val sourceType: SourceType,
    val timestamp: Long = 0L
) {
    val pointCount: Int
        get() = points.size

    /**
     * Returns a downsampled version of the point cloud.
     *
     * Useful for reducing processing time on large datasets.
     *
     * @param targetCount Desired number of points
     * @return PointCloud with approximately targetCount points
     */
    fun downsample(targetCount: Int): PointCloud {
        if (points.size <= targetCount) return this

        val step = points.size / targetCount
        val sampledPoints = points.filterIndexed { index, _ -> index % step == 0 }
            .take(targetCount)

        return copy(points = sampledPoints)
    }

    enum class SourceType {
        AR_CAPTURE,
        FILE_UPLOAD,
        DEMO
    }
}
