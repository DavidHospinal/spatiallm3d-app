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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spatiallm3d.domain.model.*

/**
 * Interactive 3D scene visualization using isometric projection with safety assessment.
 *
 * Features:
 * - Risk-based color coding (green/yellow/red)
 * - Natural language object labels
 * - Safety indicators for accessibility concerns
 * - Interactive pan and zoom gestures
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
    val textMeasurer = rememberTextMeasurer()

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
                        text = "3D Scene View - Safety Assessment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Drag to pan",
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

                    // Draw doors with safety colors
                    sceneStructure.doors.forEach { door ->
                        drawDoorWithSafety(door, scale, offsetX, offsetY, textMeasurer)
                    }

                    // Draw windows
                    sceneStructure.windows.forEach { window ->
                        drawWindow(window, scale, offsetX, offsetY)
                    }

                    // Draw object bounding boxes with natural language labels
                    sceneStructure.objects.forEach { obj ->
                        drawBoundingBoxWithLabel(obj, scale, offsetX, offsetY, textMeasurer)
                    }
                }

                // Enhanced Legend with Safety Colors
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
                        Text(
                            text = "Safety Legend",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LegendItem("Walls & Structure", Color(0xFF64B5F6))
                        LegendItem("Safe Doorways", Color(0xFF4CAF50))
                        LegendItem("Narrow Doorways", Color(0xFFFF9800))
                        LegendItem("Accessibility Risk", Color(0xFFF44336))
                        LegendItem("Windows", Color(0xFFFFB74D))
                        LegendItem("Detected Objects", Color(0xFF9C27B0))
                    }
                }

                // Safety Tips
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    color = Color(0xCC000000),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Accessibility Standards",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Standard doorway: 0.8m - 1.0m wide",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Wheelchair access: minimum 0.8m",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800)
                        )
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
private fun DrawScope.drawGrid(
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
private fun DrawScope.drawWall(
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
 * Draws a door marker with safety color coding.
 * Green = safe, Yellow = narrow, Red = accessibility concern
 */
private fun DrawScope.drawDoorWithSafety(
    door: Door,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val center = IsometricProjection.project(door.position, scale, offsetX, offsetY)

    // Determine safety color based on door width
    val (color, label) = when {
        door.width >= 0.8f -> Color(0xFF4CAF50) to "Safe" // Green - wheelchair accessible
        door.width >= 0.7f -> Color(0xFFFF9800) to "Narrow" // Orange - barely passable
        else -> Color(0xFFF44336) to "Risk!" // Red - accessibility concern
    }

    // Draw door marker circle
    drawCircle(
        color = color.copy(alpha = 0.6f),
        radius = 12f,
        center = center
    )

    drawCircle(
        color = color,
        radius = 12f,
        center = center,
        style = Stroke(width = 3f)
    )

    // Draw safety indicator icon (exclamation mark for risks)
    if (door.width < 0.8f) {
        val iconPath = Path().apply {
            // Warning triangle
            moveTo(center.x, center.y - 8f)
            lineTo(center.x - 6f, center.y + 4f)
            lineTo(center.x + 6f, center.y + 4f)
            close()
        }
        drawPath(
            path = iconPath,
            color = Color.White,
            style = Stroke(width = 2f)
        )
    }

    // Draw natural language label
    val labelText = "${door.width}m - $label"
    val textLayoutResult = textMeasurer.measure(
        text = labelText,
        style = TextStyle(
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            background = color.copy(alpha = 0.8f)
        )
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(center.x - textLayoutResult.size.width / 2, center.y + 16f)
    )
}

/**
 * Draws a window marker.
 */
private fun DrawScope.drawWindow(
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
 * Draws an object bounding box with natural language label.
 */
private fun DrawScope.drawBoundingBoxWithLabel(
    obj: BoundingBox,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
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

    // Determine color based on object safety risk
    val color = getObjectSafetyColor(obj.objectClass)

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

    // Draw natural language label
    val humanLabel = toHumanReadableLabel(obj.objectClass)
    val confidence = (obj.confidence * 100).toInt()
    val labelText = "$humanLabel ($confidence%)"

    val textLayoutResult = textMeasurer.measure(
        text = labelText,
        style = TextStyle(
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            background = color.copy(alpha = 0.85f)
        )
    )

    // Position label above the bounding box
    val labelPosition = projected[6] // Top-right corner
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            labelPosition.x - textLayoutResult.size.width / 2,
            labelPosition.y - 20f
        )
    )
}

/**
 * Determines safety color based on object type.
 * Potential hazards (stairs, furniture) get warning colors.
 */
private fun getObjectSafetyColor(objectClass: String): Color {
    return when (objectClass.lowercase()) {
        "stairs", "staircase" -> Color(0xFFF44336) // Red - fall hazard
        "chair", "table", "sofa", "furniture" -> Color(0xFFFF9800) // Orange - obstacle
        "door", "window" -> Color(0xFF4CAF50) // Green - navigation point
        else -> Color(0xFF9C27B0) // Purple - general object
    }
}

/**
 * Converts technical object class names to natural language.
 */
private fun toHumanReadableLabel(objectClass: String): String {
    return when (objectClass.lowercase()) {
        "chair" -> "Chair"
        "table" -> "Table"
        "sofa", "couch" -> "Sofa"
        "bed" -> "Bed"
        "door" -> "Doorway"
        "window" -> "Window"
        "stairs", "staircase" -> "Stairs (Caution)"
        "cabinet", "wardrobe" -> "Cabinet"
        "desk" -> "Desk"
        "shelf", "shelves" -> "Shelf"
        "tv", "television" -> "TV"
        "plant" -> "Plant"
        "lamp" -> "Lamp"
        else -> objectClass.replaceFirstChar { it.uppercase() }
    }
}
