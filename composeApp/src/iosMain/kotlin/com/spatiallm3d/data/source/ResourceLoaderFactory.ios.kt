package com.spatiallm3d.data.source

import androidx.compose.runtime.Composable

/**
 * iOS implementation of ResourceLoaderFactory.
 *
 * No parameters needed - uses NSBundle.mainBundle internally.
 */
actual object ResourceLoaderFactory {
    @Composable
    actual fun create(): ResourceLoader {
        return ResourceLoader()
    }
}
