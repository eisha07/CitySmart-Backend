package edu.skku.cs.citysmart.network

import edu.skku.cs.citysmart.domain.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// 1. The exact JSON body expected for a NEW proposal
data class IngestRequest(val user_prompt: String)

// 2. The exact JSON body expected for an UPDATED proposal (for later)
data class AmendRequest(
    val project_id: String,
    val version_tag: String,
    val amended_proposal_text: String
)

interface CitySmartApi {
    @GET("/api/v1/simulation/state")
    suspend fun getSimulationState(): UrbanSimulationState

    @GET("/api/v1/simulation/telemetry")
    suspend fun getTelemetry(): SpatialTelemetryCollection

    // 🚀 THE NEW GENESIS ENDPOINT
    @POST("/api/v1/projects/ingest")
    suspend fun ingestProposal(@Body request: IngestRequest): UrbanSimulationState

    // 🛠️ THE AMENDMENT ENDPOINT (Ready for when you build the edit feature!)
    @POST("/api/v1/projects/amend")
    suspend fun amendProposal(@Body request: AmendRequest): UrbanSimulationState
}