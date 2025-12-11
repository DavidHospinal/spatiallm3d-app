package com.spatiallm3d.platform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of FilePicker using Activity Result API.
 *
 * Requires Android Activity context. Uses modern ActivityResultContract
 * for file selection with proper MIME type filtering.
 *
 * Note: This implementation requires the FilePicker to be constructed with
 * an Activity context and registered before onCreate() completes.
 */
actual class FilePicker actual constructor() {
    private var pendingCallback: ((FilePickerResult) -> Unit)? = null

    /**
     * Picks a PLY file using Android Storage Access Framework.
     *
     * This is a simplified implementation. For production use, you should:
     * 1. Pass Activity context to constructor
     * 2. Register ActivityResultLauncher in Activity.onCreate()
     * 3. Handle permissions properly
     */
    actual suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit) {
        // Since we don't have Activity context in this architecture,
        // we provide a functional placeholder implementation

        onResult(FilePickerResult.Error(
            "Android file picker requires Activity integration. " +
            "To implement:\n" +
            "1. Add Activity context to FilePicker constructor\n" +
            "2. Register ActivityResultContracts.GetContent()\n" +
            "3. Filter for 'application/octet-stream' or '*.ply'\n" +
            "4. Read file using ContentResolver.openInputStream()\n\n" +
            "Example code:\n" +
            "val launcher = registerForActivityResult(GetContent()) { uri ->\n" +
            "    uri?.let { readPlyFromUri(it) }\n" +
            "}\n" +
            "launcher.launch(\"application/octet-stream\")"
        ))
    }
}

/**
 * Extension function to read PLY file from Android URI.
 * This would be used in the full implementation.
 */
private suspend fun Activity.readPlyFromUri(uri: Uri, onResult: (FilePickerResult) -> Unit) {
    try {
        // Get file name
        val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "unknown.ply"

        // Validate extension
        if (!fileName.endsWith(".ply", ignoreCase = true)) {
            onResult(FilePickerResult.Error("Invalid file type. Please select a .ply file."))
            return
        }

        // Read file content
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            onResult(FilePickerResult.Error("Cannot read file. Please check file permissions."))
            return
        }

        val content = inputStream.use { it.readBytes() }
        onResult(FilePickerResult.Success(fileName, content))

    } catch (e: Exception) {
        onResult(FilePickerResult.Error("Failed to read file: ${e.localizedMessage}"))
    }
}

/**
 * Helper class for Activity Result Contract integration.
 * This would be used in MainActivity or a dedicated FilePicker Activity.
 */
class AndroidFilePicker(private val activity: ComponentActivity) {
    private var callback: ((FilePickerResult) -> Unit)? = null

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            callback?.invoke(FilePickerResult.Cancelled)
        } else {
            activity.readPlyFromUri(uri) { result ->
                callback?.invoke(result)
            }
        }
        callback = null
    }

    suspend fun pickPlyFile(): FilePickerResult = suspendCancellableCoroutine { continuation ->
        callback = { result ->
            continuation.resume(result)
        }

        // Launch file picker with MIME type filter
        // Android doesn't have a specific MIME type for PLY, so we use octet-stream
        launcher.launch("application/octet-stream")

        continuation.invokeOnCancellation {
            callback = null
        }
    }
}
