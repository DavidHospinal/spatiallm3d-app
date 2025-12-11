package com.spatiallm3d.platform

/**
 * Android implementation of FilePicker using Activity Result API.
 *
 * Note: Requires Activity context and proper permission handling.
 * Full implementation will be added when Android target is active.
 */
actual class FilePicker actual constructor() {
    actual suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit) {
        // TODO: Implement using Activity Result API
        // For now, return not implemented error
        onResult(FilePickerResult.Error("File picker not yet implemented for Android"))
    }
}
