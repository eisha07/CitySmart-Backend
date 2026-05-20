/**
 * UPDATED: Added ProjectAmendmentRequest, ChatInterrogationRequest, ChatResponse,
 * ConceptRenderRequest, and ChatMessage models to match the new FastAPI backend specification.
 */
package edu.skku.cs.citysmart.domain

import com.google.gson.annotations.SerializedName

data class SpatialTelemetryCollection(
    @SerializedName("type") val type: String = "FeatureCollection",
    @SerializedName("features") val features: List<GeoJsonFeature>
)

data class GeoJsonFeature(
    @SerializedName("type") val type: String = "Feature",
    @SerializedName("geometry") val geometry: GeometryData,
    @SerializedName("properties") val properties: TelemetryProperties
)

data class GeometryData(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<List<Double>>
)

data class TelemetryProperties(
    @SerializedName("profile") val agentProfile: String,
    @SerializedName("friction_intensity") val frictionIntensity: Float,
    @SerializedName("agent_id") val agentId: String = "",
    @SerializedName("lighting_vector_safety") val lightingSafety: String? = null
) {
    val profile: String get() = agentProfile
}

data class LiveDebateTick(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("agent_name") val agentName: String? = "",
    @SerializedName("agent_profile") val agentProfile: String?,
    @SerializedName("message") val messageText: String?,
    @SerializedName("log_level") val alertLevel: String?
) {
    val message: String? get() = messageText
    val logLevel: String? get() = alertLevel
}

data class BlueprintRevision(
    @SerializedName("revision_id") val revisionId: String = "",
    @SerializedName("original_element") val originalElement: String,
    @SerializedName("failure_mode_detected") val failureModeDetected: String,
    @SerializedName("amended_design_fix") val amendedDesignFix: String
)

data class FeasibilityScores(
    @SerializedName("social_impact_score") val socialAcceptance: Float,
    @SerializedName("economic_viability_score") val economicRoi: Float,
    @SerializedName("political_feasibility_score") val politicalJustification: Float
)

data class UrbanSimulationState(
    @SerializedName("project_id") val projectId: String,
    @SerializedName("summary_verdict") val summaryVerdict: String,
    @SerializedName("scores") val scores: FeasibilityScores,
    @SerializedName("live_debate_ticks") val liveDebateTicks: List<LiveDebateTick>,
    @SerializedName("blueprint_revisions") val blueprintRevisions: List<BlueprintRevision>,
    @SerializedName("spatial_telemetry") val spatialTelemetry: SpatialTelemetryCollection? = null
)

// NEW MODELS FOR FASTAPI SPECIFICATION

data class ProjectAmendmentRequest(
    @SerializedName("project_id") val projectId: String,
    @SerializedName("version_tag") val versionTag: String,
    @SerializedName("amended_proposal_text") val amendedProposalText: String
)

data class ChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class ChatInterrogationRequest(
    @SerializedName("project_id") val projectId: String,
    @SerializedName("persona_system_instruction") val personaSystemInstruction: String,
    @SerializedName("user_message") val userMessage: String,
    @SerializedName("chat_history") val chatHistory: List<ChatMessage>
)

data class ChatResponse(
    @SerializedName("reply") val reply: String
)

data class ConceptRenderRequest(
    @SerializedName("project_id") val projectId: String,
    @SerializedName("version_tag") val versionTag: String,
    @SerializedName("design_element_description") val designElementDescription: String,
    @SerializedName("environmental_context") val environmentalContext: String = "South Asian daylight context"
)

data class ConceptRenderResponse(
    @SerializedName("project_id") val projectId: String,
    @SerializedName("version_tag") val versionTag: String,
    @SerializedName("element_rendered") val elementRendered: String,
    @SerializedName("generated_image_url") val generatedImageUrl: String,
    @SerializedName("revised_prompt_used") val revisedPromptUsed: String
)
