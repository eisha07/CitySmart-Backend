package edu.skku.cs.citysmart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.skku.cs.citysmart.domain.*
import edu.skku.cs.citysmart.ui.components.*

// 1. Define our 5 separate screens and their icons
sealed class NavScreen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Map       : NavScreen("map",      "Map",     Icons.Filled.LocationOn)
    object Analytics : NavScreen("analytics","Scores",  Icons.Filled.Assessment)
    object Blueprints: NavScreen("blueprints","Plans",  Icons.Filled.Construction)
    object Comms     : NavScreen("comms",    "Feed",    Icons.Filled.Forum)
    object Personas  : NavScreen("personas", "Agents",  Icons.Filled.Group)
}

/**
 * Bottom-navigation shell.  Five tabs: Map | Scores | Plans | Feed | Agents
 */
@Composable
fun MainNavigationShell(
    state: UrbanSimulationState,
    telemetry: SpatialTelemetryCollection
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { DashboardBottomNav(navController) },
        containerColor = Color(0xFF050505)
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = NavScreen.Map.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // TAB 1: Geographic Map
            composable(NavScreen.Map.route) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    CoreSpatialCanvas(telemetry = telemetry)
                }
            }

            // TAB 2: Analytics & Gauges
            composable(NavScreen.Analytics.route) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // ── Prompt 1: Structured bullet verdict ──────────────────
                    ArbitratorVerdictTerminal(
                        summaryPoints = state.summaryPoints,
                        headline      = state.summaryVerdict
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    // Feasibility score gauges (nullable-safe)
                    state.scores?.let { TriPillarGaugePanel(scores = it) }
                }
            }

            // TAB 3: Architecture Carousel
            composable(NavScreen.Blueprints.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    BlueprintRevisionCarousel(revisions = state.blueprintRevisions)
                }
            }

            // TAB 4: Live Agent Feed
            composable(NavScreen.Comms.route) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    LiveDebateTickerPanel(ticks = state.liveDebateTicks)
                }
            }

            // TAB 5: Prompt 2 — Active Persona Agent Cards with ⓘ modal
            composable(NavScreen.Personas.route) {
                AgentRosterPanel(
                    agents = state.activeAgents ?: emptyList(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * The Bottom Navigation Bar Component (5 tabs)
 */
@Composable
fun DashboardBottomNav(navController: NavHostController) {
    val items = listOf(
        NavScreen.Map, NavScreen.Analytics, NavScreen.Blueprints,
        NavScreen.Comms, NavScreen.Personas
    )

    // Watch the current route so we know which tab to highlight
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF0A0A0A),
        contentColor = Color.Gray
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF00E676), // Neon Green for active
                    selectedTextColor = Color(0xFF00E676),
                    unselectedIconColor = Color.DarkGray,
                    unselectedTextColor = Color.DarkGray,
                    indicatorColor = Color(0xFF003314) // Dark green glow behind active icon
                ),
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Visual Preview
 */
@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_5")
@Composable
fun PreviewMainNavigationShell() {
    val mockState = UrbanSimulationState(
        projectId     = "saddar-bazaar",
        summaryVerdict = "Pedestrian gap at G-9 crossing creates a systemic equity failure.",
        summaryPoints  = listOf(
            "Rickshaw pick-up zones must be set back 15 m from the main intersection.",
            "Underpass northern approach has a critical lighting gap — 8 lamp posts required.",
            "Eastern khokha vendors face displacement; a 3 m vending setback must be added.",
            "New lane markings conflict with informal left-turn patterns at peak hours.",
            "60-day public consultation required before political acceptance is achievable."
        ),
        scores = FeasibilityScores(84.5f, 42.0f, 70.0f),
        liveDebateTicks = listOf(
            LiveDebateTick("10:02:45", "Aisha", "female_commuter", "The proposed pedestrian bridge feels too isolated.", "WARNING")
        ),
        blueprintRevisions = listOf(
            BlueprintRevision("REV-001", "Ground-level crosswalk at Sector G-9.", "High risk collision zone.", "Elevated pedestrian bridge.")
        ),
        activeAgents = listOf(
            edu.skku.cs.citysmart.domain.PersonaMetadata(
                name = "Aisha, Female Commuter",
                iconTag = "👩",
                shortDescription = "Daily commuter for whom safety dictates every route choice.",
                characteristics = listOf(
                    "Lighting conditions are the primary route-selection factor.",
                    "Avoids unlit alleys and unmarked crossing points.",
                    "Relies on scheduled public transport over informal options."
                )
            )
        )
    )

    val mockTelemetry = SpatialTelemetryCollection(
        features = listOf(
            GeoJsonFeature(
                geometry   = GeometryData("LineString", listOf(listOf(73.0479, 33.6844), listOf(73.0579, 33.6944))),
                properties = TelemetryProperties("female_commuter", 1.5f)
            )
        )
    )

    MainNavigationShell(state = mockState, telemetry = mockTelemetry)
}