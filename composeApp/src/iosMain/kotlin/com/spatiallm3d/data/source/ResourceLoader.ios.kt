package com.spatiallm3d.data.source

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.posix.memcpy

/**
 * Implementación iOS de ResourceLoader.
 *
 * Carga archivos desde resources usando NSBundle.mainBundle.
 */
@OptIn(ExperimentalForeignApi::class)
actual class ResourceLoader {

    actual suspend fun loadString(path: String): String = withContext(Dispatchers.Default) {
        try {
            val bundle = NSBundle.mainBundle
            val parts = path.split("/")
            val filename = parts.last().substringBeforeLast(".")
            val extension = parts.last().substringAfterLast(".")

            // Construir subdirectorio si existe
            val subdirectory = if (parts.size > 1) {
                parts.dropLast(1).joinToString("/")
            } else null

            val resourcePath = bundle.pathForResource(
                name = filename,
                ofType = extension,
                inDirectory = subdirectory
            ) ?: throw ResourceLoadException("Resource not found: $path")

            NSString.stringWithContentsOfFile(
                path = resourcePath,
                encoding = NSUTF8StringEncoding,
                error = null
            ) as? String ?: throw ResourceLoadException("Failed to read: $path")

        } catch (e: Exception) {
            throw ResourceLoadException("Failed to load resource: $path", e)
        }
    }

    actual suspend fun loadBytes(path: String): ByteArray = withContext(Dispatchers.Default) {
        try {
            val bundle = NSBundle.mainBundle
            val parts = path.split("/")
            val filename = parts.last().substringBeforeLast(".")
            val extension = parts.last().substringAfterLast(".")

            // Construir subdirectorio si existe
            val subdirectory = if (parts.size > 1) {
                parts.dropLast(1).joinToString("/")
            } else null

            val resourcePath = bundle.pathForResource(
                name = filename,
                ofType = extension,
                inDirectory = subdirectory
            ) ?: throw ResourceLoadException("Resource not found: $path")

            val data = NSData.create(contentsOfFile = resourcePath)
                ?: throw ResourceLoadException("Failed to read bytes: $path")

            ByteArray(data.length.toInt()).apply {
                usePinned {
                    memcpy(it.addressOf(0), data.bytes, data.length)
                }
            }

        } catch (e: Exception) {
            throw ResourceLoadException("Failed to load resource bytes: $path", e)
        }
    }
}
