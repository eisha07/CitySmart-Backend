package edu.skku.cs.citysmart.domain

import com.google.gson.annotations.SerializedName

// ── Prompt 2: Persona Metadata ────────────────────────────────────────────────
/**
 * Rich profile object for each active agent persona.
 * Drives the (ⓘ) icon popup modal in the Jetpack Compose agent list.
 * Maps 1-to-1 with the backend PersonaMetadata Pydantic schema.
 */
data class PersonaMetadata(
    @SerializedName("name")              val name: String,
    @SerializedName("icon_tag")          val iconTag: String = "\uD83D\uDC64",  // 👤 fallback
    @SerializedName("short_description") val shortDescription: String,
    @SerializedName("characteristics")   val characteristics: List<String>
)

/**
 * Steps 5 & 6: GEOSPATIAL & MAP DATA MODELS
 * Captures coordinates and custom telemetry friction intensities for neon heatmaps.
 */
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
    @SerializedName("type") val type: String, // "LineString" or "Point"
    @SerializedName("coordinates") val coordinates: List<List<Double>> // Pairs of [Longitude, Latitude]
)

data class TelemetryProperties(
    @SerializedName("agent_profile") val agentProfile: String, // "female_commuter" or "qingqi_driver"
    @SerializedName("friction_intensity") val frictionIntensity: Float // Scales from 0.00 to 1.99
)

/**
 * Step 7: LIVE-DEBATE TICKER DATA MODELS
 * Feeds the concurrent split-screen stream for Aisha and Tariq.
 */
data class LiveDebateTick(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("agent_name") val agentName: String,
    @SerializedName("agent_profile") val agentProfile: String, // "female_commuter" or "qingqi_driver"
    @SerializedName("message_text") val messageText: String,
    @SerializedName("alert_level") val alertLevel: String // "INFO", "WARNING", "CRITICAL"
)

/**
 * Step 8: GENERATIVE BLUEPRINT REVISION DATA MODELS
 * Handles the structural "Before & After" schematic carousel details.
 */
data class BlueprintRevision(
    @SerializedName("revision_id") val revisionId: String,
    @SerializedName("original_element") val originalElement: String,
    @SerializedName("failure_mode_detected") val failureModeDetected: String,
    @SerializedName("amended_design_fix") val amendedDesignFix: String
)

/**
 * Step 9: ANALYTICAL SUMMARY & SCORECARD DATA MODELS
 * Captures the radial circular architectural progress bar values.
 */
data class FeasibilityScores(
    @SerializedName("social_acceptance") val socialAcceptance: Float,       // 0.0 to 100.0
    @SerializedName("economic_roi") val economicRoi: Float,                 // 0.0 to 100.0
    @SerializedName("political_justification") val politicalJustification: Float // 0.0 to 100.0
)

/**
 * OVERARCHING URBAN DASHBOARD MASTER STATE
 * The single master wrapper that unites every piece of the spec.
 *
 * Prompt 1: [summaryPoints] replaces [summaryVerdict] as the primary display field.
 *   Each element is one independent bullet rendered in a LazyColumn.
 * Prompt 2: [activeAgents] provides full PersonaMetadata for every active agent
 *   so the Compose (\u24d8) info-modal always has structured characteristic data.
 * All new fields are nullable / defaulted for backward compatibility.
 */
data class UrbanSimulationState(
    @SerializedName("project_id")        val projectId: String,
    // Legacy single-string verdict — kept for backward compat
    @SerializedName("summary_verdict")   val summaryVerdict: String? = null,
    @SerializedName("scores")            val scores: FeasibilityScores? = null,
    @SerializedName("live_debate_ticks") val liveDebateTicks: List<LiveDebateTick> = emptyList(),
    @SerializedName("blueprint_revisions") val blueprintRevisions: List<BlueprintRevision> = emptyList(),

    // ── Prompt 1: Structured mediator bullet points ─────────────────────────
    @SerializedName("summary_points")    val summaryPoints: List<String> = emptyList(),

    // ── Prompt 2: Rich persona metadata for agent info-modals ───────────────
    @SerializedName("active_agents")     val activeAgents: List<PersonaMetadata>? = null
)