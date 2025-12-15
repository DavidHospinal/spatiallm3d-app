package com.spatiallm3d.platform

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of FilePicker using Activity Result API.
 *
 * On Android, use the Composable function `rememberPlyFilePicker()` instead
 * of constructing this class directly, as it handles ActivityResultLauncher
 * registration properly.
 *
 * Example:
 * ```
 * val filePicker = rememberPlyFilePicker()
 * Button(onClick = { filePicker.pick { result -> ... } }) { ... }
 * ```
 */
actual class FilePicker actual constructor() {
    /**
     * Not directly usable on Android - use rememberPlyFilePicker() Composable instead.
     */
    actual suspend fun pickPlyFile(onResult: (FilePickerResult) -> Unit) {
        onResult(
            FilePickerResult.Error(
                "On Android, use rememberPlyFilePicker() Composable instead of FilePicker().\n\n" +
                        "Example:\n" +
                        "val filePicker = rememberPlyFilePicker()\n" +
                        "filePicker.pick { result -> ... }"
            )
        )
    }
}

/**
 * Android-specific file picker that integrates with Compose and ActivityResultContract.
 *
 * Provides a simple API to pick PLY files and read their content using Android's
 * Storage Access Framework.
 */
class AndroidPlyFilePicker(
    private val context: Context,
    private val launcher: (String) -> Unit
) {
    /**
     * Launches the file picker to select a PLY file.
     *
     * @param onResult Callback invoked with the result (Success, Error, or Cancelled)
     */
    fun pick(onResult: (FilePickerResult) -> Unit) {
        pendingCallback = onResult
        // Launch with MIME type for binary files (PLY doesn't have specific MIME type)
        launcher("application/octet-stream")
    }

    /**
     * Internal callback storage.
     */
    private var pendingCallback: ((FilePickerResult) -> Unit)? = null

    /**
     * Handles the selected URI from the file picker.
     * Called internally by the ActivityResultLauncher.
     */
    internal fun handleUri(uri: Uri?) {
        val callback = pendingCallback ?: return
        pendingCallback = null

        if (uri == null) {
            callback(FilePickerResult.Cancelled)
            return
        }

        readPlyFromUri(uri, callback)
    }

    /**
     * Reads PLY file content from Android URI using ContentResolver.
     */
    private fun readPlyFromUri(uri: Uri, onResult: (FilePickerResult) -> Unit) {
        try {
            // Get file name from ContentResolver
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            } ?: "unknown.ply"

            // Validate file extension
            if (!fileName.endsWith(".ply", ignoreCase = true)) {
                onResult(
                    FilePickerResult.Error(
                        "Invalid file type: $fileName\n" +
                                "Please select a .ply file."
                    )
                )
                return
            }

            // Read file content using ContentResolver
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                onResult(
                    FilePickerResult.Error(
                        "Cannot read file.\n" +
                                "Please check file permissions and try again."
                    )
                )
                return
            }

            // Read all bytes from stream
            val content = inputStream.use { it.readBytes() }

            // Success - return file content
            onResult(FilePickerResult.Success(fileName, content))

        } catch (e: SecurityException) {
            onResult(
                FilePickerResult.Error(
                    "Permission denied.\n" +
                            "Please grant storage access and try again."
                )
            )
        } catch (e: Exception) {
            onResult(
                FilePickerResult.Error(
                    "Failed to read file: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
                )
            )
        }
    }
}

/**
 * Composable function that creates and remembers an AndroidPlyFilePicker.
 *
 * This is the Android-specific implementation for file picking in Compose.
 * It properly registers the ActivityResultLauncher and handles the file reading.
 *
 * Internal function - use `rememberPlyFilePicker()` from common code instead.
 *
 * @return AndroidPlyFilePicker instance ready to use
 */
@Composable
internal fun rememberAndroidPlyFilePicker(): AndroidPlyFilePicker {
    val context = LocalContext.current

    // Create the file picker instance (will be remembered)
    val filePicker = remember {
        AndroidPlyFilePicker(
            context = context,
            launcher = {} // Placeholder, will be replaced by launcher
        )
    }

    // Register ActivityResultLauncher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        filePicker.handleUri(uri)
    }

    // Return file picker with proper launcher
    return remember(launcher) {
        AndroidPlyFilePicker(
            context = context,
            launcher = { mimeType -> launcher.launch(mimeType) }
        )
    }
}
