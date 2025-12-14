package com.spatiallm3d.data.source

import androidx.compose.runtime.Composable

/**
 * Platform-specific factory for creating ResourceLoader instances.
 *
 * Each platform implements this differently:
 * - Android: Needs Context from LocalContext.current
 * - iOS/Desktop/Web: No parameters needed
 *
 * This is a @Composable function to allow Android to access LocalContext.
 */
expect object ResourceLoaderFactory {
    @Composable
    fun create(): ResourceLoader
}
