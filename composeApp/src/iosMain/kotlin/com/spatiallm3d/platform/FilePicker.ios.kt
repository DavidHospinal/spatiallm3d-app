package com.spatiallm3d.platform

/**
 * iOS implementation of FilePicker using UIDocumentPickerViewController.
 *
 * Full implementation will be added when iOS target is active.
 */
actual class FilePicker actual constructor() {
    actual suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit) {
        // TODO: Implement using UIDocumentPickerViewController
        onResult(FilePickerResult.Error("File picker not yet implemented for iOS"))
    }
}
