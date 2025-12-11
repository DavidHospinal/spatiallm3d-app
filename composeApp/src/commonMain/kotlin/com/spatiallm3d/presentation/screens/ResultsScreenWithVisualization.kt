package com.spatiallm3d.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spatiallm3d.domain.model.AnalysisResult
import com.spatiallm3d.presentation.components.AccessibilityScoreCard
import com.spatiallm3d.presentation.components.ObjectCategoryChip
import com.spatiallm3d.presentation.components.RecommendationCard
import com.spatiallm3d.presentation.visualization.Scene3DView

/**
 * Enhanced results screen with 3D visualization and accessibility insights.
 *
 * Shows:
 * - 3D scene visualization
 * - Accessibility score
 * - Safety recommendations
 * - Detailed detections list
 *
 * @param result Analysis result
 * @param onBackToHome Callback to return home
 */
@Composable
fun ResultsScreenWithVisualization(
    result: AnalysisResult,
    onBackToHome: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

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
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Scene Analysis Complete",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoChip("Model: ${result.modelVersion}")
                        InfoChip("Time: ${"%.2f".format(result.inferenceTime)}s")
                        InfoChip("Points: ${result.pointCount}")
                    }
                }
            }

            // Tab bar
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("3D View") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Accessibility") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Details") }
                )
            }

            // Tab content
            when (selectedTab) {
                0 -> Scene3DView(
                    sceneStructure = result.scene,
                    modifier = Modifier.weight(1f)
                )

                1 -> AccessibilityTab(
                    result = result,
                    modifier = Modifier.weight(1f)
                )

                2 -> DetailsTab(
                    result = result,
                    modifier = Modifier.weight(1f)
                )
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
                        text = "Analyze Another Scene",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun AccessibilityTab(
    result: AnalysisResult,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calculate mock accessibility score (in real app, backend would provide this)
        val score = calculateAccessibilityScore(result)

        item {
            AccessibilityScoreCard(
                score = score,
                title = "Home Accessibility Score",
                description = "Based on spatial analysis and safety standards"
            )
        }

        item {
            Text(
                text = "Safety Recommendations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Generate recommendations
        items(generateRecommendations(result)) { recommendation ->
            RecommendationCard(
                title = recommendation.title,
                description = recommendation.description,
                priority = recommendation.priority
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Detected Objects",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                result.scene.objects.take(6).forEach { obj ->
                    ObjectCategoryChip(
                        category = obj.objectClass,
                        confidence = obj.confidence
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsTab(
    result: AnalysisResult,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader("Walls (${result.scene.walls.size})")
        }
        items(result.scene.walls) { wall ->
            DetailCard("Wall ${wall.id}", listOf(
                "Length: ${"%.2f".format(wall.length)}m",
                "Height: ${"%.2f".format(wall.height)}m",
                "Area: ${"%.2f".format(wall.area)}m²"
            ))
        }

        item {
            SectionHeader("Doors (${result.scene.doors.size})")
        }
        items(result.scene.doors) { door ->
            DetailCard("Door ${door.id}", listOf(
                "Wall: ${door.wallId}",
                "Size: ${"%.2f".format(door.width)}m x ${"%.2f".format(door.height)}m",
                "Standard: ${if (door.isStandardSize) "Yes" else "No"}"
            ))
        }

        item {
            SectionHeader("Windows (${result.scene.windows.size})")
        }
        items(result.scene.windows) { window ->
            DetailCard("Window ${window.id}", listOf(
                "Wall: ${window.wallId}",
                "Size: ${"%.2f".format(window.width)}m x ${"%.2f".format(window.height)}m",
                "Area: ${"%.2f".format(window.area)}m²"
            ))
        }

        item {
            SectionHeader("Objects (${result.scene.objects.size})")
        }
        items(result.scene.objects) { obj ->
            DetailCard(obj.objectClass.replaceFirstChar { it.uppercase() }, listOf(
                "Confidence: ${(obj.confidence * 100).toInt()}%",
                "Volume: ${"%.2f".format(obj.volume)}m³",
                "Position: (${"%.1f".format(obj.position.x)}, ${"%.1f".format(obj.position.y)}, ${"%.1f".format(obj.position.z)})"
            ))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun DetailCard(title: String, details: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            details.forEach { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// Helper functions

private fun calculateAccessibilityScore(result: AnalysisResult): Int {
    var score = 100

    // Deduct points for narrow doors
    result.scene.doors.forEach { door ->
        if (!door.isStandardSize) score -= 10
    }

    // Deduct points for many obstacles
    if (result.scene.objects.size > 10) score -= 15

    return score.coerceIn(0, 100)
}

private data class Recommendation(
    val title: String,
    val description: String,
    val priority: String
)

private fun generateRecommendations(result: AnalysisResult): List<Recommendation> {
    val recommendations = mutableListOf<Recommendation>()

    // Check doors
    result.scene.doors.forEach { door ->
        if (!door.isStandardSize) {
            recommendations.add(
                Recommendation(
                    title = "Non-standard door detected",
                    description = "Door ${door.id} is ${door.width}m wide. Standard wheelchair-accessible doors should be at least 0.8m wide.",
                    priority = "HIGH"
                )
            )
        }
    }

    // Check for clutter
    if (result.scene.objects.size > 15) {
        recommendations.add(
            Recommendation(
                title = "High object density detected",
                description = "Space contains ${result.scene.objects.size} objects. Consider reducing clutter to improve safety and mobility.",
                priority = "MEDIUM"
            )
        )
    }

    // Add positive feedback
    if (recommendations.isEmpty()) {
        recommendations.add(
            Recommendation(
                title = "Space is well-organized",
                description = "No critical accessibility issues detected. The space appears safe and accessible.",
                priority = "LOW"
            )
        )
    }

    return recommendations
}
