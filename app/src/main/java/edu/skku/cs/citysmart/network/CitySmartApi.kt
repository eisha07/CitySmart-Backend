/**
 * UPDATED: Added interrogateAgent and renderConcept endpoints. 
 * Migrated amendProposal to use the new ProjectAmendmentRequest model from the domain package.
 */
package edu.skku.cs.citysmart.network

import edu.skku.cs.citysmart.domain.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class IngestRequest(val user_prompt: String)

data class IngestResponse(
    val status: String,
    val extracted_metadata: ExtractedMetadata
)

data class ExtractedMetadata(
    val project_id: String,
    val title: String
)

interface CitySmartApi {
    @GET("/api/v1/simulation/{project_id}/state")
    suspend fun getSimulationState(@Path("project_id") projectId: String): UrbanSimulationState

    // 🚀 INGESTION: Returns metadata + project_id
    @POST("/api/v1/projects/ingest")
    suspend fun ingestProposal(@Body request: IngestRequest): IngestResponse

    @POST("/api/v1/projects/amend")
    suspend fun amendProposal(@Body request: ProjectAmendmentRequest): UrbanSimulationState

    // 💬 CHAT INTERROGATION: Deep dive with specific agent personas
    @POST("/api/v1/chat/interrogate")
    suspend fun interrogateAgent(@Body request: ChatInterrogationRequest): ChatResponse

    // 🎨 CONCEPT RENDERING: Generate visual interpretations of design elements
    @POST("/api/v1/visual/render")
    suspend fun renderConcept(@Body request: ConceptRenderRequest): ConceptRenderResponse
}
