"""
app/services/persona_registry.py

Central static registry for persona metadata profiles.

Populated once at import time. The simulation endpoint calls
`lookup_persona_metadata(name, role)` after the LLM generates its
dynamic persona list. The function fuzzy-matches the LLM-produced
`demographic_role` string against known keyword patterns and returns
the closest canonical `PersonaMetadata` object, overlaying the
LLM-generated name for personalisation.

This decouples characteristic data from the LLM chain so the
Jetpack Compose (ⓘ) modal always gets structured, non-hallucinated data.
"""

from app.schemas.simulation import PersonaMetadata

# ── Canonical registry ────────────────────────────────────────────────────────
# Key  = internal slug (never sent to client)
# Value = PersonaMetadata used by the frontend info-modal
PERSONA_REGISTRY: dict[str, PersonaMetadata] = {
    "rickshaw_driver": PersonaMetadata(
        name="Informal Transit Operator",
        icon_tag="🛺",
        short_description="Qingqi / rickshaw driver navigating dense urban corridors.",
        characteristics=[
            "Survival depends on road-side stopping zones for passenger pick-up.",
            "Highly sensitive to lane redesigns that remove informal transit space.",
            "Daily income is directly tied to route efficiency and stop accessibility.",
            "Frequently exposed to safety incidents due to absence of dedicated lanes.",
            "Acts as a critical last-mile connector for low-income neighbourhoods.",
            "Operates without formal registration; vulnerable to enforcement sweeps.",
        ],
    ),
    "street_vendor": PersonaMetadata(
        name="Street Vendor / Khokha Owner",
        icon_tag="🏪",
        short_description="Fixed-stall merchant whose revenue depends on foot-traffic.",
        characteristics=[
            "Business viability depends on unobstructed pedestrian access near the stall.",
            "Resists lane expansions that shrink pavement width or eliminate vending zones.",
            "Operates without formal tenancy; highly vulnerable to displacement by roadworks.",
            "Part of an informal supply chain supporting thousands of micro-businesses.",
            "Primary concern: maintaining customer visibility and physical accessibility.",
            "Prolonged construction detours can cause permanent customer-base attrition.",
        ],
    ),
    "female_commuter": PersonaMetadata(
        name="Female Commuter",
        icon_tag="👩",
        short_description="Daily commuter for whom safety dictates every route choice.",
        characteristics=[
            "Lighting conditions and surveillance coverage are primary route-selection factors.",
            "Prefers well-lit, monitored pedestrian paths and enclosed bus stops.",
            "Avoids isolated underpasses, unlit alleys, and unmarked crossing points.",
            "Represents a core urban equity metric; poor design directly increases risk.",
            "Relies on scheduled public transport over informal options for predictability.",
            "Peak vulnerability windows: early morning and late evening shifts.",
        ],
    ),
    "elderly_resident": PersonaMetadata(
        name="Elderly Resident",
        icon_tag="👴",
        short_description="Long-term resident with mobility and quality-of-life priorities.",
        characteristics=[
            "Requires smooth, continuous sidewalks free of construction debris or drop-offs.",
            "Dependent on accessible crossing infrastructure: ramps, tactile paving, clear signals.",
            "High sensitivity to noise, dust, and air-quality degradation during construction.",
            "Often on fixed income; prolonged disruption creates significant financial hardship.",
            "Advocates for neighbourhood continuity, heritage preservation, and familiar wayfinding.",
            "Represents a legally protected demographic under urban accessibility codes.",
        ],
    ),
    "traffic_warden": PersonaMetadata(
        name="Traffic Warden",
        icon_tag="🦺",
        short_description="Municipal officer who manages intersections and peak-hour flow.",
        characteristics=[
            "Operational effectiveness depends on clearly marked lanes and consistent signal logic.",
            "Identifies design dead-zones and bottlenecks that create chronic congestion.",
            "Advocates for enforcement-ready infrastructure with no ambiguous lane boundaries.",
            "Possesses ground-truth knowledge of peak-hour stress points.",
            "Tracks informal transit non-compliance zones not captured in design models.",
            "Input is critical for validating intersection capacity vs. design assumptions.",
        ],
    ),
    "student": PersonaMetadata(
        name="Student Commuter",
        icon_tag="🎓",
        short_description="Young daily commuter relying on affordable, safe transit options.",
        characteristics=[
            "Budget-constrained; depends on shared transport (bus, qingqi, walking).",
            "Requires safe pedestrian crossings and sheltered waiting infrastructure.",
            "Active social-media user; amplifies negative design experiences virally.",
            "High exposure to peak-hour congestion near educational institutions.",
            "Supports modernisation but concerned about long-term affordability impacts.",
            "Sensitive to changes in route safety that affect female peers disproportionately.",
        ],
    ),
    "shopkeeper": PersonaMetadata(
        name="Local Shopkeeper",
        icon_tag="🏬",
        short_description="Formal storefront merchant dependent on accessible clientele.",
        characteristics=[
            "Revenue directly tied to customer parking availability and pedestrian footfall.",
            "Sensitive to construction duration and detour routing that diverts traffic.",
            "Invested in local commercial vitality, property values, and neighbourhood prestige.",
            "Advocates for clear business-signage visibility post-redesign.",
            "Informally serves as a community anchor and local information hub.",
            "Potential to organise collective commercial opposition to disruptive projects.",
        ],
    ),
    "property_owner": PersonaMetadata(
        name="Property Owner / Landlord",
        icon_tag="🏘️",
        short_description="Real-estate stakeholder monitoring land-value implications.",
        characteristics=[
            "Closely tracks project impact on local property valuations and rental income.",
            "Concerned about eminent-domain proceedings or compulsory plot acquisition.",
            "Advocates for infrastructure upgrades that increase neighbourhood prestige.",
            "Monitors zoning changes affecting commercial-vs-residential usage ratios.",
            "Often politically networked; capable of influencing local approval processes.",
            "Evaluates long-term ROI of public investment against disruption risk.",
        ],
    ),
    "sanitation_crew": PersonaMetadata(
        name="Sanitation Worker",
        icon_tag="🧹",
        short_description="Municipal worker who operates in road infrastructure corridors.",
        characteristics=[
            "Requires safe, dedicated service-lane access for waste-collection vehicles.",
            "Vulnerable to redesigns that eliminate service alleys or access clearances.",
            "Early-morning shift patterns conflict with standard daytime planning assumptions.",
            "Represents a frequently overlooked but critical operational dependency.",
            "Key indicator of long-term infrastructure maintainability and upkeep cost.",
            "Works in physical proximity to traffic hazards without formal safety protocols.",
        ],
    ),
    "night_shift_worker": PersonaMetadata(
        name="Night-Shift Worker",
        icon_tag="🌙",
        short_description="Industrial or hospitality worker commuting during off-hours.",
        characteristics=[
            "Dependent on road safety and transit continuity between midnight and 5 AM.",
            "Highlights critical gaps in 24/7 infrastructure planning assumptions.",
            "High vulnerability to poorly lit paths and infrequent off-peak public transport.",
            "Often invisible to standard 9-to-5 urban simulation models.",
            "Represents factory shift employees, hospital staff, and informal night-economy workers.",
            "Transit disruptions have cascading effects on employment reliability.",
        ],
    ),
}

# ── Fuzzy keyword → registry key mapping ─────────────────────────────────────
# Order matters: more specific phrases appear before generic single words.
_ROLE_KEYWORD_MAP: list[tuple[str, str]] = [
    ("qingqi", "rickshaw_driver"),
    ("rickshaw", "rickshaw_driver"),
    ("transit operator", "rickshaw_driver"),
    ("informal transit", "rickshaw_driver"),
    ("khokha", "street_vendor"),
    ("street vendor", "street_vendor"),
    ("vendor", "street_vendor"),
    ("hawker", "street_vendor"),
    ("stall", "street_vendor"),
    ("female commuter", "female_commuter"),
    ("woman commuter", "female_commuter"),
    ("female", "female_commuter"),
    ("elderly", "elderly_resident"),
    ("senior citizen", "elderly_resident"),
    ("senior", "elderly_resident"),
    ("traffic warden", "traffic_warden"),
    ("warden", "traffic_warden"),
    ("traffic police", "traffic_warden"),
    ("student", "student"),
    ("schoolchild", "student"),
    ("university", "student"),
    ("shopkeeper", "shopkeeper"),
    ("merchant", "shopkeeper"),
    ("retailer", "shopkeeper"),
    ("storefront", "shopkeeper"),
    ("property owner", "property_owner"),
    ("landlord", "property_owner"),
    ("real estate", "property_owner"),
    ("sanitation", "sanitation_crew"),
    ("waste collector", "sanitation_crew"),
    ("cleaner", "sanitation_crew"),
    ("night shift", "night_shift_worker"),
    ("night-shift", "night_shift_worker"),
    ("factory worker", "night_shift_worker"),
    ("shift worker", "night_shift_worker"),
]


def lookup_persona_metadata(name: str, role: str) -> PersonaMetadata:
    """
    Fuzzy-matches a dynamically-generated demographic role string to its
    canonical PersonaMetadata profile from the static registry.

    Strategy:
    1. Concatenate LLM-generated `name` + `role`, lowercase.
    2. Iterate the ordered keyword map (longer phrases first) for first match.
    3. Clone the canonical profile and overlay the LLM name for personalisation.
    4. Return a generic fallback profile if no keyword matches.

    Args:
        name: LLM-generated persona name (e.g. "Muhammad, rickshaw driver").
        role: LLM-generated demographic_role (e.g. "Informal Transit Operator").

    Returns:
        A PersonaMetadata instance ready for serialisation in the API response.
    """
    combined = f"{name} {role}".lower()
    for keyword, registry_key in _ROLE_KEYWORD_MAP:
        if keyword in combined:
            canonical = PERSONA_REGISTRY[registry_key]
            # Overlay with the LLM-personalised name; keep canonical metadata.
            return PersonaMetadata(
                name=name,
                icon_tag=canonical.icon_tag,
                short_description=canonical.short_description,
                characteristics=canonical.characteristics,
            )

    # Generic fallback — ensures the API never returns a null agent profile.
    return PersonaMetadata(
        name=name,
        icon_tag="👤",
        short_description=f"Urban stakeholder: {role}",
        characteristics=[
            f"Directly impacted by proposed infrastructure changes in their role as {role}.",
            "Evaluates project feasibility through the lens of daily operational constraints.",
            "Core concerns span safety, accessibility, economic continuity, and community cohesion.",
            "Represents a demographic segment whose input is essential for inclusive urban planning.",
        ],
    )
