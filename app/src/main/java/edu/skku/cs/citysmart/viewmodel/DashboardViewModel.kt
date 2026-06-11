package edu.skku.cs.citysmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import edu.skku.cs.citysmart.domain.*
import edu.skku.cs.citysmart.network.IngestRequest
import edu.skku.cs.citysmart.network.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel : ViewModel() {

    private val _simulationState = MutableStateFlow<UrbanSimulationState?>(null)
    val simulationState: StateFlow<UrbanSimulationState?> = _simulationState.asStateFlow()

    private val _telemetry = MutableStateFlow<SpatialTelemetryCollection?>(null)
    val telemetry: StateFlow<SpatialTelemetryCollection?> = _telemetry.asStateFlow()

    private val _projectTitle = MutableStateFlow<String?>(null)
    val projectTitle: StateFlow<String?> = _projectTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showFallbackOption = MutableStateFlow(false)
    val showFallbackOption: StateFlow<Boolean> = _showFallbackOption.asStateFlow()

    private var lastUserPrompt: String = ""

    fun updateServerIp(ip: String) {
        NetworkClient.updateBaseUrl(ip)
    }

    fun resetState() {
        _simulationState.value = null
        _telemetry.value = null
        _projectTitle.value = null
        _errorMessage.value = null
        _showFallbackOption.value = false
        _isLoading.value = false
        lastUserPrompt = ""
    }

    private fun getProxySimulationState(userPrompt: String): UrbanSimulationState {
        val gson = Gson()
        
        val mockAgents = listOf(
            PersonaMetadata(
                name = "Aisha", 
                iconTag = "👩", 
                shortDescription = "Daily commuter focusing on safety and lighting.", 
                demographics = "Female, 24, G-9 Resident, Software Engineer", 
                characteristics = listOf("Prioritizes lighting", "Sidewalk focus")
            ),
            PersonaMetadata(
                name = "Tariq", 
                iconTag = "🛺", 
                shortDescription = "Rickshaw driver focused on maneuverability.", 
                demographics = "Male, 42, Blue Area Route, 15yr Driver", 
                characteristics = listOf("Turning radius focus", "Loading zone needs")
            ),
            PersonaMetadata(
                name = "Zahid", 
                iconTag = "🏪", 
                shortDescription = "Shopkeeper concerned about customer accessibility.", 
                demographics = "Male, 55, Shopkeeper in G-9 Markaz", 
                characteristics = listOf("Frontage access focus", "Parking focus")
            ),
            PersonaMetadata(
                name = "Mr. Khan", 
                iconTag = "👴", 
                shortDescription = "Retired citizen advocating for senior mobility.", 
                demographics = "Male, 68, Retired Civil Servant", 
                characteristics = listOf("Level surface needs", "Quiet zone focus")
            )
        )

        val sampleFeatures = listOf(
            GeoJsonFeature(
                geometry = GeometryData("Point", gson.toJsonTree(listOf(73.055, 33.707))),
                properties = TelemetryProperties("proposed_infrastructure", 0.0f, "station_alpha", agentName = "G-9 Central Multi-Modal Terminal")
            ),
            GeoJsonFeature(
                geometry = GeometryData("Point", gson.toJsonTree(listOf(73.054, 33.706))),
                properties = TelemetryProperties("female_commuter", 1.8f, "agent_0", agentName = "Aisha")
            ),
            GeoJsonFeature(
                geometry = GeometryData("Point", gson.toJsonTree(listOf(73.052, 33.708))),
                properties = TelemetryProperties("qingqi_driver", 0.8f, "agent_1", agentName = "Tariq")
            ),
            GeoJsonFeature(
                geometry = GeometryData("Point", gson.toJsonTree(listOf(73.057, 33.706))),
                properties = TelemetryProperties("shopkeeper", 0.0f, "agent_2", agentName = "Zahid")
            ),
            GeoJsonFeature(
                geometry = GeometryData("Point", gson.toJsonTree(listOf(73.053, 33.709))),
                properties = TelemetryProperties("senior_resident", 0.0f, "agent_3", agentName = "Mr. Khan")
            ),
            GeoJsonFeature(
                geometry = GeometryData("Point", gson.toJsonTree(listOf(73.057, 33.709))),
                properties = TelemetryProperties("vulnerable_demographic", 2.2f, "bottleneck_1", agentName = "Intersection Conflict Point")
            )
        )

        // --- DYNAMIC MULTI-POLICY EXTRACTION (RESPONSIVE TO PROMPT) ---
        val policies = mutableListOf<BlueprintRevision>()
        val p = userPrompt.lowercase()

        // 1. CORE INTENT
        val coreLabel = if (userPrompt.isBlank()) "Urban Connectivity Node" else userPrompt.split(" ").take(6).joinToString(" ") + "..."
        policies.add(BlueprintRevision("R1", "CORE PROPOSAL: $coreLabel", 
            "HELPFUL: Engineering agents verify this core alignment establishes the necessary transit backbone for the sector.", 
            "VALIDATED: Move to detailed planning.", "FEASIBLE", "POSITIVE"))

        // 2. SECURITY (Active Deliberation)
        policies.add(BlueprintRevision("R2", "SECURITY & ILLUMINATION", 
            "NOT HELPFUL: Baseline design lacks specified lighting nodes. Aisha flagged 60% of connecting paths as 'High Risk' zones.", 
            "REVISION: Deploy 12 solar-powered 4000K LED masts at 15m intervals.", "CONFLICT", "NEGATIVE"))

        // 3. TRANSIT MANEUVERABILITY (Reactive)
        if (p.contains("wall") || p.contains("divider") || p.contains("block")) {
            policies.add(BlueprintRevision("R3", "BARRIER PERMEABILITY", 
                "NOT HELPFUL: High concrete dividers prevent rickshaw U-turns. Tariq reports this forces a 1.2km detour, causing major gridlock.", 
                "REVISION: Replace rigid dividers with low-profile modular bollards.", "CONFLICT", "NEGATIVE"))
        } else {
            policies.add(BlueprintRevision("R3", "LANE SEGREGATION", 
                "HELPFUL: Standard markings allow for high-speed BRT flow while maintaining local lane accessibility.", 
                "OPTIMAL: No amendments needed.", "OPTIMAL", "POSITIVE"))
        }

        // 4. ACCESSIBILITY (Reactive)
        if (p.contains("bridge") || p.contains("crossing") || p.contains("up")) {
            policies.add(BlueprintRevision("R4", "VERTICAL CONNECTIVITY", 
                "NOT HELPFUL: 10% ramp incline detected. Mr. Khan flagged this as prohibitive for elderly and wheelchair users.", 
                "REVISION: Recalibrate gradient to 4.5% and install mechanized lift modules.", "CONFLICT", "NEGATIVE"))
        } else {
            policies.add(BlueprintRevision("R4", "SURFACE UNIFORMITY", 
                "HELPFUL: Proposed removal of curb lips and implementation of tactile paving will assist 100% of seniors.", 
                "OPTIMAL: Design exceeds baseline standards.", "OPTIMAL", "POSITIVE"))
        }

        // 5. ECONOMIC VIABILITY
        policies.add(BlueprintRevision("R5", "COMMERCIAL FRONTAGE ACCESS", 
            "NOT HELPFUL: Construction footprint blocks 4 shop entrances in the Markaz. Zahid predicts a 40% revenue drop during build.", 
            "REVISION: Instate 'Phased Clear-Span' construction with temporary pedestrian gangways.", "CONFLICT", "NEGATIVE"))

        // 6. SOCIAL GATHERING SPACE
        policies.add(BlueprintRevision("R6", "COMMUNITY NODES", 
            "HELPFUL: Design includes 2 shaded seating areas. Agents note this improves social cohesion indices by 18%.", 
            "VALIDATED: Engineering specs approved.", "FEASIBLE", "POSITIVE"))

        return UrbanSimulationState(
            projectId = "proxy-demo-id",
            summaryVerdict = "DYNAMIC STAKEHOLDER EVALUATION: \"$userPrompt\"\n- 6 Complex policies extracted and analyzed.\n- 3 Conflicts identified requiring user intervention.\n- Active agent deliberation engaged across 4 demographics.",
            scores = FeasibilityScores(78.5f, 62.0f, 55.0f),
            liveDebateTicks = listOf(
                LiveDebateTick("10:00:05", "System", "admin", "Simulation initialized. Extracting multi-vector policies from prompt...", "INFO"),
                LiveDebateTick("10:01:15", "Aisha", "female_commuter", "I can see my pin on the map. But look at Card R2, the lighting is still missing!", "WARNING"),
                LiveDebateTick("10:02:10", "Tariq", "qingqi_driver", "Card R3 is the problem for me. If those walls stay, I'll block the whole Markaz. Change it to bollards!", "CRITICAL"),
                LiveDebateTick("10:03:00", "Zahid", "shopkeeper", "My customers can't reach me in R5. We need more than one fix here!", "WARNING")
            ),
            blueprintRevisions = policies,
            spatialTelemetry = SpatialTelemetryCollection(features = sampleFeatures),
            activeAgents = mockAgents
        )
    }

    fun triggerFallback(onSimulationReady: () -> Unit) {
        val proxy = getProxySimulationState(lastUserPrompt)
        _projectTitle.value = "OFFLINE PROXY: Dynamic Extraction"
        _simulationState.value = proxy
        _telemetry.value = proxy.spatialTelemetry
        _showFallbackOption.value = false
        _errorMessage.value = null
        onSimulationReady()
    }

    fun submitGenesisPrompt(prompt: String, onSimulationReady: () -> Unit) {
        lastUserPrompt = prompt
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _showFallbackOption.value = false
            try {
                val ingestRequest = IngestRequest(user_prompt = prompt)
                val ingestResponse = NetworkClient.api.ingestProposal(ingestRequest)
                _projectTitle.value = ingestResponse.extracted_metadata.title
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

    /**
     * Active User Participation: Updates the policy and adds real-time agent reactions.
     */
    fun amendPolicy(revisionId: String, newSuggestion: String) {
        _simulationState.value?.let { current ->
            val updatedRevisions = current.blueprintRevisions.map { rev ->
                if (rev.revisionId == revisionId) {
                    rev.copy(
                        amendedDesignFix = "USER OVERRIDE: $newSuggestion",
                        feasibilityStatus = "FEASIBLE",
                        agentSentiment = "POSITIVE"
                    )
                } else rev
            }
            
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val reactionTick = LiveDebateTick(time, "System", "admin", 
                "USER ACTION DETECTED: Policy $revisionId updated with custom override. recalculating stakeholder feasibility...", "INFO")
            
            val agentReaction = when(revisionId) {
                "R2" -> LiveDebateTick(time, "Aisha", "female_commuter", "Thank you! With that lighting fix, the paths will be much safer for everyone.", "INFO")
                "R3" -> LiveDebateTick(time, "Tariq", "qingqi_driver", "Finally! Flexible bollards mean I can keep the traffic moving. Good call.", "INFO")
                else -> LiveDebateTick(time, "Zahid", "shopkeeper", "This user fix helps my frontage access concerns. The project is looking much better.", "INFO")
            }

            _simulationState.value = current.copy(
                blueprintRevisions = updatedRevisions,
                liveDebateTicks = current.liveDebateTicks + reactionTick + agentReaction
            )
        }
    }
}
