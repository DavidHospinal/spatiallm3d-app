package com.spatiallm3d.presentation.visualization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spatiallm3d.domain.model.*

/**
 * Interactive 3D scene visualization using isometric projection.
 *
 * Renders walls, doors, windows, and object bounding boxes in a
 * 2D isometric view. Supports pan and zoom gestures on all platforms.
 *
 * @param sceneStructure Scene data to visualize
 * @param modifier Modifier for layout customization
 */
@Composable
fun Scene3DView(
    sceneStructure: SceneStructure,
    modifier: Modifier = Modifier
) {
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomLevel by remember { mutableStateOf(1f) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)),
        color = Color(0xFF1E1E1E)
    ) {
        Column {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2D2D2D),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3D Scene View",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Isometric Projection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // 3D Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                panOffset += dragAmount
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Collect all 3D points for viewport calculation
                    val allPoints = buildList {
                        sceneStructure.walls.forEach {
                            add(it.startPoint)
                            add(it.endPoint)
                        }
                        sceneStructure.objects.forEach {
                            add(it.position)
                        }
                    }

                    if (allPoints.isEmpty()) {
                        return@Canvas
                    }

                    val (baseScale, centerOffset) = IsometricProjection.calculateViewport(
                        points = allPoints,
                        canvasWidth = canvasWidth,
                        canvasHeight = canvasHeight,
                        padding = 80f
                    )

                    val scale = baseScale * zoomLevel
                    val offsetX = centerOffset.x + panOffset.x
                    val offsetY = centerOffset.y + panOffset.y

                    // Draw grid (floor reference)
                    drawGrid(scale, offsetX, offsetY)

                    // Draw walls
                    sceneStructure.walls.forEach { wall ->
                        drawWall(wall, scale, offsetX, offsetY)
                    }

                    // Draw doors
                    sceneStructure.doors.forEach { door ->
                        drawDoor(door, scale, offsetX, offsetY)
                    }

                    // Draw windows
                    sceneStructure.windows.forEach { window ->
                        drawWindow(window, scale, offsetX, offsetY)
                    }

                    // Draw object bounding boxes
                    sceneStructure.objects.forEach { obj ->
                        drawBoundingBox(obj, scale, offsetX, offsetY)
                    }
                }

                // Legend
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    color = Color(0xCC000000),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        LegendItem("Walls", Color(0xFF64B5F6))
                        LegendItem("Doors", Color(0xFF81C784))
                        LegendItem("Windows", Color(0xFFFFB74D))
                        LegendItem("Objects", Color(0xFFE57373))
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

/**
 * Draws a reference grid on the floor plane.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val gridSize = 10
    val gridSpacing = 1f

    for (i in -gridSize..gridSize) {
        val x = i * gridSpacing

        // Lines along X axis
        val start1 = IsometricProjection.project(
            Point3D(x, 0f, -gridSize * gridSpacing),
            scale,
            offsetX,
            offsetY
        )
        val end1 = IsometricProjection.project(
            Point3D(x, 0f, gridSize * gridSpacing),
            scale,
            offsetX,
            offsetY
        )

        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = start1,
            end = end1,
            strokeWidth = 1f
        )

        // Lines along Z axis
        val start2 = IsometricProjection.project(
            Point3D(-gridSize * gridSpacing, 0f, x),
            scale,
            offsetX,
            offsetY
        )
        val end2 = IsometricProjection.project(
            Point3D(gridSize * gridSpacing, 0f, x),
            scale,
            offsetX,
            offsetY
        )

        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = start2,
            end = end2,
            strokeWidth = 1f
        )
    }
}

/**
 * Draws a wall as a vertical plane.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWall(
    wall: Wall,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val bottomStart = IsometricProjection.project(wall.startPoint, scale, offsetX, offsetY)
    val bottomEnd = IsometricProjection.project(wall.endPoint, scale, offsetX, offsetY)

    val topStart = IsometricProjection.project(
        Point3D(wall.startPoint.x, wall.startPoint.y + wall.height, wall.startPoint.z),
        scale,
        offsetX,
        offsetY
    )
    val topEnd = IsometricProjection.project(
        Point3D(wall.endPoint.x, wall.endPoint.y + wall.height, wall.endPoint.z),
        scale,
        offsetX,
        offsetY
    )

    // Draw wall as filled polygon
    val path = Path().apply {
        moveTo(bottomStart.x, bottomStart.y)
        lineTo(bottomEnd.x, bottomEnd.y)
        lineTo(topEnd.x, topEnd.y)
        lineTo(topStart.x, topStart.y)
        close()
    }

    drawPath(
        path = path,
        color = Color(0xFF64B5F6).copy(alpha = 0.3f)
    )

    drawPath(
        path = path,
        color = Color(0xFF64B5F6),
        style = Stroke(width = 2f)
    )
}

/**
 * Draws a door marker.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDoor(
    door: Door,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val center = IsometricProjection.project(door.position, scale, offsetX, offsetY)

    drawCircle(
        color = Color(0xFF81C784),
        radius = 8f,
        center = center
    )

    drawCircle(
        color = Color(0xFF81C784),
        radius = 8f,
        center = center,
        style = Stroke(width = 2f)
    )
}

/**
 * Draws a window marker.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWindow(
    window: Window,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val center = IsometricProjection.project(window.position, scale, offsetX, offsetY)

    drawRect(
        color = Color(0xFFFFB74D).copy(alpha = 0.5f),
        topLeft = Offset(center.x - 6f, center.y - 6f),
        size = Size(12f, 12f)
    )

    drawRect(
        color = Color(0xFFFFB74D),
        topLeft = Offset(center.x - 6f, center.y - 6f),
        size = Size(12f, 12f),
        style = Stroke(width = 2f)
    )
}

/**
 * Draws an object bounding box.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBoundingBox(
    obj: BoundingBox,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val center = obj.position
    val halfScale = Point3D(obj.scale.x / 2, obj.scale.y / 2, obj.scale.z / 2)

    // Calculate 8 corners of the bounding box
    val corners = listOf(
        Point3D(center.x - halfScale.x, center.y - halfScale.y, center.z - halfScale.z),
        Point3D(center.x + halfScale.x, center.y - halfScale.y, center.z - halfScale.z),
        Point3D(center.x + halfScale.x, center.y - halfScale.y, center.z + halfScale.z),
        Point3D(center.x - halfScale.x, center.y - halfScale.y, center.z + halfScale.z),
        Point3D(center.x - halfScale.x, center.y + halfScale.y, center.z - halfScale.z),
        Point3D(center.x + halfScale.x, center.y + halfScale.y, center.z - halfScale.z),
        Point3D(center.x + halfScale.x, center.y + halfScale.y, center.z + halfScale.z),
        Point3D(center.x - halfScale.x, center.y + halfScale.y, center.z + halfScale.z),
    )

    val projected = corners.map {
        IsometricProjection.project(it, scale, offsetX, offsetY)
    }

    val color = Color(0xFFE57373)

    // Draw bottom face
    drawLine(color, projected[0], projected[1], 2f)
    drawLine(color, projected[1], projected[2], 2f)
    drawLine(color, projected[2], projected[3], 2f)
    drawLine(color, projected[3], projected[0], 2f)

    // Draw top face
    drawLine(color, projected[4], projected[5], 2f)
    drawLine(color, projected[5], projected[6], 2f)
    drawLine(color, projected[6], projected[7], 2f)
    drawLine(color, projected[7], projected[4], 2f)

    // Draw vertical edges
    drawLine(color, projected[0], projected[4], 2f)
    drawLine(color, projected[1], projected[5], 2f)
    drawLine(color, projected[2], projected[6], 2f)
    drawLine(color, projected[3], projected[7], 2f)
}
