import json
from google.cloud import aiplatform
from vertexai.generative_models import GenerativeModel
from app.services.llm_client import vertex_client
from app.models.database import DocumentInsightModel

class UrbanAgentSimulationEngine:
    def __init__(self):
        # Target Gemini 1.5 Pro to manage complex, multi-perspective roleplay boundaries
        self.model = GenerativeModel("gemini-1.5-pro")

    def _get_system_instruction_for_persona(self, persona_type: str) -> str:
        if persona_type == "female_commuter":
            return """
            You are 'Aisha', a 24-year-old software engineer commuting home at 9 PM through poorly lit transit junctions in Karachi/Islamabad. 
            Your analysis lens is strictly focused on:
            - Gendered Mobility Safety: Identifying dark walkways, blind corners behind overpasses, and isolated footbridges.
            - Footpath Integrity: Checking if sidewalks are blocked by construction or vehicle parking, forcing you into active traffic lanes.
            Provide your feedback raw, critical, and protective of public safety.
            """
        else:
            return """
            You are 'Tariq', a veteran Qingqi Rickshaw driver navigating local transit markets.
            Your analysis lens is strictly focused on:
            - Informal Congestion Nodes: Identifying points where official lanes squeeze your vehicle or conflict with street vendors (khokhas).
            - Economic Utility: Highlighting where prestige urban infrastructure ignores passenger boarding spots or cuts off access to local market bazaars.
            Provide your feedback pragmatic, raw, and focused on economic survival.
            """

    async def simulate_agent_critique(self, persona_type: str, context_chunks: list[str]) -> str:
        """Asynchronously commands Gemini to simulate a highly specific localized citizen persona."""
        system_prompt = self._get_system_instruction_for_persona(persona_type)
        combined_context = "\n\n--- Context Segment ---\n".join(context_chunks)
        
        user_prompt = f"""
        Analyze the following proposed urban infrastructure data segments through your specific lived experience.
        Identify exactly 2 critical failure modes or liabilities.
        
        DATA SEGMENTS:
        {combined_context}
        """
        
        # Instantiate model instance with persona parameters
        agent_session = GenerativeModel(
            "gemini-1.5-pro",
            system_instruction=system_prompt
        )
        
        response = await agent_session.generate_content_async(user_prompt)
        return response.text

    async def run_consensus_negotiation(self, commuter_feedback: str, driver_feedback: str) -> dict:
        """Runs an arbitration loop where Gemini balances conflicting agent perspectives to generate final urban scores."""
        arbitration_prompt = f"""
        You are an elite, neutral urban planning arbitrator specializing in Pakistani public infrastructure stability.
        Review these two conflicting citizen feedback reports regarding a proposed municipal expansion project:
        
        FEMALE COMMUTER PROFILE CRITIQUE:
        {commuter_feedback}
        
        INFORMAL QINGQI DRIVER PROFILE CRITIQUE:
        {driver_feedback}
        
        TASK:
        Synthesize these viewpoints. Balance public demographic safety concerns against informal commercial utility.
        Output your evaluation strictly in a JSON object format containing the exact keys specified below. Do not add markdown backticks or explanations.
        
        REQUIRED JSON OUTPUT FORMAT:
        {{
            "social_impact_score": 0.0 to 100.0,
            "economic_viability_score": 0.0 to 100.0,
            "political_feasibility_score": 0.0 to 100.0,
            "summary_verdict": "Clear synthesis text detailing if this project is a 'White Elephant' asset or an impactful design."
        }}
        """
        
        response = await self.model.generate_content_async(arbitration_prompt)
        
        # Clean potential markdown wrapping anomalies securely
        raw_text = response.text.strip().replace("```json", "").replace("```", "")
        return json.loads(raw_text)

simulation_engine = UrbanAgentSimulationEngine()
