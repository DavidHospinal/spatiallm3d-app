package com.spatiallm3d.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spatiallm3d.domain.model.AnalysisResult
import com.spatiallm3d.domain.model.BoundingBox
import com.spatiallm3d.domain.model.Door
import com.spatiallm3d.domain.model.Wall
import com.spatiallm3d.domain.model.Window
import com.spatiallm3d.utils.format

/**
 * Results screen displaying all detected scene elements.
 *
 * Shows walls, doors, windows, and all detected objects with their categories.
 *
 * @param result The analysis result containing all detections
 * @param onBackToHome Callback to navigate back to home screen
 */
@Composable
fun ResultsScreen(
    result: AnalysisResult,
    onBackToHome: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Analysis Results",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetadataChip("Model: ${result.modelVersion}")
                        MetadataChip("Time: ${result.inferenceTime.format(2)}s")
                        MetadataChip("Points: ${result.pointCount}")
                    }
                }
            }

            // Results content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Walls section
                item {
                    SectionHeader("Walls (${result.scene?.walls?.size ?: 0})")
                }
                items(result.scene?.walls ?: emptyList()) { wall ->
                    WallCard(wall)
                }

                // Doors section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("Doors (${result.scene?.doors?.size ?: 0})")
                }
                items(result.scene?.doors ?: emptyList()) { door ->
                    DoorCard(door)
                }

                // Windows section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("Windows (${result.scene?.windows?.size ?: 0})")
                }
                items(result.scene?.windows ?: emptyList()) { window ->
                    WindowCard(window)
                }

                // Objects section - ALL objects with their categories
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader("Objects (${result.scene?.objects?.size ?: 0})")
                }
                items(result.scene?.objects ?: emptyList()) { obj ->
                    ObjectCard(obj)
                }
            }

            // Footer button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp
            ) {
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Back to Home",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun WallCard(wall: Wall) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Wall ${wall.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Start Point", "(${wall.startPoint.x}, ${wall.startPoint.y}, ${wall.startPoint.z})")
            DetailRow("End Point", "(${wall.endPoint.x}, ${wall.endPoint.y}, ${wall.endPoint.z})")
            DetailRow("Height", "${wall.height.format(2)}m")
            DetailRow("Length", "${wall.length.format(2)}m")
        }
    }
}

@Composable
private fun DoorCard(door: Door) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Door ${door.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Wall", door.wallId)
            DetailRow("Position", "(${door.position.x}, ${door.position.y}, ${door.position.z})")
            DetailRow("Dimensions", "${door.width.format(2)}m x ${door.height.format(2)}m")
            DetailRow("Area", "${door.area.format(2)}m²")
        }
    }
}

@Composable
private fun WindowCard(window: Window) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Window ${window.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Wall", window.wallId)
            DetailRow("Position", "(${window.position.x}, ${window.position.y}, ${window.position.z})")
            DetailRow("Dimensions", "${window.width.format(2)}m x ${window.height.format(2)}m")
            DetailRow("Area", "${window.area.format(2)}m²")
        }
    }
}

@Composable
private fun ObjectCard(obj: BoundingBox) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = obj.objectClass.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "${(obj.confidence * 100).format(1)}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Position", "(${obj.position.x}, ${obj.position.y}, ${obj.position.z})")
            DetailRow("Size", "${obj.scale.x} x ${obj.scale.y} x ${obj.scale.z}")
            DetailRow("Rotation", "${obj.rotation} rad")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
