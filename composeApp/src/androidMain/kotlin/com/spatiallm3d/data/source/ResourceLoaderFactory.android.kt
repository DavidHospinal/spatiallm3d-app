package com.spatiallm3d.data.source

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable

/**
 * Android implementation of ResourceLoaderFactory.
 *
 * Uses LocalContext to get the Android Context at the composition level.
 */
actual object ResourceLoaderFactory {
    @Composable
    actual fun create(): ResourceLoader {
        val context = LocalContext.current
        return ResourceLoader(context)
    }
}
