package com.spatiallm3d.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of FilePicker using Swing JFileChooser.
 */
actual class FilePicker actual constructor() {
    actual suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Select PLY File"
                    fileFilter = FileNameExtensionFilter("PLY Files (*.ply)", "ply")
                    isAcceptAllFileFilterUsed = false
                }

                val result = fileChooser.showOpenDialog(null)

                if (result == JFileChooser.APPROVE_OPTION) {
                    val selectedFile = fileChooser.selectedFile

                    if (selectedFile.exists() && selectedFile.extension.equals("ply", ignoreCase = true)) {
                        val content = selectedFile.readBytes()
                        onResult(FilePickerResult.Success(selectedFile.absolutePath, content))
                    } else {
                        onResult(FilePickerResult.Error("Invalid file: must be a .ply file"))
                    }
                } else {
                    onResult(FilePickerResult.Cancelled)
                }
            } catch (e: Exception) {
                onResult(FilePickerResult.Error("File picker error: ${e.message}"))
            }
        }
    }
}
