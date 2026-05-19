import json
from vertexai.generative_models import GenerativeModel, Content, Part
from vertexai.preview.vision_models import ImageGenerationModel


class UrbanAgentSimulationEngine:
    def __init__(self):
        # Initialize as None for lazy loading to prevent GoogleAuthError during module imports
        self._model = None
        self._imagen_model = None

    @property
    def model(self):
        if self._model is None:
            self._model = GenerativeModel("gemini-1.5-pro")
        return self._model

    @property
    def imagen_model(self):
        if self._imagen_model is None:
            self._imagen_model = ImageGenerationModel.from_pretrained(
                "imagen-3.0-generate-002"
            )
        return self._imagen_model

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

    async def simulate_dynamic_critique(
        self, system_instruction: str, context_chunks: list[str]
    ) -> str:
        """Commands Gemini to roleplay using one of the 10 dynamically extracted citizen profile configurations."""
        combined_context = "\n\n--- Context Segment ---\n".join(context_chunks)
        user_prompt = f"""
        Review this infrastructure design data:
        {combined_context}
        Identify exactly 2 critical personal vulnerabilities, structural dangers, or operational constraints this layout forces onto your day-to-day survival.
        """
        agent_session = GenerativeModel(
            "gemini-1.5-pro", system_instruction=system_instruction
        )
        response = await agent_session.generate_content_async(user_prompt)
        return response.text

    async def run_consensus_negotiation(self, critiques_list: list[dict]) -> dict:
        """Arbitrates between the comprehensive multi-persona array to synthesize scores and generate optimal design fixes."""
        formatted_critiques = "\n\n".join(
            [
                f"PROFILE: {c['name']} ({c['type']})\nCRITIQUE: {c['text']}"
                for c in critiques_list
            ]
        )

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

    async def interrogate_persona(
        self, system_instruction: str, history: list[dict], user_message: str
    ) -> str:
        """Maintains an active, stateful dialogue inside a specific citizen's semantic roleplay boundary."""
        # 1. Instantiate the model with the exact system instruction persona block
        chat_agent = GenerativeModel(
            "gemini-1.5-pro", system_instruction=system_instruction
        )

        # 2. Reconstruct the chat history parameters safely using Vertex AI Content objects
        formatted_history = []
        for msg in history:
            role = "user" if msg.get("role") == "user" else "model"
            formatted_history.append(
                Content(role=role, parts=[Part.from_text(text=msg.get("content", ""))])
            )

        # 3. Spin up an active chat session seeded with the historical logs
        chat_session = chat_agent.start_chat(history=formatted_history)

        # 4. Stream transmission asynchronously out to the cloud broker
        response = await chat_session.send_message_async(user_message)
        return response.text

    async def render_architectural_concept(self, element: str, context: str) -> dict:
        """Leverages Imagen 3 to generate a visual mockup of a proposed urban design amendment."""
        # 1. Synthesize a professional, non-hallucinatory prompt using Gemini
        refinement_prompt = f"""
        Transform this urban design modification into a descriptive, professional architectural visualization prompt for an image generation model.
        MODIFICATION: {element}
        CONTEXT: {context}
        
        The output prompt must focus on architectural precision, urban layout clarity, street infrastructure, safety features, and realistic lighting. Avoid buzzwords like 'photorealistic' or 'stunning'.
        OUTPUT FORMAT: Return only the plain optimized prompt string. No markdown, no quotes.
        """
        prompt_response = await self.model.generate_content_async(refinement_prompt)
        optimized_prompt = prompt_response.text.strip()

        # 2. Invoke the Imagen 3 model to generate the concept image
        result = self.imagen_model.generate_images(
            prompt=optimized_prompt,
            number_of_images=1,
            aspect_ratio="1:1",
            guidance_scale=12.0,
            safety_filter_level="block_medium_and_above",
        )

        # 3. Handle saving the generated byte array to storage
        generated_image = result.images[0]
        # In a full staging pipeline, you upload this file to Google Cloud Storage (GCS).
        # We will structure a reliable asset tracking URL pattern for the frontend canvas layer.
        simulated_storage_url = (
            f"/static/assets/renders/concept_{hash(optimized_prompt) & 0xffffffff}.png"
        )

        # Save bytes locally for staging verification
        generated_image.save(
            location=f"app{simulated_storage_url}", include_generation_parameters=False
        )

        return {"url": simulated_storage_url, "prompt": optimized_prompt}


# Re-instantiate the engine cleanly
simulation_engine = UrbanAgentSimulationEngine()

# Lazy GenAI Client wrapper to support the google-genai SDK at request time
from google import genai
from app.core.config import settings


class LazyGenAIClient:
    def __init__(self):
        self._client = None

    @property
    def client(self):
        if self._client is None:
            import os

            api_key = settings.GEMINI_API_KEY or os.environ.get("GEMINI_API_KEY")
            if api_key:
                self._client = genai.Client(api_key=api_key)
            else:
                self._client = genai.Client(
                    vertexai=True,
                    project=settings.GCP_PROJECT_ID,
                    location=settings.GCP_REGION,
                )
        return self._client

    @property
    def models(self):
        return self.client.models


client = LazyGenAIClient()
