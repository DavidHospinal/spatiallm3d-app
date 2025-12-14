package com.spatiallm3d.data.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación Desktop (JVM) de ResourceLoader.
 *
 * Carga archivos desde resources usando ClassLoader.
 */
actual class ResourceLoader {

    actual suspend fun loadString(path: String): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = this::class.java.classLoader.getResourceAsStream(path)
                ?: throw ResourceLoadException("Resource not found: $path")

            inputStream.bufferedReader().use { it.readText() }

        } catch (e: Exception) {
            throw ResourceLoadException("Failed to load resource: $path", e)
        }
    }

    actual suspend fun loadBytes(path: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            val inputStream = this::class.java.classLoader.getResourceAsStream(path)
                ?: throw ResourceLoadException("Resource not found: $path")

            inputStream.use { it.readBytes() }

        } catch (e: Exception) {
            throw ResourceLoadException("Failed to load resource bytes: $path", e)
        }
    }
}

actual class ResourceLoadException actual constructor(message: String, cause: Throwable?) : Exception(message, cause) {
    actual constructor(message: String) : this(message, null)
}
