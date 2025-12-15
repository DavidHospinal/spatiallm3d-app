package com.spatiallm3d.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * iOS implementation of rememberPlyFilePicker.
 *
 * Uses the traditional FilePicker with UIDocumentPickerViewController.
 */
@Composable
actual fun rememberPlyFilePicker(): PlyFilePicker {
    val filePicker = remember { FilePicker() }
    val scope = rememberCoroutineScope()

    return object : PlyFilePicker {
        override fun pickFile(onResult: (FilePickerResult) -> Unit) {
            scope.launch {
                filePicker.pickPlyFile(onResult)
            }
        }
    }
}
