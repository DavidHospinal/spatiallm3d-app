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
import com.spatiallm3d.utils.format

/**
 * Enhanced results screen with 3D visualization and natural language accessibility insights.
 *
 * Shows:
 * - 3D scene visualization with safety colors
 * - Human-readable accessibility score
 * - Actionable safety recommendations in plain English
 * - Detailed detection list with natural descriptions
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
                        text = "Your Home Analysis is Ready",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoChip("Analyzed in ${result.inferenceTime.format(1)}s")
                        InfoChip("${result.pointCount} data points")
                        InfoChip("AI Model: ${result.modelVersion}")
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
                    text = { Text("Safety Score") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Full Report") }
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
                        text = "Analyze Another Room",
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
        // Calculate accessibility score
        val scoreResult = calculateDetailedAccessibilityScore(result)

        item {
            AccessibilityScoreCard(
                score = scoreResult.score,
                title = "Home Safety Score",
                description = scoreResult.summary
            )
        }

        item {
            Text(
                text = "What This Means",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = scoreResult.interpretation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            Text(
                text = "Recommended Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Generate actionable recommendations
        items(generateNaturalLanguageRecommendations(result)) { recommendation ->
            RecommendationCard(
                title = recommendation.title,
                description = recommendation.description,
                priority = recommendation.priority
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Found in Your Space",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Text(
                text = "We detected ${result.scene.objects.size} items in your room:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
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
            SectionHeader("Room Structure")
        }

        item {
            DetailCard("Walls", listOf(
                "Found ${result.scene.walls.size} walls in this room",
                "Total wall area: ${result.scene.walls.sumOf { it.area.toDouble() }.format(1)} square meters",
                "Average height: ${result.scene.walls.map { it.height }.average().format(2)} meters"
            ))
        }

        item {
            SectionHeader("Entryways (${result.scene.doors.size})")
        }

        items(result.scene.doors) { door ->
            val safetyStatus = when {
                door.width >= 0.8f -> "Wheelchair accessible"
                door.width >= 0.7f -> "Standard doorway"
                else -> "May be too narrow for some users"
            }

            DetailCard("Doorway ${door.id.replace("door_", "#")}", listOf(
                "Width: ${door.width.format(2)} meters (${(door.width * 39.37).format(0)} inches)",
                "Status: $safetyStatus",
                "Height: ${door.height.format(2)} meters",
                if (door.isStandardSize) "Meets standard building codes" else "Non-standard dimensions"
            ))
        }

        item {
            SectionHeader("Windows (${result.scene.windows.size})")
        }

        items(result.scene.windows) { window ->
            DetailCard("Window ${window.id.replace("window_", "#")}", listOf(
                "Size: ${window.width.format(2)}m x ${window.height.format(2)}m",
                "Area: ${window.area.format(2)} square meters",
                "Natural light source"
            ))
        }

        item {
            SectionHeader("Furniture & Objects (${result.scene.objects.size})")
        }

        items(result.scene.objects) { obj ->
            val humanName = toHumanReadableObjectName(obj.objectClass)
            val confidence = (obj.confidence * 100).toInt()
            val safetyNote = getObjectSafetyNote(obj.objectClass)

            DetailCard(humanName, listOf(
                "Detection confidence: $confidence%",
                "Approximate size: ${obj.volume.format(2)} cubic meters",
                "Location: (${obj.position.x.format(1)}m, ${obj.position.y.format(1)}m, ${obj.position.z.format(1)}m)",
                safetyNote
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
                    text = "• $detail",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Helper data class for detailed scoring
private data class AccessibilityScoreResult(
    val score: Int,
    val summary: String,
    val interpretation: String
)

// Enhanced scoring with natural language feedback
private fun calculateDetailedAccessibilityScore(result: AnalysisResult): AccessibilityScoreResult {
    var score = 100
    val issues = mutableListOf<String>()

    // Check doorways for accessibility
    val narrowDoors = result.scene.doors.count { it.width < 0.8f }
    val veryNarrowDoors = result.scene.doors.count { it.width < 0.7f }

    when {
        veryNarrowDoors > 0 -> {
            score -= 20
            issues.add("$veryNarrowDoors very narrow doorway(s) detected")
        }
        narrowDoors > 0 -> {
            score -= 10
            issues.add("$narrowDoors doorway(s) may be difficult for wheelchair users")
        }
    }

    // Check for potential obstacles
    val furnitureCount = result.scene.objects.count {
        it.objectClass.lowercase() in listOf("chair", "table", "sofa", "desk", "cabinet")
    }

    if (furnitureCount > 15) {
        score -= 15
        issues.add("High furniture density may limit mobility")
    } else if (furnitureCount > 10) {
        score -= 8
        issues.add("Moderate furniture density")
    }

    // Check for hazards
    val hasStairs = result.scene.objects.any {
        it.objectClass.lowercase() in listOf("stairs", "staircase")
    }

    if (hasStairs) {
        score -= 10
        issues.add("Stairs present - fall hazard for some users")
    }

    val finalScore = score.coerceIn(0, 100)

    val summary = when {
        finalScore >= 90 -> "Excellent accessibility - well-designed space"
        finalScore >= 75 -> "Good accessibility with minor areas for improvement"
        finalScore >= 60 -> "Fair accessibility - some modifications recommended"
        else -> "Accessibility concerns identified - improvements needed"
    }

    val interpretation = buildString {
        append("Based on our analysis of ")
        append("${result.scene.walls.size} walls, ")
        append("${result.scene.doors.size} doorways, ")
        append("and ${result.scene.objects.size} objects, ")

        if (finalScore >= 85) {
            append("your space is well-designed for accessibility. ")
            append("The doorways are wide enough for wheelchairs, ")
            append("and there's good circulation space.")
        } else if (finalScore >= 65) {
            append("your space has some accessibility features but could be improved. ")
            if (issues.isNotEmpty()) {
                append("Key concerns: ${issues.joinToString(", ")}. ")
            }
            append("Consider the recommendations below to make your space more accessible.")
        } else {
            append("your space has several accessibility challenges. ")
            if (issues.isNotEmpty()) {
                append("Major issues: ${issues.joinToString(", ")}. ")
            }
            append("We strongly recommend reviewing the safety recommendations below.")
        }
    }

    return AccessibilityScoreResult(
        score = finalScore,
        summary = summary,
        interpretation = interpretation
    )
}

private data class Recommendation(
    val title: String,
    val description: String,
    val priority: String
)

// Generate natural language, actionable recommendations
private fun generateNaturalLanguageRecommendations(result: AnalysisResult): List<Recommendation> {
    val recommendations = mutableListOf<Recommendation>()

    // Check each door
    result.scene.doors.forEach { door ->
        when {
            door.width < 0.7f -> {
                recommendations.add(
                    Recommendation(
                        title = "Critical: Very Narrow Doorway",
                        description = "Doorway ${door.id.replace("door_", "#")} is only ${door.width}m (${(door.width * 39.37).toInt()} inches) wide. This is too narrow for wheelchair access and may be difficult for people with mobility aids. Consider widening to at least 0.8m (32 inches) or consulting with a contractor about accessibility modifications.",
                        priority = "HIGH"
                    )
                )
            }
            door.width < 0.8f -> {
                recommendations.add(
                    Recommendation(
                        title = "Narrow Doorway Detected",
                        description = "Doorway ${door.id.replace("door_", "#")} is ${door.width}m wide. While passable, this is below the recommended 0.8m (32 inches) for wheelchair accessibility. If this is a frequently used entrance, consider widening it for better access.",
                        priority = "MEDIUM"
                    )
                )
            }
        }
    }

    // Check for clutter
    val objectCount = result.scene.objects.size
    if (objectCount > 15) {
        recommendations.add(
            Recommendation(
                title = "High Object Density",
                description = "We detected $objectCount items in your space. While this isn't necessarily a problem, high furniture density can make navigation difficult, especially for people with mobility challenges. Consider decluttering or reorganizing to create clearer pathways.",
                priority = "MEDIUM"
            )
        )
    }

    // Check for stairs
    val hasStairs = result.scene.objects.any {
        it.objectClass.lowercase() in listOf("stairs", "staircase")
    }

    if (hasStairs) {
        recommendations.add(
            Recommendation(
                title = "Stairs Present - Fall Hazard",
                description = "Stairs were detected in your space. These can be a fall hazard, especially for elderly individuals or those with mobility challenges. Consider adding handrails on both sides, ensuring good lighting, and using non-slip surfaces. If possible, ensure an alternative accessible route is available.",
                priority = "HIGH"
            )
        )
    }

    // Add positive feedback if no major issues
    if (recommendations.isEmpty() || recommendations.none { it.priority == "HIGH" }) {
        recommendations.add(
            0, // Insert at beginning
            Recommendation(
                title = "Well-Designed Space",
                description = "Your space shows good accessibility features. All doorways meet or exceed standard width requirements, and there's adequate circulation space. Continue maintaining clear pathways and good lighting for optimal safety.",
                priority = "LOW"
            )
        )
    }

    // Add general safety tips
    recommendations.add(
        Recommendation(
            title = "General Safety Tips",
            description = "Ensure good lighting throughout the space, keep pathways clear of obstacles, secure loose rugs to prevent trips, and maintain at least 0.9m (36 inches) of clear passage width in hallways for comfortable navigation.",
            priority = "LOW"
        )
    )

    return recommendations
}

// Convert technical names to user-friendly labels
private fun toHumanReadableObjectName(objectClass: String): String {
    return when (objectClass.lowercase()) {
        "chair" -> "Chair"
        "table" -> "Table"
        "sofa", "couch" -> "Sofa"
        "bed" -> "Bed"
        "door" -> "Doorway"
        "window" -> "Window"
        "stairs", "staircase" -> "Staircase"
        "cabinet", "wardrobe" -> "Storage Cabinet"
        "desk" -> "Desk"
        "shelf", "shelves" -> "Shelving Unit"
        "tv", "television" -> "Television"
        "plant" -> "Plant"
        "lamp" -> "Lighting Fixture"
        "refrigerator", "fridge" -> "Refrigerator"
        "oven" -> "Oven"
        "sink" -> "Sink"
        else -> objectClass.split("_", "-")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}

// Provide safety context for detected objects
private fun getObjectSafetyNote(objectClass: String): String {
    return when (objectClass.lowercase()) {
        "stairs", "staircase" -> "Potential fall hazard - ensure handrails and good lighting"
        "chair", "table" -> "Ensure adequate clearance around furniture for easy navigation"
        "sofa", "couch" -> "Large furniture - maintain clear pathways around it"
        "cabinet", "wardrobe" -> "Ensure stable and secured to wall if tall"
        "rug", "carpet" -> "Ensure securely fastened to prevent trip hazards"
        else -> "No specific safety concerns for this item"
    }
}
