package com.spatiallm3d.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spatiallm3d.platform.FilePickerResult
import com.spatiallm3d.platform.rememberPlyFilePicker
import kotlinx.coroutines.launch

/**
 * Home screen with options to analyze scenes.
 *
 * Supports analyzing sample scenes or uploading custom PLY files.
 *
 * @param onAnalyzeClick Callback invoked when the analyze sample button is clicked
 * @param onFileSelected Callback invoked when a PLY file is selected (content, filename)
 */
@Composable
fun HomeScreen(
    onAnalyzeClick: () -> Unit,
    onFileSelected: ((ByteArray, String?) -> Unit)? = null
) {
    // Use platform-specific file picker
    val plyFilePicker = rememberPlyFilePicker()
    val scope = rememberCoroutineScope()
    var isPickingFile by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SpatialLM3D",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Accessibility & Safety Assistant for Home",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Converting 3D point clouds into actionable insights",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Sample scene button
            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                enabled = !isPickingFile
            ) {
                Text(
                    text = "Analyze Sample Scene",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // File picker button (works on all platforms)
            OutlinedButton(
                onClick = {
                    isPickingFile = true
                    errorMessage = null
                    plyFilePicker.pickFile { result ->
                        isPickingFile = false
                        when (result) {
                            is FilePickerResult.Success -> {
                                // Extract filename from path
                                val filename = result.path.substringAfterLast("/")
                                    .substringAfterLast("\\") // Handle Windows paths
                                println("HomeScreen: File selected: $filename (${result.content.size} bytes)")
                                onFileSelected?.invoke(result.content, filename)
                            }
                            is FilePickerResult.Error -> {
                                errorMessage = result.message
                            }
                            FilePickerResult.Cancelled -> {
                                // User cancelled
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                enabled = !isPickingFile
            ) {
                Text(
                    text = if (isPickingFile) "Opening file picker..." else "Load Custom PLY File",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Powered by SpatialLM | CC-BY-NC-4.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
