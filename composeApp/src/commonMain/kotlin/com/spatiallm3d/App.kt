package com.spatiallm3d

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spatiallm3d.data.parser.PlyParser
import com.spatiallm3d.data.remote.client.SpatialLMClient
import com.spatiallm3d.data.repository.MlRepositoryImpl
import com.spatiallm3d.presentation.navigation.Screen
import com.spatiallm3d.presentation.screens.AnalysisScreen
import com.spatiallm3d.presentation.screens.HomeScreen
import com.spatiallm3d.presentation.screens.ResultsScreenWithVisualization
import com.spatiallm3d.presentation.viewmodel.SceneState
import com.spatiallm3d.presentation.viewmodel.SceneViewModel

/**
 * Main application entry point.
 *
 * Manages navigation between screens and coordinates the ViewModel.
 */
@Composable
fun App() {
    MaterialTheme {
        // Initialize dependencies
        val client = remember { SpatialLMClient() }
        val repository = remember { MlRepositoryImpl(client) }
        val viewModel = remember { SceneViewModel(repository) }

        // Observe ViewModel state
        val sceneState by viewModel.sceneState.collectAsStateWithLifecycle()

        // Navigation state
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

        // Navigate based on scene state
        LaunchedEffect(sceneState) {
            when (sceneState) {
                is SceneState.Loading -> currentScreen = Screen.Analysis
                is SceneState.Success -> currentScreen = Screen.Results
                is SceneState.Error -> currentScreen = Screen.Home
                is SceneState.Idle -> { /* Stay on current screen */ }
            }
        }

        // Display current screen
        when (currentScreen) {
            Screen.Home -> {
                HomeScreen(
                    onAnalyzeClick = {
                        viewModel.analyzeScene(
                            pointCloudUrl = "sample_scene.ply"
                        )
                    },
                    onFileSelected = { plyContent ->
                        try {
                            println("PLY file received: ${plyContent.size} bytes")
                            val pointCloud = PlyParser.parseDownsampled(plyContent, maxPoints = 50000)
                            println("PLY parsed: ${pointCloud.points.size} points")

                            if (pointCloud.points.isEmpty()) {
                                println("ERROR: PointCloud is empty after parsing")
                                viewModel.resetState()
                                currentScreen = Screen.Home
                            } else {
                                println("Analyzing point cloud...")
                                viewModel.analyzeLocalPointCloud(pointCloud)
                            }
                        } catch (e: Exception) {
                            println("ERROR parsing PLY: ${e.message}")
                            e.printStackTrace()
                            viewModel.resetState()
                            currentScreen = Screen.Home
                        }
                    }
                )
            }

            Screen.Analysis -> {
                AnalysisScreen()
            }

            Screen.Results -> {
                val result = (sceneState as? SceneState.Success)?.result
                if (result != null) {
                    ResultsScreenWithVisualization(
                        result = result,
                        onBackToHome = {
                            viewModel.resetState()
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }
    }
}