package com.spatiallm3d.domain.model

import kotlinx.serialization.Serializable

/**
 * Representa una escena precargada para el modo demo.
 *
 * Contiene referencias a los assets locales (imagen raw, JSON)
 * y metadata simulada del archivo PLY original.
 */
@Serializable
data class DemoScene(
    val id: String,
    val name: String,
    val description: String,
    val rawImage: String,          // Path relativo: "images/raw/scene0000_00.png"
    val dataFile: String,          // Path relativo: "data/scene0000_00_results.json"
    val metadata: SceneMetadata
)

@Serializable
data class SceneMetadata(
    val originalPlySize: String,   // "10.5 MB" - solo para UI
    val pointCount: Int,            // 50000
    val captureDate: String        // "2024-11-12"
)

@Serializable
data class DemoScenesManifest(
    val scenes: List<DemoScene>
)
