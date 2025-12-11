package com.spatiallm3d.platform

/**
 * Result of a file picking operation.
 */
sealed class FilePickerResult {
    data class Success(val path: String, val content: ByteArray) : FilePickerResult()
    data class Error(val message: String) : FilePickerResult()
    data object Cancelled : FilePickerResult()
}

/**
 * Multiplatform file picker for selecting PLY files.
 *
 * Implementations:
 * - Desktop: Swing JFileChooser
 * - Android: Activity Result API
 * - iOS: UIDocumentPickerViewController
 * - Web: HTML5 file input
 */
expect class FilePicker() {
    /**
     * Opens a file picker dialog to select a PLY file.
     *
     * @param onResult Callback invoked with the file content or error
     */
    suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit)
}
