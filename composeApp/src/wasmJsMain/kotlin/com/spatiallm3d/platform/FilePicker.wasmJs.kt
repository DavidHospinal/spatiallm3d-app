package com.spatiallm3d.platform

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume

/**
 * Web (WASM) implementation of FilePicker using HTML5 FileReader API.
 *
 * Creates a hidden file input element, triggers click programmatically,
 * and reads the selected PLY file using FileReader.
 */
actual class FilePicker actual constructor() {

    actual suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit) {
        try {
            val result = pickFileFromBrowser()
            onResult(result)
        } catch (e: Exception) {
            onResult(FilePickerResult.Error("File picker error: ${e.message ?: "Unknown error"}"))
        }
    }

    /**
     * Picks a file using HTML5 file input and FileReader API.
     */
    private suspend fun pickFileFromBrowser(): FilePickerResult = suspendCancellableCoroutine { continuation ->
        // Create hidden file input element
        val fileInput = document.createElement("input") as HTMLInputElement
        fileInput.type = "file"
        fileInput.accept = ".ply,application/octet-stream" // Accept PLY files
        fileInput.style.display = "none"

        // Handle file selection
        fileInput.onchange = { event ->
            val files = fileInput.files
            if (files == null || files.length == 0) {
                continuation.resume(FilePickerResult.Cancelled)
            } else {
                val file = files.item(0)
                if (file == null) {
                    continuation.resume(FilePickerResult.Cancelled)
                } else {
                    readFile(file, continuation)
                }
            }

            // Clean up: remove input element
            fileInput.remove()
        }

        // Handle cancellation (user closes dialog without selecting)
        fileInput.oncancel = {
            continuation.resume(FilePickerResult.Cancelled)
            fileInput.remove()
        }

        // Add to DOM temporarily and trigger click
        document.body?.appendChild(fileInput)
        fileInput.click()

        // Handle coroutine cancellation
        continuation.invokeOnCancellation {
            fileInput.remove()
        }
    }

    /**
     * Reads file content using FileReader API.
     */
    private fun readFile(
        file: File,
        continuation: kotlin.coroutines.Continuation<FilePickerResult>
    ) {
        val fileName = file.name

        // Validate file extension
        if (!fileName.endsWith(".ply", ignoreCase = true)) {
            continuation.resume(
                FilePickerResult.Error("Invalid file type. Please select a .ply file (selected: $fileName)")
            )
            return
        }

        // Check file size (warn if very large)
        val fileSizeMB = file.size / (1024.0 * 1024.0)
        if (fileSizeMB > 100.0) {
            val proceed = js("confirm('The selected file is large (' + fileSizeMB.toFixed(1) + ' MB). Processing may take time. Continue?')") as Boolean
            if (!proceed) {
                continuation.resume(FilePickerResult.Cancelled)
                return
            }
        }

        // Read file as ArrayBuffer
        val reader = FileReader()

        reader.onload = { event ->
            try {
                val arrayBuffer = reader.result as? ArrayBuffer
                if (arrayBuffer == null) {
                    continuation.resume(FilePickerResult.Error("Failed to read file content"))
                } else {
                    // Convert ArrayBuffer to ByteArray
                    val int8Array = Int8Array(arrayBuffer)
                    val byteArray = ByteArray(int8Array.length) { index ->
                        int8Array[index]
                    }

                    continuation.resume(FilePickerResult.Success(fileName, byteArray))
                }
            } catch (e: Exception) {
                continuation.resume(FilePickerResult.Error("Error processing file: ${e.message}"))
            }
        }

        reader.onerror = { event ->
            continuation.resume(
                FilePickerResult.Error("Failed to read file: ${reader.error?.message ?: "Unknown error"}")
            )
        }

        // Start reading file
        reader.readAsArrayBuffer(file)
    }
}

/**
 * Alternative implementation using modern File System Access API.
 * This is more powerful but has limited browser support (Chrome, Edge).
 * Kept as reference for future enhancement.
 */
private suspend fun pickFileWithFileSystemAccess(): FilePickerResult = suspendCancellableCoroutine { continuation ->
    js("""
        if ('showOpenFilePicker' in window) {
            window.showOpenFilePicker({
                types: [{
                    description: 'PLY Point Cloud Files',
                    accept: {
                        'application/octet-stream': ['.ply']
                    }
                }],
                multiple: false
            })
            .then(fileHandles => {
                if (fileHandles.length === 0) {
                    return Promise.reject('No file selected');
                }
                return fileHandles[0].getFile();
            })
            .then(file => {
                if (!file.name.toLowerCase().endsWith('.ply')) {
                    throw new Error('Invalid file type. Please select a .ply file.');
                }
                return file.arrayBuffer();
            })
            .then(arrayBuffer => {
                const int8Array = new Int8Array(arrayBuffer);
                const byteArray = Array.from(int8Array);
                // Success callback with file data
                return { name: file.name, data: byteArray };
            })
            .catch(error => {
                // Error callback
                throw error;
            });
        } else {
            throw new Error('File System Access API not supported in this browser');
        }
    """)

    // Note: This is pseudocode - actual implementation would need proper JS interop
    continuation.resume(FilePickerResult.Error("File System Access API integration not yet implemented"))
}
