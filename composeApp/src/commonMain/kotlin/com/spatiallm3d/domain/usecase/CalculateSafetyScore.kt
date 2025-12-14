package com.spatiallm3d.domain.usecase

import com.spatiallm3d.domain.model.AnalysisResult
import com.spatiallm3d.domain.model.SafetyReport
import com.spatiallm3d.domain.model.SafetyCheck
import com.spatiallm3d.domain.model.SafetyLevel
import com.spatiallm3d.domain.model.SafetySeverity

/**
 * Use Case: Calcula el Safety Score basado en los resultados del análisis.
 *
 * Criterios:
 * - Puertas: Ancho mínimo 0.9m (accesibilidad)
 * - Ventanas: Al menos 1 por habitación (ventilación)
 * - Objetos peligrosos: Detección de obstáculos en rutas
 * - Espacios abiertos: Distancia mínima entre objetos
 */
class CalculateSafetyScore {

    operator fun invoke(analysis: AnalysisResult): SafetyReport {
        val checks = mutableListOf<SafetyCheck>()
        var totalScore = 100f

        // 1. Verificar puertas (accesibilidad)
        val doorChecks = checkDoors(analysis)
        checks.addAll(doorChecks)
        totalScore -= doorChecks.sumOf { if (!it.passed) it.penaltyPoints.toDouble() else 0.0 }.toFloat()

        // 2. Verificar ventanas (ventilación)
        val windowChecks = checkWindows(analysis)
        checks.addAll(windowChecks)
        totalScore -= windowChecks.sumOf { if (!it.passed) it.penaltyPoints.toDouble() else 0.0 }.toFloat()

        // 3. Verificar obstáculos y espacios
        val obstacleChecks = checkObstacles(analysis)
        checks.addAll(obstacleChecks)
        totalScore -= obstacleChecks.sumOf { if (!it.passed) it.penaltyPoints.toDouble() else 0.0 }.toFloat()

        // 4. Verificar salidas de emergencia
        val exitChecks = checkEmergencyExits(analysis)
        checks.addAll(exitChecks)
        totalScore -= exitChecks.sumOf { if (!it.passed) it.penaltyPoints.toDouble() else 0.0 }.toFloat()

        // Normalizar score (0-100)
        val finalScore = totalScore.coerceIn(0f, 100f)

        return SafetyReport(
            score = finalScore,
            level = getSafetyLevel(finalScore),
            checks = checks,
            objectsDetected = analysis.scene.objects.size,
            wallsDetected = analysis.scene.walls.size,
            doorsDetected = analysis.scene.doors.size,
            windowsDetected = analysis.scene.windows.size,
            recommendations = generateRecommendations(checks)
        )
    }

    private fun checkDoors(analysis: AnalysisResult): List<SafetyCheck> {
        val checks = mutableListOf<SafetyCheck>()

        if (analysis.scene.doors.isEmpty()) {
            checks.add(
                SafetyCheck(
                    category = "Accessibility",
                    description = "No doors detected in scene",
                    passed = false,
                    severity = SafetySeverity.HIGH,
                    penaltyPoints = 20
                )
            )
        } else {
            analysis.scene.doors.forEach { door ->
                val isWideEnough = door.width >= 0.9f // Estándar de accesibilidad
                checks.add(
                    SafetyCheck(
                        category = "Accessibility",
                        description = "${door.id}: Width ${String.format("%.2f", door.width)}m " +
                                if (isWideEnough) "(✓ Accessible)" else "(✗ Too narrow)",
                        passed = isWideEnough,
                        severity = if (isWideEnough) SafetySeverity.NONE else SafetySeverity.MEDIUM,
                        penaltyPoints = if (isWideEnough) 0 else 10
                    )
                )
            }
        }

        return checks
    }

    private fun checkWindows(analysis: AnalysisResult): List<SafetyCheck> {
        val checks = mutableListOf<SafetyCheck>()

        if (analysis.scene.windows.isEmpty()) {
            checks.add(
                SafetyCheck(
                    category = "Ventilation",
                    description = "No windows detected - Poor natural ventilation",
                    passed = false,
                    severity = SafetySeverity.MEDIUM,
                    penaltyPoints = 15
                )
            )
        } else {
            checks.add(
                SafetyCheck(
                    category = "Ventilation",
                    description = "${analysis.scene.windows.size} window(s) detected - Good ventilation",
                    passed = true,
                    severity = SafetySeverity.NONE,
                    penaltyPoints = 0
                )
            )
        }

        return checks
    }

    private fun checkObstacles(analysis: AnalysisResult): List<SafetyCheck> {
        val checks = mutableListOf<SafetyCheck>()

        // Detectar objetos que puedan ser obstáculos en rutas
        val potentialObstacles = analysis.scene.objects.filter { obj ->
            obj.objectClass.lowercase() in listOf("chair", "table", "cabinet", "box")
        }

        if (potentialObstacles.size > 10) {
            checks.add(
                SafetyCheck(
                    category = "Mobility",
                    description = "${potentialObstacles.size} objects detected - May obstruct movement",
                    passed = false,
                    severity = SafetySeverity.LOW,
                    penaltyPoints = 5
                )
            )
        } else {
            checks.add(
                SafetyCheck(
                    category = "Mobility",
                    description = "Clear pathways - Good mobility",
                    passed = true,
                    severity = SafetySeverity.NONE,
                    penaltyPoints = 0
                )
            )
        }

        return checks
    }

    private fun checkEmergencyExits(analysis: AnalysisResult): List<SafetyCheck> {
        val checks = mutableListOf<SafetyCheck>()

        val doorCount = analysis.scene.doors.size

        if (doorCount < 1) {
            checks.add(
                SafetyCheck(
                    category = "Emergency",
                    description = "No emergency exits available",
                    passed = false,
                    severity = SafetySeverity.CRITICAL,
                    penaltyPoints = 30
                )
            )
        } else if (doorCount == 1) {
            checks.add(
                SafetyCheck(
                    category = "Emergency",
                    description = "Single exit - Consider additional escape routes",
                    passed = false,
                    severity = SafetySeverity.MEDIUM,
                    penaltyPoints = 10
                )
            )
        } else {
            checks.add(
                SafetyCheck(
                    category = "Emergency",
                    description = "$doorCount exits available - Good emergency access",
                    passed = true,
                    severity = SafetySeverity.NONE,
                    penaltyPoints = 0
                )
            )
        }

        return checks
    }

    private fun getSafetyLevel(score: Float): SafetyLevel {
        return when {
            score >= 90 -> SafetyLevel.EXCELLENT
            score >= 75 -> SafetyLevel.GOOD
            score >= 60 -> SafetyLevel.MODERATE
            score >= 40 -> SafetyLevel.POOR
            else -> SafetyLevel.CRITICAL
        }
    }

    private fun generateRecommendations(checks: List<SafetyCheck>): List<String> {
        val recommendations = mutableListOf<String>()

        val failedChecks = checks.filter { !it.passed }

        if (failedChecks.any { it.category == "Accessibility" }) {
            recommendations.add("✓ Widen doorways to at least 0.9m for wheelchair access")
        }

        if (failedChecks.any { it.category == "Ventilation" }) {
            recommendations.add("✓ Install windows or ventilation systems")
        }

        if (failedChecks.any { it.category == "Emergency" }) {
            recommendations.add("✓ Add emergency exit routes")
            recommendations.add("✓ Install emergency signage")
        }

        if (failedChecks.any { it.category == "Mobility" }) {
            recommendations.add("✓ Reorganize furniture for clearer pathways")
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✓ Space meets all safety standards")
            recommendations.add("✓ Maintain current accessibility features")
        }

        return recommendations
    }
}
