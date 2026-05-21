package edu.skku.cs.citysmart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.skku.cs.citysmart.domain.*
import edu.skku.cs.citysmart.network.IngestRequest
import edu.skku.cs.citysmart.network.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import org.json.JSONObject

class DashboardViewModel : ViewModel() {

    private val _simulationState = MutableStateFlow<UrbanSimulationState?>(null)
    val simulationState: StateFlow<UrbanSimulationState?> = _simulationState.asStateFlow()

    private val _telemetry = MutableStateFlow<SpatialTelemetryCollection?>(null)
    val telemetry: StateFlow<SpatialTelemetryCollection?> = _telemetry.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showFallbackOption = MutableStateFlow(false)
    val showFallbackOption: StateFlow<Boolean> = _showFallbackOption.asStateFlow()

    fun updateServerIp(ip: String) {
        NetworkClient.updateBaseUrl(ip)
    }

    /**
     * Resets the entire simulation state to allow for a fresh policy ingestion.
     */
    fun resetState() {
        _simulationState.value = null
        _telemetry.value = null
        _errorMessage.value = null
        _showFallbackOption.value = false
        _isLoading.value = false
    }

    private fun getProxySimulationState(): UrbanSimulationState {
        // COORDINATES clustered around Islamabad Centaurus (73.05, 33.70)
        val sampleFeatures = listOf(
            // 1. PROPOSED STRUCTURE (White Box)
            GeoJsonFeature(
                geometry = GeometryData("LineString", listOf(
                    listOf(73.055, 33.707),
                    listOf(73.057, 33.707),
                    listOf(73.057, 33.709),
                    listOf(73.055, 33.709),
                    listOf(73.055, 33.707)
                )),
                properties = TelemetryProperties("proposed_infrastructure", 0.0f, "station_alpha", "Station Alpha")
            ),
            // 2. COMMUTER PATH (Magenta)
            GeoJsonFeature(
                geometry = GeometryData("LineString", listOf(listOf(73.054, 33.706), listOf(73.058, 33.710))),
                properties = TelemetryProperties("female_commuter", 1.8f, "agent_1", "Aisha (Commuter)")
            ),
            // 3. TRANSIT FLOW (Cyan)
            GeoJsonFeature(
                geometry = GeometryData("LineString", listOf(listOf(73.052, 33.708), listOf(73.060, 33.708))),
                properties = TelemetryProperties("qingqi_driver", 0.8f, "agent_2", "Tariq (Driver)")
            ),
            // 4. CRITICAL PIN (Red Marker)
            GeoJsonFeature(
                geometry = GeometryData("Point", listOf(listOf(73.056, 33.708))),
                properties = TelemetryProperties("vulnerable_demographic", 2.2f, "bottleneck_1", "Pedestrian Friction Point")
            )
        )

        return UrbanSimulationState(
            projectId = "proxy-demo-id",
            summaryVerdict = "SYSTEM OFFLINE: Utilizing Proxy Telemetry.\n- Pedestrian safety risks identified in the G-9 sector.\n- Current barriers obstruct the path for small transit vehicles like qingqis and rickshaws.\n- Inadequate nighttime lighting poses a significant security challenge.",
            scores = FeasibilityScores(78.5f, 62.0f, 55.0f),
            liveDebateTicks = listOf(
                LiveDebateTick("10:00:05", "System", "admin", "Simulation shuru ho chuki hai. Sab data process ho raha hai.", "INFO"),
                LiveDebateTick("10:01:12", "Aisha", "female_commuter", "Yar, station ke paas roshni bohat kam hai. Raat ko bohat darr lagta hai nikalnay main.", "WARNING"),
                LiveDebateTick("10:02:45", "Tariq", "qingqi_driver", "Agar yeh dividers pakkay hain toh mera bohat nuqsan hoga. Rickshaw murnay ki jagah hi nahi bache gi.", "CRITICAL"),
                LiveDebateTick("10:03:30", "Aisha", "female_commuter", "Tariq bhai theek keh rahay hain, in barriers ki wajah se logon ko road ke beech main ana parta hai.", "INFO")
            ),
            blueprintRevisions = listOf(
                BlueprintRevision(
                    revisionId = "R1", 
                    originalElement = "High-profile concrete road dividers (1.5m).", 
                    failureModeDetected = "Turning radius insufficient for local rickshaws and qingqi drivers.", 
                    amendedDesignFix = "Replace with low-profile modular bollards to improve vehicle maneuverability.",
                    feasibilityStatus = "CONFLICT",
                    agentSentiment = "NEGATIVE"
                ),
                BlueprintRevision(
                    revisionId = "R2", 
                    originalElement = "Dedicated BRT Lane in Saddar core.", 
                    failureModeDetected = "None", 
                    amendedDesignFix = "None",
                    feasibilityStatus = "FEASIBLE",
                    agentSentiment = "POSITIVE"
                ),
                BlueprintRevision(
                    revisionId = "R3", 
                    originalElement = "Standard ground-level crosswalks.", 
                    failureModeDetected = "High risk of accidents during low-visibility night hours.", 
                    amendedDesignFix = "Install overhead pedestrian bridges with solar-powered illumination.",
                    feasibilityStatus = "CONFLICT",
                    agentSentiment = "NEGATIVE"
                ),
                BlueprintRevision(
                    revisionId = "R4", 
                    originalElement = "Solar-powered smart bus stations.", 
                    failureModeDetected = "None", 
                    amendedDesignFix = "None",
                    feasibilityStatus = "FEASIBLE",
                    agentSentiment = "POSITIVE"
                )
            ),
            spatialTelemetry = SpatialTelemetryCollection(features = sampleFeatures)
        )
    }

    fun triggerFallback(onSimulationReady: () -> Unit) {
        val proxy = getProxySimulationState()
        _simulationState.value = proxy
        _telemetry.value = proxy.spatialTelemetry
        _showFallbackOption.value = false
        _errorMessage.value = null
        onSimulationReady()
    }

    fun submitGenesisPrompt(prompt: String, onSimulationReady: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _showFallbackOption.value = false

            try {
                val ingestRequest = IngestRequest(user_prompt = prompt)
                val ingestResponse = NetworkClient.api.ingestProposal(ingestRequest)
                val projectId = ingestResponse.extracted_metadata.project_id

                val state = NetworkClient.api.getSimulationState(projectId)
                
                _simulationState.value = state
                _telemetry.value = state.spatialTelemetry ?: SpatialTelemetryCollection(features = emptyList())
                
                onSimulationReady()

            } catch (e: Exception) {
                _errorMessage.value = "Simulation Link Interrupted: ${e.message}"
                _showFallbackOption.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }
}
