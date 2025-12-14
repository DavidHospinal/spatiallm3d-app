package com.spatiallm3d.domain.model

/**
 * Safety assessment report generated from scene analysis.
 */
data class SafetyReport(
    val score: Float,              // 0-100
    val level: SafetyLevel,
    val checks: List<SafetyCheck>,
    val objectsDetected: Int,
    val wallsDetected: Int,
    val doorsDetected: Int,
    val windowsDetected: Int,
    val recommendations: List<String>
)

/**
 * Individual safety check result.
 */
data class SafetyCheck(
    val category: String,          // "Accessibility", "Ventilation", etc.
    val description: String,
    val passed: Boolean,
    val severity: SafetySeverity,
    val penaltyPoints: Int
)

/**
 * Overall safety level based on score.
 */
enum class SafetyLevel {
    EXCELLENT,   // 90-100
    GOOD,        // 75-89
    MODERATE,    // 60-74
    POOR,        // 40-59
    CRITICAL     // 0-39
}

/**
 * Severity of individual safety issues.
 */
enum class SafetySeverity {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
