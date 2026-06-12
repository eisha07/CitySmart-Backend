package edu.skku.cs.citysmart

import com.google.gson.Gson
import edu.skku.cs.citysmart.domain.UrbanSimulationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DashboardDataMappingTest {

    @Test
    fun verifyDashboardSpecJsonParsing() {
        // Updated JSON chunk to match the backend's SimulationStateResponse schema exactly
        val mockJsonString = """
            {
              "project_id": "saddar-bazaar-simulation",
              "summary_verdict": "The layout requires structural revisions due to severe vendor displacement.",
              "scores": {
                "social_impact_score": 84.5,
                "economic_viability_score": 42.0,
                "political_feasibility_score": 70.0
              },
              "live_debate_ticks": [
                {
                  "timestamp": "12:00:00",
                  "agent_profile": "female_commuter",
                  "message": "Load-shedding blackouts caught in grid zone.",
                  "log_level": "CRITICAL"
                }
              ],
              "blueprint_revisions": [
                {
                  "original_element": "Concrete barrier wall",
                  "failure_mode_detected": "Creates a severe rickshaw bottleneck.",
                  "amended_design_fix": "Introduce recessed utility slots."
                }
              ]
            }
        """.trimIndent()

        // Test the engine's conversion ability
        val parsedState = Gson().fromJson(mockJsonString, UrbanSimulationState::class.java)

        // Validations
        assertNotNull(parsedState)
        assertEquals("saddar-bazaar-simulation", parsedState.projectId)
        
        // economicRoi is mapped from economic_viability_score
        assertEquals(42.0f, parsedState.scores.economicRoi)
        
        // logLevel replaces alertLevel (mapped from log_level)
        assertEquals("CRITICAL", parsedState.liveDebateTicks[0].logLevel)
        
        // Verify message content
        assertEquals("Load-shedding blackouts caught in grid zone.", parsedState.liveDebateTicks[0].message)
        
        // Verify blueprint content (original_element mapped to originalElement)
        assertEquals("Concrete barrier wall", parsedState.blueprintRevisions[0].originalElement)
    }
}
