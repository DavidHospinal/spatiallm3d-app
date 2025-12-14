package com.spatiallm3d.data.source

import androidx.compose.runtime.Composable

/**
 * Desktop (JVM) implementation of ResourceLoaderFactory.
 *
 * No parameters needed - uses ClassLoader internally.
 */
actual object ResourceLoaderFactory {
    @Composable
    actual fun create(): ResourceLoader {
        return ResourceLoader()
    }
}
