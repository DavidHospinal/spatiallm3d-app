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

        // Read header as text to determine format
        val headerEndMarker = "end_header\n".toByteArray()
        var headerEndIndex = -1

        for (i in 0..content.size - headerEndMarker.size) {
            var match = true
            for (j in headerEndMarker.indices) {
                if (content[i + j] != headerEndMarker[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                headerEndIndex = i + headerEndMarker.size
                break
            }
        }

        if (headerEndIndex == -1) {
            println("PlyParser ERROR: Could not find end_header marker")
            throw IllegalArgumentException("Invalid PLY file: missing end_header")
        }

        val headerBytes = content.sliceArray(0 until headerEndIndex)
        val headerText = headerBytes.decodeToString()
        val headerLines = headerText.lines()

        println("PlyParser: Found header with ${headerLines.size} lines")

        if (headerLines.isEmpty() || !headerLines[0].trim().equals("ply", ignoreCase = true)) {
            println("PlyParser ERROR: Missing 'ply' header")
            throw IllegalArgumentException("Invalid PLY file: missing 'ply' header")
        }

        var vertexCount = 0
        var isBinary = false
        val points = mutableListOf<Point3D>()
        val vertexProperties = mutableListOf<Pair<String, String>>() // (type, name)

        // Parse header
        var inVertexElement = false
        for (line in headerLines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("format")) {
                if (trimmed.contains("binary")) {
                    isBinary = true
                    println("PlyParser: Binary format detected")
                } else {
                    println("PlyParser: ASCII format detected")
                }
            }

            if (trimmed.startsWith("element vertex")) {
                vertexCount = trimmed.split(" ")[2].toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid vertex count")
                println("PlyParser: Vertex count = $vertexCount")
                inVertexElement = true
            } else if (trimmed.startsWith("element")) {
                inVertexElement = false
            }

            if (inVertexElement && trimmed.startsWith("property")) {
                val parts = trimmed.split(Regex("\\s+"))
                if (parts.size >= 3) {
                    val propertyType = parts[1]
                    val propertyName = parts[2]
                    vertexProperties.add(Pair(propertyType, propertyName))
                }
            }
        }

        println("PlyParser: Detected ${vertexProperties.size} vertex properties:")
        vertexProperties.forEach { (type, name) ->
            println("  - $name: $type")
        }

        if (vertexCount == 0) {
            println("PlyParser WARNING: Vertex count is 0")
            return PointCloud(
                points = emptyList(),
                sourceType = PointCloud.SourceType.FILE_UPLOAD,
                timestamp = 0L
            )
        }

        // Parse vertex data based on format
        if (isBinary) {
            println("PlyParser: Parsing binary vertex data from offset $headerEndIndex")
            return parseBinaryVertices(content, headerEndIndex, vertexCount, vertexProperties)
        } else {
            println("PlyParser: Parsing ASCII vertex data")
            return parseAsciiVertices(headerLines, vertexCount)
        }
    }

    private fun parseBinaryVertices(
        content: ByteArray,
        dataOffset: Int,
        vertexCount: Int,
        properties: List<Pair<String, String>> // (type, name)
    ): PointCloud {
        val points = mutableListOf<Point3D>()

        // Calculate bytes per vertex based on property types
        var bytesPerVertex = 0
        for ((type, _) in properties) {
            bytesPerVertex += when (type) {
                "float", "float32" -> 4
                "double", "float64" -> 8
                "uchar", "uint8", "char", "int8" -> 1
                "ushort", "uint16", "short", "int16" -> 2
                "uint", "uint32", "int", "int32" -> 4
                else -> 4 // Default to 4 bytes
            }
        }

        println("PlyParser: Calculated $bytesPerVertex bytes per vertex (${properties.size} properties)")

        var offset = dataOffset
        for (i in 0 until vertexCount) {
            if (offset + bytesPerVertex > content.size) {
                println("PlyParser WARNING: Not enough data for all vertices, parsed ${points.size}/$vertexCount")
                break
            }

            // Read x, y, z (assume they are the first 3 properties and are floats)
            val x = readFloatLE(content, offset)
            val y = readFloatLE(content, offset + 4)
            val z = readFloatLE(content, offset + 8)

            points.add(Point3D(x, y, z))
            offset += bytesPerVertex
        }

        println("PlyParser: Successfully parsed ${points.size} binary points (expected $vertexCount)")
        if (points.isNotEmpty()) {
            println("PlyParser: First point: x=${points[0].x}, y=${points[0].y}, z=${points[0].z}")
            if (points.size >= 2) {
                println("PlyParser: Second point: x=${points[1].x}, y=${points[1].y}, z=${points[1].z}")
            }
        }

        return PointCloud(
            points = points,
            sourceType = PointCloud.SourceType.FILE_UPLOAD,
            timestamp = 0L
        )
    }

    private fun readFloatLE(bytes: ByteArray, offset: Int): Float {
        val intBits = (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
        return Float.fromBits(intBits)
    }

    private fun parseAsciiVertices(headerLines: List<String>, vertexCount: Int): PointCloud {
        val points = mutableListOf<Point3D>()
        var count = 0
        var startParsing = false

        for (line in headerLines) {
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
                    if (count < 10) {
                        println("PlyParser: Skipping invalid line at index $count: '$line'")
                    }
                }
            }
        }

        println("PlyParser: Successfully parsed ${points.size} ASCII points (expected $vertexCount)")

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
