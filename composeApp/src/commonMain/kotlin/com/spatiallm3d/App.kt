package com.spatiallm3d

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spatiallm3d.data.parser.PlyParser
import com.spatiallm3d.data.remote.client.SpatialLMClient
import com.spatiallm3d.data.repository.DataMode
import com.spatiallm3d.data.repository.MlRepositoryImpl
import com.spatiallm3d.data.source.DemoDataSource
import com.spatiallm3d.data.source.ResourceLoaderFactory
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

        // Create ResourceLoader (platform-specific)
        val resourceLoader = ResourceLoaderFactory.create()

        // Create DemoDataSource for loading local assets
        val demoDataSource = remember(resourceLoader) {
            DemoDataSource(resourceLoader)
        }

        // Data mode state (DEMO for sample scenes, BACKEND for custom files)
        var dataMode by remember { mutableStateOf(DataMode.DEMO) }

        // Create Repository with dynamic mode
        val repository = remember(client, demoDataSource, dataMode) {
            MlRepositoryImpl(
                client = client,
                demoDataSource = demoDataSource,
                dataMode = dataMode
            )
        }

        val viewModel = remember(repository) { SceneViewModel(repository) }

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
                        // Use DEMO mode for sample scene
                        dataMode = DataMode.DEMO
                        viewModel.analyzeScene(
                            pointCloudUrl = "sample_scene.ply"
                        )
                    },
                    onFileSelected = { plyContent, filename ->
                        try {
                            println("PLY file received: $filename (${plyContent.size} bytes)")

                            // Switch to BACKEND mode for custom file uploads
                            dataMode = DataMode.BACKEND
                            println("Switched to BACKEND mode for custom file analysis")

                            val pointCloud = PlyParser.parseDownsampled(
                                content = plyContent,
                                maxPoints = 50000,
                                filename = filename
                            )
                            println("PLY parsed: ${pointCloud.points.size} points, filename: ${pointCloud.filename}")

                            if (pointCloud.points.isEmpty()) {
                                println("ERROR: PointCloud is empty after parsing")
                                viewModel.resetState()
                                currentScreen = Screen.Home
                            } else {
                                println("Analyzing point cloud with backend...")
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