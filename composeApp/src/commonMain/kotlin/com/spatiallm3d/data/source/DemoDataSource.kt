package com.spatiallm3d.data.source

import com.spatiallm3d.domain.model.AnalysisResult
import com.spatiallm3d.domain.model.DemoScene
import com.spatiallm3d.domain.model.DemoScenesManifest
import kotlinx.serialization.json.Json

/**
 * Data Source para el modo DEMO.
 *
 * Carga escenas precargadas desde resources locales.
 * NO requiere conexión a internet ni backend.
 */
class DemoDataSource(
    private val resourceLoader: ResourceLoader
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * Carga el manifiesto de escenas demo desde resources.
     */
    suspend fun loadDemoScenes(): List<DemoScene> {
        val manifestContent = resourceLoader.loadString("data/demo_metadata.json")
        val manifest = json.decodeFromString<DemoScenesManifest>(manifestContent)
        return manifest.scenes
    }

    /**
     * Carga los datos de análisis para una escena específica.
     */
    suspend fun loadSceneAnalysis(scene: DemoScene): AnalysisResult {
        val jsonContent = resourceLoader.loadString(scene.dataFile)

        // Parsear el JSON usando el formato pre-computado
        return json.decodeFromString<AnalysisResult>(jsonContent)
    }

    /**
     * Carga la imagen raw de una escena como ByteArray.
     *
     * Nota: En KMP, el manejo de imágenes varía por plataforma.
     * Este método retorna los bytes crudos para que cada plataforma
     * los convierta a su formato nativo (Bitmap, UIImage, etc.)
     */
    suspend fun loadSceneImage(scene: DemoScene): ByteArray {
        return resourceLoader.loadBytes(scene.rawImage)
    }
}

/**
 * Interfaz para cargar recursos desde assets.
 *
 * Cada plataforma implementará esto de forma específica:
 * - Android: AssetManager
 * - iOS: Bundle.main
 * - Desktop: ClassLoader.getResourceAsStream
 * - Web: fetch API
 */
expect class ResourceLoader() {
    suspend fun loadString(path: String): String
    suspend fun loadBytes(path: String): ByteArray
}
