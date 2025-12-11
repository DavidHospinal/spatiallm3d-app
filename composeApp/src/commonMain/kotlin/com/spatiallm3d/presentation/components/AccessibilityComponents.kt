package com.spatiallm3d.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Risk level for accessibility assessment.
 */
enum class RiskLevel {
    SAFE,      // Green - No issues
    WARNING,   // Yellow - Minor concerns
    DANGER     // Red - Critical issues
}

/**
 * Card displaying accessibility score with visual indicator.
 *
 * Shows a 0-100 score with color-coded risk level and animated progress.
 * Works on all platforms with touch, click, and keyboard support.
 *
 * @param score Accessibility score (0-100)
 * @param title Card title
 * @param description Optional description text
 * @param modifier Modifier for layout customization
 */
@Composable
fun AccessibilityScoreCard(
    score: Int,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    val riskLevel = when {
        score >= 80 -> RiskLevel.SAFE
        score >= 50 -> RiskLevel.WARNING
        else -> RiskLevel.DANGER
    }

    val color = when (riskLevel) {
        RiskLevel.SAFE -> Color(0xFF4CAF50)
        RiskLevel.WARNING -> Color(0xFFFFC107)
        RiskLevel.DANGER -> Color(0xFFF44336)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated score circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f))
                )

                // Score text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Risk level indicator
            RiskIndicator(riskLevel)

            if (description != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Visual risk level indicator with icon and text.
 *
 * @param level Risk level to display
 */
@Composable
fun RiskIndicator(level: RiskLevel) {
    val (text, color) = when (level) {
        RiskLevel.SAFE -> "Safe & Accessible" to Color(0xFF4CAF50)
        RiskLevel.WARNING -> "Minor Concerns" to Color(0xFFFFC107)
        RiskLevel.DANGER -> "Critical Issues" to Color(0xFFF44336)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

/**
 * Card for displaying actionable recommendations.
 *
 * @param title Recommendation title
 * @param description Detailed recommendation text
 * @param priority Priority level (HIGH, MEDIUM, LOW)
 * @param modifier Modifier for layout customization
 */
@Composable
fun RecommendationCard(
    title: String,
    description: String,
    priority: String = "MEDIUM",
    modifier: Modifier = Modifier
) {
    val priorityColor = when (priority.uppercase()) {
        "HIGH" -> Color(0xFFF44336)
        "MEDIUM" -> Color(0xFFFFC107)
        "LOW" -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = priority,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Chip component for object categories with confidence.
 *
 * @param category Object category name
 * @param confidence Confidence score (0.0 to 1.0)
 */
@Composable
fun ObjectCategoryChip(
    category: String,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
