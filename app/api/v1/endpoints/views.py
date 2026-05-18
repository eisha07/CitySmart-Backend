from fastapi import APIRouter, Request, Depends, HTTPException
from fastapi.responses import HTMLResponse
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.database import get_db
from app.api.v1.endpoints.simulation import get_simulation_state

router = APIRouter(prefix="/dashboard", tags=["Management Dashboard Views"])

@router.get("/{project_id}", response_class=HTMLResponse)
async def render_management_analytics_dashboard(request: Request, project_id: str, db: AsyncSession = Depends(get_db)):
    try:
        # Fetch our live simulation dataset payload structured in Phase 5
        sim_data = await get_simulation_state(project_id=project_id, db=db)
        
        # Programmatically construct an interactive, dark-mode administrative analytical viewport
        html_content = f"""
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>CitySmart Urban Simulation Core | Project Matrix</title>
            <script src="https://cdn.tailwindcss.com"></script>
        </head>
        <body class="bg-slate-950 text-slate-100 min-h-screen font-sans p-8">
            <header class="mb-8 border-b border-slate-800 pb-4">
                <h1 class="text-3xl font-extrabold tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-indigo-500">
                    CitySmart Strategic Planning Portal
                </h1>
                <p class="text-sm text-slate-400 mt-1">Project Identifier Boundary Scope: <span class="text-cyan-400 font-mono">{project_id}</span></p>
            </header>

            <!-- 1. Scorecard Panel -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <div class="bg-slate-900 border border-slate-800 p-6 rounded-xl shadow-lg">
                    <h3 class="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-2">Social Impact Index</h3>
                    <div class="text-4xl font-bold text-emerald-400">{sim_data.scores.social_impact_score}%</div>
                </div>
                <div class="bg-slate-900 border border-slate-800 p-6 rounded-xl shadow-lg">
                    <h3 class="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-2">Economic Viability Index</h3>
                    <div class="text-4xl font-bold text-cyan-400">{sim_data.scores.economic_viability_score}%</div>
                </div>
                <div class="bg-slate-900 border border-slate-800 p-6 rounded-xl shadow-lg">
                    <h3 class="text-sm font-semibold text-slate-400 uppercase tracking-wider mb-2">Political Feasibility Index</h3>
                    <div class="text-4xl font-bold text-amber-400">{sim_data.scores.political_feasibility_score}%</div>
                </div>
            </div>

            <!-- 2. Arbitrator Verdict Box -->
            <div class="bg-gradient-to-r from-slate-900 to-indigo-950 border border-indigo-900/50 p-6 rounded-xl shadow-lg mb-8">
                <h2 class="text-lg font-bold text-indigo-300 mb-2">⚖️ Strategic Verdict Summary (Arbitrator Evaluation)</h2>
                <p class="text-slate-300 leading-relaxed text-sm italic">"{sim_data.summary_verdict}"</p>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <!-- 3. Live Debate Ticker Display -->
                <div class="bg-slate-900 border border-slate-800 p-6 rounded-xl shadow-md">
                    <h2 class="text-lg font-bold text-slate-200 mb-4 flex items-center gap-2">
                        <span class="animate-pulse w-2.5 h-2.5 rounded-full bg-red-500"></span>
                        Active Multi-Agent Stream Logs (Live Ticker)
                    </h2>
                    <div class="space-y-4 max-h-[350px] overflow-y-auto pr-2 font-mono text-xs">
                        {"".join([f'''
                        <div class="p-3 rounded bg-slate-950 border-l-2 {'border-red-500' if t.log_level == 'CRITICAL' else 'border-amber-500'}">
                            <span class="text-slate-500">[{t.timestamp}]</span> 
                            <span class="font-bold {'text-pink-400' if t.agent_profile == 'female_commuter' else 'text-cyan-400'}">{t.agent_profile.upper()}</span>: 
                            <p class="text-slate-300 mt-1 font-sans text-sm">{t.message}</p>
                        </div>
                        ''' for t in sim_data.live_debate_ticks])}
                    </div>
                </div>

                <!-- 4. Design Catalyst Blueprint Revision Display -->
                <div class="bg-slate-900 border border-slate-800 p-6 rounded-xl shadow-md">
                    <h2 class="text-lg font-bold text-slate-200 mb-4 flex items-center gap-2">🛠️ Design Catalyst Architectural Prescriptions</h2>
                    <div class="space-y-4">
                        {"".join([f'''
                        <div class="p-4 rounded-lg bg-slate-950 border border-slate-800">
                            <div class="text-xs font-bold text-red-400 line-through mb-1">Proposed Option: {r.original_element}</div>
                            <div class="text-xs text-amber-400 mb-2 font-medium">⚠️ Fault: {r.failure_mode_detected}</div>
                            <div class="text-sm text-emerald-400 font-semibold bg-emerald-950/30 p-2 rounded border border-emerald-900/40">
                                ✅ Prescriptive Hack: {r.amended_design_fix}
                            </div>
                        </div>
                        ''' for r in sim_data.blueprint_revisions])}
                    </div>
                </div>
            </div>
            
            <footer class="mt-12 text-center text-xs text-slate-600 border-t border-slate-900 pt-4">
                CitySmart Active Vision Ingestion Stack • Powered by Google Vertex AI Gemini-1.5-Pro Engine
            </footer>
        </body>
        </html>
        """
        return HTMLResponse(content=html_content, status_code=200)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Dashboard View Collapse: {{str(e)}}")
