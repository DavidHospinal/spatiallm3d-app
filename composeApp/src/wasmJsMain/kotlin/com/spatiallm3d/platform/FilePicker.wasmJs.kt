package com.spatiallm3d.platform

/**
 * Web implementation of FilePicker using HTML5 file input.
 *
 * Full implementation will be added when web target is fully tested.
 */
actual class FilePicker actual constructor() {
    actual suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit) {
        // TODO: Implement using HTML5 file input and FileReader API
        onResult(FilePickerResult.Error("File picker not yet implemented for Web"))
    }
}
