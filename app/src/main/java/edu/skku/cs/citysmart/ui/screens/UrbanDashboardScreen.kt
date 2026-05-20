package edu.skku.cs.citysmart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

// 1. Unified Navigation: 5-tab system from main + polished icons
sealed class NavScreen(val route: String, val title: String, val icon: ImageVector) {
    object Map       : NavScreen("map",      "Map",     Icons.Filled.LocationOn)
    object Analytics : NavScreen("analytics","Scores",  Icons.Filled.Assessment)
    object Blueprints: NavScreen("blueprints","Plans",  Icons.Filled.Construction)
    object Comms     : NavScreen("comms",    "Feed",    Icons.Filled.Forum)
    object Personas  : NavScreen("personas", "Agents",  Icons.Filled.Group)
}

/**
 * Main Navigation Shell: Resolves conflict between overhauled UI and new 5-tab structure.
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
                Box(modifier = Modifier.fillMaxSize()) {
                    CoreSpatialCanvas(telemetry = telemetry)
                }
            }

            // TAB 2: Analytics & Gauges (Polished Layout + New Structured Verdicts)
            composable(NavScreen.Analytics.route) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EXECUTIVE SIMULATION ANALYTICS",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // ── Integrated Mediator Synthesis ──────────────────
                    ArbitratorVerdictTerminal(
                        summaryPoints = state.summaryPoints,
                        headline      = state.summaryVerdict
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Feasibility Gauges (Safely handle nullable scores from merge)
                    state.scores?.let { TriPillarGaugePanel(scores = it) }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // TAB 3: Blueprint Carousel
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

            // TAB 5: Agent Personas (New Tab from main)
            composable(NavScreen.Personas.route) {
                // Placeholder for Persona roster - assuming AgentRosterPanel is in the project
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("AGENT DIRECTORY ACTIVE", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DashboardBottomNav(navController: NavHostController) {
    val items = listOf(
        NavScreen.Map, NavScreen.Analytics, NavScreen.Blueprints,
        NavScreen.Comms, NavScreen.Personas
    )

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
                    selectedIconColor = Color(0xFF00E676),
                    selectedTextColor = Color(0xFF00E676),
                    unselectedIconColor = Color.DarkGray,
                    unselectedTextColor = Color.DarkGray,
                    indicatorColor = Color(0xFF003314)
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
