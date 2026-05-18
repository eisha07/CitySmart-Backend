import json
from vertexai.generative_models import GenerativeModel

class UrbanAgentSimulationEngine:
    def __init__(self):
        # Target Gemini 1.5 Pro to manage the complex semantic boundaries of 10 concurrent personas
        self.model = GenerativeModel("gemini-1.5-pro")

    async def discover_dynamic_stakeholders(self, document_context: str) -> list[dict]:
        """Analyzes the project proposal text and dynamically extracts exactly 10 highly differentiated citizen profiles."""
        discovery_prompt = f"""
        You are a principal social impact anthropologist specializing in South Asian urban development.
        Analyze this raw infrastructure proposal text:
        
        "{document_context[:5000]}"
        
        Identify exactly 10 highly differentiated citizen personas who will experience direct impacts, utility shifts, or daily friction due to this design layout.
        You MUST search across diverse demographics: include formal commuters, informal transit operators (qingqi/rickshaw drivers), street vendors (khokha owners), elderly or disabled residents, night-shift workers, women traveling alone, schoolchildren, local storefront merchants, sanitation crews, and nearby residential property owners.
        
        For each of these 10 distinct profiles, construct a comprehensive, deep first-person system instruction configuration that forces an LLM to roleplay as them flawlessly, maintaining their personal constraints, vocabulary, economic realities, and daily anxieties.
        
        OUTPUT FORMAT: You MUST return a valid, clean JSON list containing exactly 10 objects with the keys "name", "type", and "system_instruction". Do not add markdown code block backticks, explanations, or trailing commentary.
        """
        response = await self.model.generate_content_async(discovery_prompt)
        raw_text = response.text.strip().replace("```json", "").replace("```", "")
        stakeholder_matrix = json.loads(raw_text)
        return stakeholder_matrix[:10]

    async def simulate_dynamic_critique(self, system_instruction: str, context_chunks: list[str]) -> str:
        """Commands Gemini to roleplay using one of the 10 dynamically extracted citizen profile configurations."""
        combined_context = "\n\n--- Context Segment ---\n".join(context_chunks)
        user_prompt = f"""
        Review this infrastructure design data:
        {combined_context}
        Identify exactly 2 critical personal vulnerabilities, structural dangers, or operational constraints this layout forces onto your day-to-day survival.
        """
        agent_session = GenerativeModel("gemini-1.5-pro", system_instruction=system_instruction)
        response = await agent_session.generate_content_async(user_prompt)
        return response.text

    async def run_consensus_negotiation(self, critiques_list: list[dict]) -> dict:
        """Arbitrates between the comprehensive multi-persona array to synthesize scores and generate optimal design fixes."""
        formatted_critiques = "\n\n".join([f"PROFILE: {c['name']} ({c['type']})\nCRITIQUE: {c['text']}" for c in critiques_list])
        
        arbitration_prompt = f"""
        You are an elite urban planning arbitrator specializing in developing civic infrastructure stability.
        Review this 10-persona community feedback matrix representing real local populations:
        
        {formatted_critiques}
        
        Synthesize these diverse citizen viewpoints. Balance public demographic safety concerns against commercial utility, pedestrian accessibility, and informal transit flows.
        Output your evaluation strictly in a JSON object format containing the exact keys specified below. No markdown wrapping.
        
        REQUIRED JSON OUTPUT FORMAT:
        {{
            "social_impact_score": 0.0 to 100.0,
            "economic_viability_score": 0.0 to 100.0,
            "political_feasibility_score": 0.0 to 100.0,
            "summary_verdict": "Clear synthesis text detailing systemic liabilities or community wins.",
            "blueprint_revisions": [
                {{
                    "original_element": "Description of official proposed element",
                    "failure_mode_detected": "Why it fails or who it harms",
                    "amended_design_fix": "Concrete engineering or structural compromise fix"
                }}
            ]
        }}
        """
        response = await self.model.generate_content_async(arbitration_prompt)
        raw_text = response.text.strip().replace("```json", "").replace("```", "")
        return json.loads(raw_text)

    async def interrogate_persona(self, system_instruction: str, history: list, user_message: str) -> str:
        """Maintains an active, stateful conversation stream inside any chosen citizen's dynamic persona block."""
        chat_agent = GenerativeModel("gemini-1.5-pro", system_instruction=system_instruction)
        chat = chat_agent.start_chat()
        for msg in history:
            if msg.get('role') == 'user':
                chat.send_message(msg.get('content', ''))
        response = await chat.send_message_async(user_message)
        return response.text

simulation_engine = UrbanAgentSimulationEngine()
