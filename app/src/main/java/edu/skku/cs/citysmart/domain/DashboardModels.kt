package edu.skku.cs.citysmart.domain

import com.google.gson.annotations.SerializedName

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
 * Step 10: OVERARCHING URBAN DASHBOARD MASTER STATE
 * The single master wrapper that unites every piece of your spec together.
 */
data class UrbanSimulationState(
    @SerializedName("project_id") val projectId: String,
    @SerializedName("summary_verdict") val summaryVerdict: String,
    @SerializedName("scores") val scores: FeasibilityScores,
    @SerializedName("live_debate_ticks") val liveDebateTicks: List<LiveDebateTick>,
    @SerializedName("blueprint_revisions") val blueprintRevisions: List<BlueprintRevision>
)