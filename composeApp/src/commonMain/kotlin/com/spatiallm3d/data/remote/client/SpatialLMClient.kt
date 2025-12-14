package com.spatiallm3d.data.remote.client

import com.spatiallm3d.data.remote.dto.AnalysisRequestDto
import com.spatiallm3d.data.remote.dto.AnalysisResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HTTP client for communicating with the SpatialLM backend API.
 *
 * Handles network requests to the FastAPI backend deployed on Render.
 * Uses Ktor client with JSON serialization.
 */
class SpatialLMClient(
    private val baseUrl: String = "https://spatiallm3d-backend.onrender.com"
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 60_000
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    /**
     * Sends an analysis request to the backend API.
     *
     * @param request Analysis request parameters
     * @return AnalysisResponseDto with detection results
     * @throws Exception if network request fails
     */
    suspend fun analyzeScene(request: AnalysisRequestDto): AnalysisResponseDto {
        return httpClient.post("$baseUrl/api/v1/analyze") {
            setBody(request)
        }.body()
    }

    /**
     * Get pre-computed analysis results by scene ID.
     *
     * This method allows fetching results without uploading the entire PLY file.
     * Useful for demo/contest mode with known scenes.
     *
     * @param sceneId Scene identifier without .ply extension (e.g., "scene0000_00")
     * @return AnalysisResponseDto with pre-computed data from backend
     * @throws ClientRequestException if scene not found (404)
     * @throws Exception if network request fails
     */
    suspend fun getPrecomputedResult(sceneId: String): AnalysisResponseDto {
        println("SpatialLMClient: Fetching pre-computed result for $sceneId")
        return httpClient.get("$baseUrl/api/v1/precomputed/$sceneId").body()
    }

    /**
     * Checks if the backend API is healthy and reachable.
     *
     * @return true if API responds with healthy status
     */
    suspend fun healthCheck(): Boolean {
        return try {
            val response = httpClient.get("$baseUrl/health")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Closes the HTTP client and releases resources.
     */
    fun close() {
        httpClient.close()
    }
}
