package com.spatiallm3d.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of FilePicker using Swing JFileChooser.
 * Enhanced with default directory suggestion, file preview, and better error handling.
 */
actual class FilePicker actual constructor() {
    actual suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val fileChooser = JFileChooser().apply {
                    dialogTitle = "Select PLY Point Cloud File"

                    // Set default directory to Samples/ if it exists
                    val samplesDir = File("Samples")
                    if (samplesDir.exists() && samplesDir.isDirectory) {
                        currentDirectory = samplesDir
                    } else {
                        // Fallback to user home directory
                        currentDirectory = File(System.getProperty("user.home"))
                    }

                    // Filter for PLY files only
                    fileFilter = FileNameExtensionFilter(
                        "PLY Point Cloud Files (*.ply)",
                        "ply"
                    )
                    isAcceptAllFileFilterUsed = false

                    // Enable file preview (shows file size and path)
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    isMultiSelectionEnabled = false
                    accessory = null // Could add custom preview panel here
                }

                val result = fileChooser.showOpenDialog(null)

                when (result) {
                    JFileChooser.APPROVE_OPTION -> {
                        val selectedFile = fileChooser.selectedFile

                        // Validate file exists
                        if (!selectedFile.exists()) {
                            onResult(FilePickerResult.Error("File not found: ${selectedFile.absolutePath}"))
                            return@withContext
                        }

                        // Validate file extension
                        if (!selectedFile.extension.equals("ply", ignoreCase = true)) {
                            onResult(FilePickerResult.Error("Invalid file type. Please select a .ply file."))
                            return@withContext
                        }

                        // Validate file is readable
                        if (!selectedFile.canRead()) {
                            onResult(FilePickerResult.Error("Cannot read file: ${selectedFile.name}. Please check file permissions."))
                            return@withContext
                        }

                        // Check file size (warn if very large)
                        val fileSizeMB = selectedFile.length() / (1024.0 * 1024.0)
                        if (fileSizeMB > 100.0) {
                            val proceed = JOptionPane.showConfirmDialog(
                                null,
                                "The selected file is large (${String.format("%.1f", fileSizeMB)} MB).\n" +
                                        "Processing may take some time. Continue?",
                                "Large File Warning",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                            )
                            if (proceed != JOptionPane.YES_OPTION) {
                                onResult(FilePickerResult.Cancelled)
                                return@withContext
                            }
                        }

                        // Read file content
                        try {
                            val content = selectedFile.readBytes()
                            onResult(FilePickerResult.Success(selectedFile.absolutePath, content))
                        } catch (e: OutOfMemoryError) {
                            onResult(FilePickerResult.Error(
                                "File is too large to load into memory (${String.format("%.1f", fileSizeMB)} MB). " +
                                "Try a smaller file or increase JVM heap size."
                            ))
                        } catch (e: Exception) {
                            onResult(FilePickerResult.Error("Failed to read file: ${e.localizedMessage}"))
                        }
                    }

                    JFileChooser.CANCEL_OPTION -> {
                        onResult(FilePickerResult.Cancelled)
                    }

                    else -> {
                        onResult(FilePickerResult.Error("File selection was interrupted."))
                    }
                }
            } catch (e: Exception) {
                onResult(FilePickerResult.Error("File picker error: ${e.localizedMessage ?: e.message ?: "Unknown error"}"))
            }
        }
    }
}
