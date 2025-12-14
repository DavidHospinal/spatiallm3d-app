package com.spatiallm3d.data.source

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación Android de ResourceLoader.
 *
 * Carga archivos desde src/main/resources usando AssetManager.
 */
actual class ResourceLoader(private val context: Context) {

    actual suspend fun loadString(path: String): String = withContext(Dispatchers.IO) {
        try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            throw ResourceLoadException("Failed to load resource: $path", e)
        }
    }

    actual suspend fun loadBytes(path: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            context.assets.open(path).use { it.readBytes() }
        } catch (e: Exception) {
            throw ResourceLoadException("Failed to load resource bytes: $path", e)
        }
    }
}

class ResourceLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)
