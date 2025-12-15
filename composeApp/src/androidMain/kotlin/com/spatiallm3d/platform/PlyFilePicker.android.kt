package com.spatiallm3d.platform

import androidx.compose.runtime.Composable

/**
 * Android implementation of rememberPlyFilePicker.
 *
 * Returns an AndroidPlyFilePicker wrapped in PlyFilePicker interface.
 * Uses ActivityResultContract for proper integration with Android's Storage Access Framework.
 */
@Composable
actual fun rememberPlyFilePicker(): PlyFilePicker {
    val androidPicker = rememberAndroidPlyFilePicker()

    return object : PlyFilePicker {
        override fun pickFile(onResult: (FilePickerResult) -> Unit) {
            androidPicker.pick(onResult)
        }
    }
}
