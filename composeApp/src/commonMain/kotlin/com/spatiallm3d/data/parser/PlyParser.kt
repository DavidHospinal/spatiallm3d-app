package com.spatiallm3d.data.parser

import com.spatiallm3d.domain.model.Point3D
import com.spatiallm3d.domain.model.PointCloud

/**
 * Simple PLY file parser for reading point cloud data.
 *
 * Supports ASCII PLY format with XYZ coordinates and optional RGB colors.
 */
object PlyParser {

    /**
     * Parses a PLY file from byte array content.
     *
     * @param content PLY file content as bytes
     * @return PointCloud with parsed points
     * @throws IllegalArgumentException if file format is invalid
     */
    fun parse(content: ByteArray): PointCloud {
        println("PlyParser: Starting parse, content size = ${content.size} bytes")

        val text = try {
            content.decodeToString()
        } catch (e: Exception) {
            println("PlyParser ERROR: Failed to decode bytes to string: ${e.message}")
            throw IllegalArgumentException("Cannot decode PLY file - may be binary format (not supported)")
        }

        val lines = text.lines()
        println("PlyParser: File has ${lines.size} lines")

        if (lines.isEmpty() || !lines[0].trim().equals("ply", ignoreCase = true)) {
            println("PlyParser ERROR: Missing 'ply' header, first line: '${lines.getOrNull(0)}'")
            throw IllegalArgumentException("Invalid PLY file: missing 'ply' header")
        }

        var vertexCount = 0
        var headerEnded = false
        var isBinaryFormat = false
        val points = mutableListOf<Point3D>()

        // Parse header
        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed == "end_header") {
                headerEnded = true
                break
            }

            if (trimmed.startsWith("format")) {
                if (trimmed.contains("binary")) {
                    isBinaryFormat = true
                    println("PlyParser ERROR: Binary PLY format detected (not supported)")
                    throw IllegalArgumentException("Binary PLY format not supported. Please use ASCII format PLY files.")
                }
                println("PlyParser: Format detected: $trimmed")
            }

            if (trimmed.startsWith("element vertex")) {
                vertexCount = trimmed.split(" ")[2].toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid vertex count")
                println("PlyParser: Vertex count = $vertexCount")
            }
        }

        if (!headerEnded) {
            println("PlyParser ERROR: Missing 'end_header'")
            throw IllegalArgumentException("Invalid PLY file: missing 'end_header'")
        }

        if (vertexCount == 0) {
            println("PlyParser WARNING: Vertex count is 0")
            return PointCloud(
                points = emptyList(),
                sourceType = PointCloud.SourceType.FILE_UPLOAD,
                timestamp = 0L
            )
        }

        // Parse vertex data
        var count = 0
        var startParsing = false

        for (line in lines) {
            if (!startParsing) {
                if (line.trim() == "end_header") {
                    startParsing = true
                }
                continue
            }

            if (count >= vertexCount) break

            val parts = line.trim().split(Regex("\\s+"))
            if (parts.size >= 3) {
                try {
                    val x = parts[0].toFloat()
                    val y = parts[1].toFloat()
                    val z = parts[2].toFloat()

                    points.add(Point3D(x, y, z))
                    count++
                } catch (e: NumberFormatException) {
                    // Skip invalid lines
                    if (count < 10) {
                        println("PlyParser: Skipping invalid line at index $count: '$line'")
                    }
                }
            }
        }

        println("PlyParser: Successfully parsed ${points.size} points (expected $vertexCount)")

        return PointCloud(
            points = points,
            sourceType = PointCloud.SourceType.FILE_UPLOAD,
            timestamp = 0L
        )
    }

    /**
     * Parses a PLY file and returns a downsampled version.
     *
     * Useful for large point clouds to improve rendering performance.
     *
     * @param content PLY file content
     * @param maxPoints Maximum number of points to keep
     * @return Downsampled PointCloud
     */
    fun parseDownsampled(content: ByteArray, maxPoints: Int = 10000): PointCloud {
        val fullCloud = parse(content)

        if (fullCloud.points.size <= maxPoints) {
            return fullCloud
        }

        // Simple uniform downsampling
        val step = fullCloud.points.size / maxPoints
        val downsampled = fullCloud.points.filterIndexed { index, _ ->
            index % step == 0
        }.take(maxPoints)

        return PointCloud(
            points = downsampled,
            sourceType = PointCloud.SourceType.FILE_UPLOAD,
            timestamp = 0L
        )
    }
}
