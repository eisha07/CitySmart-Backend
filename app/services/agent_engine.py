import json
import os
import re
import logging
from vertexai.generative_models import GenerativeModel, Content, Part
from vertexai.preview.vision_models import ImageGenerationModel
from google import genai
from google.genai import types
from app.core.config import settings

# Suppress noisy warning and info logs from the Google GenAI SDK
logging.getLogger("google_genai").setLevel(logging.ERROR)


# ── Mediator Output Parser ────────────────────────────────────────────────────────

def parse_summary_points(raw_text: str) -> list[str]:
    """
    Robustly extracts a clean ``list[str]`` from the Mediator Agent's raw text
    output, regardless of the format the LLM chose to use.

    Parsing strategy (attempted in order):
    1. Direct JSON array  – ``["Point 1", "Point 2"]``
    2. JSON object with a ``summary_points`` / ``points`` key.
    3. ``###`` delimiter splitting (explicit split token).
    4. Markdown bullet stripping  – lines starting with ``-``, ``*``, or ``•``.
    5. Numbered list stripping  – lines starting with ``1.``, ``2.`` …
    6. Double-newline paragraph splitting as last resort.

    All strategies strip empty strings and whitespace so the frontend
    never receives blank bullet entries.

    Args:
        raw_text: The raw string from the LLM response.

    Returns:
        A non-empty list of clean, stripped point strings.
        Returns ``[raw_text.strip()]`` as a safe single-item fallback
        if all strategies fail to produce multiple points.
    """
    text = raw_text.strip()
    if not text:
        return []

    # 1. Try parsing as a raw JSON array
    try:
        parsed = json.loads(text)
        if isinstance(parsed, list):
            points = [str(p).strip() for p in parsed if str(p).strip()]
            if points:
                return points
        # 2. JSON object with a known key
        if isinstance(parsed, dict):
            for key in ("summary_points", "points", "verdict_points", "bullets"):
                if key in parsed and isinstance(parsed[key], list):
                    points = [str(p).strip() for p in parsed[key] if str(p).strip()]
                    if points:
                        return points
    except (json.JSONDecodeError, ValueError):
        pass

    # Strip markdown code fences if present (```json ... ```)
    text = re.sub(r"```[\w]*\n?", "", text).strip()

    # 3. ### delimiter
    if "###" in text:
        points = [p.strip() for p in text.split("###") if p.strip()]
        if len(points) > 1:
            return points

    # 4. Markdown bullet lines (-, *, •)
    bullet_lines = re.findall(r"^[\-\*•]\s+(.+)", text, re.MULTILINE)
    if len(bullet_lines) > 1:
        return [line.strip() for line in bullet_lines if line.strip()]

    # 5. Numbered list lines  (1. / 1) / 1-)
    numbered_lines = re.findall(r"^\d+[\.)\-]\s+(.+)", text, re.MULTILINE)
    if len(numbered_lines) > 1:
        return [line.strip() for line in numbered_lines if line.strip()]

    # 6. Double-newline paragraph split
    paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
    if len(paragraphs) > 1:
        return paragraphs

    # Ultimate fallback: return the full text as a single-item list.
    return [text]


from app.services.llm_client import client


class UrbanAgentSimulationEngine:
    def __init__(self):
        self._imagen_model = None
        self.primary_model = "gemini-2.5-flash"
        self.fallback_models = ["gemini-2.5-flash", "gemini-3.1-flash-lite", "gemini-2.5-flash-lite"]

    async def _generate_with_fallback(self, contents, config=None, model_override=None):
        """Helper to manage model generation with automatic retries and fallback logic."""
        import asyncio
        import random

        # Build list of models to try
        models_to_try = list(self.fallback_models)
        if model_override:
            if model_override in models_to_try:
                # Reorder so model_override is first
                idx = models_to_try.index(model_override)
                models_to_try = [model_override] + models_to_try[:idx] + models_to_try[idx+1:]
            else:
                models_to_try = [model_override] + models_to_try

        last_exception = None
        for i, model in enumerate(models_to_try):
            print(f"ℹ️ Attempting generation with model: {model}")
            is_last_model = (i == len(models_to_try) - 1)
            max_retries = 3
            
            for attempt in range(max_retries + 1):
                try:
                    return await client.client.aio.models.generate_content(
                        model=model,
                        contents=contents,
                        config=config,
                    )
                except Exception as e:
                    last_exception = e
                    err_msg = str(e)
                    print(f"⚠️ Error with model {model} (attempt {attempt + 1}/{max_retries + 1}): {err_msg}")
                    
                    should_retry = False
                    if attempt < max_retries:
                        err_str = err_msg.lower()
                        # If it's a permanent quota limit (limit: 0), never retry
                        if "limit: 0" not in err_str and "limit:0" not in err_str:
                            if is_last_model:
                                # Last model: retry on any 429/503/etc.
                                should_retry = any(kw in err_str for kw in ["429", "503", "resource_exhausted", "unavailable", "rate limit", "quota", "demand", "temporary", "overload"])
                            else:
                                # Not last model: retry on 503/unavailable, fallback immediately on 429
                                should_retry = any(kw in err_str for kw in ["503", "unavailable", "overload", "demand", "temporary"])
                    
                    if should_retry:
                        # Calculate backoff with jitter
                        delay = (1.5 ** attempt) + random.uniform(0.1, 0.5)
                        print(f"⏳ Transient error on {model}. Retrying in {delay:.2f}s...")
                        await asyncio.sleep(delay)
                    else:
                        print(f"❌ Failed or encountered rate limit on {model}. Moving to fallback/next model...")
                        break
        # If we got here, all models in the fallback chain failed
        raise last_exception

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
        response = await self._generate_with_fallback(
            contents=discovery_prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                temperature=0.2,
            ),
        )
        raw_text = response.text.strip()
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
        response = await self._generate_with_fallback(
            contents=user_prompt,
            config=types.GenerateContentConfig(
                system_instruction=system_instruction,
                temperature=0.7,
            ),
        )
        return response.text

    async def simulate_agent_critique(
        self, system_instruction: str, context_chunks: list[str]
    ) -> str:
        """Alias for simulate_dynamic_critique to support pubsub worker."""
        return await self.simulate_dynamic_critique(system_instruction, context_chunks)

    async def run_consensus_negotiation(self, *args, **kwargs) -> dict:
        """
        Arbitrates between the comprehensive multi-persona array to synthesize scores and generate optimal design fixes.
        Supports both a single list of dicts, or multiple critique strings/dicts passed as arguments.
        """
        critiques_list = []
        if args:
            if len(args) == 1 and isinstance(args[0], list):
                critiques_list = args[0]
            else:
                for idx, arg in enumerate(args):
                    if isinstance(arg, dict):
                        critiques_list.append(arg)
                    elif isinstance(arg, str):
                        critiques_list.append({
                            "name": f"Agent {idx + 1}",
                            "type": "Demographic",
                            "text": arg
                        })

        formatted_critiques = "\n\n".join(
            [
                f"PROFILE: {c.get('name', 'Agent')} ({c.get('type', 'Demographic')})\nCRITIQUE: {c.get('text', '') or c.get('critique', '')}"
                for c in critiques_list
            ]
        )

        arbitration_prompt = f"""
        You are the CitySmart Mediator Agent — an elite urban planning arbitrator specialising in developing-world civic infrastructure.
        Review this community feedback matrix representing real local populations:

        {formatted_critiques}

        CRITICAL OUTPUT RULES (MUST follow exactly):
        • You MUST NOT write continuous prose paragraphs or narrative summaries.
        • Your synthesis MUST be structured as a JSON object with the exact keys listed below.
        • The `summary_points` field MUST be a JSON array of 5 to 7 distinct, self-contained strings.
        • Each string in `summary_points` represents one clear, actionable policy insight.
        • No Markdown, no code fences, no trailing commentary outside the JSON object.

        REQUIRED JSON OUTPUT FORMAT:
        {{
            "social_impact_score": 0.0 to 100.0,
            "economic_viability_score": 0.0 to 100.0,
            "political_feasibility_score": 0.0 to 100.0,
            "summary_verdict": "Single concise headline sentence — the most critical finding.",
            "summary_points": [
                "Actionable policy point 1 with concrete recommendation.",
                "Actionable policy point 2 identifying specific demographic risk.",
                "Actionable policy point 3 detailing structural trade-off.",
                "Actionable policy point 4 — minimum 5, maximum 7 total points.",
                "Actionable policy point 5 with engineering or zoning fix."
            ],
            "blueprint_revisions": [
                {{
                    "original_element": "Description of official proposed element",
                    "failure_mode_detected": "Why it fails or who it harms",
                    "amended_design_fix": "Concrete engineering or structural compromise fix"
                }}
            ]
        }}
        """
        response = await self._generate_with_fallback(
            contents=arbitration_prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                temperature=0.7,
            ),
        )
        return json.loads(response.text.strip())

    async def interrogate_persona(
        self, system_instruction: str, history: list[dict], user_message: str
    ) -> str:
        """Maintains an active, stateful dialogue inside a specific citizen's semantic roleplay boundary."""
        contents = []
        for msg in history:
            role = "user" if msg.get("role") == "user" else "model"
            contents.append(
                types.Content(
                    role=role,
                    parts=[types.Part.from_text(text=msg.get("content", ""))]
                )
            )
        contents.append(
            types.Content(
                role="user",
                parts=[types.Part.from_text(text=user_message)]
            )
        )

        response = await self._generate_with_fallback(
            contents=contents,
            config=types.GenerateContentConfig(
                system_instruction=system_instruction,
                temperature=0.7,
            ),
        )
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
        prompt_response = await self._generate_with_fallback(
            contents=refinement_prompt,
            model_override="gemini-2.5-flash",
        )
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
