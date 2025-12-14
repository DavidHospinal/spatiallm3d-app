package com.spatiallm3d.data.source

/**
 * Platform-specific resource loader for loading files from application resources.
 *
 * Each platform implements this differently:
 * - Android: Uses AssetManager
 * - iOS: Uses NSBundle.mainBundle
 * - Desktop: Uses ClassLoader.getResourceAsStream
 */
expect class ResourceLoader {
    suspend fun loadString(path: String): String
    suspend fun loadBytes(path: String): ByteArray
}
