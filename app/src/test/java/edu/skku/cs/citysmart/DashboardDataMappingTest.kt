package edu.skku.cs.citysmart

import com.google.gson.Gson
import edu.skku.cs.citysmart.domain.UrbanSimulationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DashboardDataMappingTest {

    @Test
    fun verifyDashboardSpecJsonParsing() {
        // Simulated JSON chunk exactly mimicking your partner's FastAPI specs
        val mockJsonString = """
            {
              "project_id": "saddar-bazaar-simulation",
              "summary_verdict": "The layout requires structural revisions due to severe vendor displacement.",
              "scores": {
                "social_acceptance": 84.5,
                "economic_roi": 42.0,
                "political_justification": 70.0
              },
              "live_debate_ticks": [
                {
                  "timestamp": "12:00:00",
                  "agent_name": "Aisha",
                  "agent_profile": "female_commuter",
                  "message_text": "Load-shedding blackouts caught in grid zone.",
                  "alert_level": "CRITICAL"
                }
              ],
              "blueprint_revisions": [
                {
                  "revision_id": "REV-001",
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
        assertEquals(42.0f, parsedState.scores.economicRoi)
        assertEquals("CRITICAL", parsedState.liveDebateTicks[0].alertLevel)
        assertEquals("REV-001", parsedState.blueprintRevisions[0].revisionId)
    }
}
