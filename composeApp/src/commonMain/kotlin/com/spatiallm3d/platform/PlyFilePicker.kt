package com.spatiallm3d.platform

import androidx.compose.runtime.Composable

/**
 * Common interface for PLY file picking across all platforms.
 *
 * This interface provides a unified API for file picking that works
 * on Android, Desktop, iOS, and Web.
 */
interface PlyFilePicker {
    /**
     * Opens a file picker and invokes the callback with the result.
     *
     * @param onResult Callback invoked with Success, Error, or Cancelled result
     */
    fun pickFile(onResult: (FilePickerResult) -> Unit)
}

/**
 * Creates and remembers a platform-specific PlyFilePicker.
 *
 * This is an expect/actual function that provides the correct implementation
 * for each platform:
 * - Android: Uses ActivityResultContract with ContentResolver
 * - Desktop: Uses Swing JFileChooser
 * - iOS: Uses UIDocumentPickerViewController
 * - Web: Uses HTML5 file input
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     val filePicker = rememberPlyFilePicker()
 *
 *     Button(
 *         onClick = {
 *             filePicker.pickFile { result ->
 *                 when (result) {
 *                     is FilePickerResult.Success -> {
 *                         // Handle: result.path, result.content
 *                     }
 *                     is FilePickerResult.Error -> {
 *                         // Handle: result.message
 *                     }
 *                     FilePickerResult.Cancelled -> {
 *                         // User cancelled
 *                     }
 *                 }
 *             }
 *         }
 *     ) {
 *         Text("Pick PLY File")
 *     }
 * }
 * ```
 */
@Composable
expect fun rememberPlyFilePicker(): PlyFilePicker
