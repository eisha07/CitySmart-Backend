package edu.skku.cs.citysmart.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.skku.cs.citysmart.domain.*
import edu.skku.cs.citysmart.ui.components.*
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme
import edu.skku.cs.citysmart.ui.theme.MidnightPurple
import edu.skku.cs.citysmart.ui.theme.SoftViolet

// 1. Unified Navigation: 5-tab system from main + polished icons
sealed class NavScreen(val route: String, val title: String, val icon: ImageVector) {
    object Map : NavScreen("map", "Map", Icons.Filled.LocationOn)
    object Analytics : NavScreen("analytics", "Analytics", Icons.Filled.Assessment)
    object Blueprints : NavScreen("blueprints", "Plans", Icons.Filled.Construction)
    object Comms : NavScreen("comms", "Feed", Icons.Filled.Forum)
    object Personas : NavScreen("personas", "Agents", Icons.Filled.Group)
}

@Composable
fun OverhauledDashboardScreen(
    state: UrbanSimulationState,
    onBackToPrompt: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // Safely calculate aggregate score from feasibility pillars
    val averageScore = remember(state.scores) {
        state.scores?.let {
            (it.socialAcceptance + it.economicRoi + it.politicalJustification) / 3f
        } ?: 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // --- NEW: TOP NAVIGATION ROW ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DASHBOARD",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            
            // Explicit Back Button
            TextButton(
                onClick = onBackToPrompt,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00E676))
            ) {
                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RETURN TO TERMINAL", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Massive dynamic aggregate score header
        Text(
            text = String.format("%.1f", averageScore),
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "AGGREGATE FEASIBILITY INDEX",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- RE-INTEGRATED FEASIBILITY GAUGES ---
        state.scores?.let { scores ->
            TriPillarGaugePanel(
                scores = scores,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Pill Menu Row for secondary metrics
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { PillItem(Icons.Default.Speed, "Traffic", "Low") }
            item { PillItem(Icons.Default.Cloud, "Emission", "Med") }
            item { PillItem(Icons.Default.Bolt, "Power", "High") }
            item { PillItem(Icons.Default.WaterDrop, "Water", "Stable") }
        }

        // Card with overlapping 3D asset simulation
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            Soft3DCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(180.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "URBAN CONNECTIVITY",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Agent deliberations indicate that current modifications support ${state.scores?.socialAcceptance?.toInt() ?: 0}% of pedestrian and transit flow without critical bottlenecks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 20.dp, y = (-10).dp)
                    .align(Alignment.TopEnd)
                    .zIndex(1f),
                color = Color.Transparent
            ) {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ArbitratorVerdictTerminal(
            summaryPoints = state.summaryPoints,
            headline = state.summaryVerdict
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun PillItem(icon: ImageVector, title: String, value: String) {
    GlassPanel(
        modifier = Modifier
            .width(90.dp)
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun MainNavigationShell(
    state: UrbanSimulationState,
    telemetry: SpatialTelemetryCollection,
    userPrompt: String = "",
    onAmendPolicy: (String, String) -> Unit = { _, _ -> },
    onResetAndNavigateBack: () -> Unit = {},
    onExitApp: () -> Unit = {}
) {
    val navController = rememberNavController()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = MidnightPurple,
            title = {
                Text("TERMINATE SESSION?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Do you want to return or exit?", color = Color.White.copy(alpha = 0.7f))
            },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; onResetAndNavigateBack() }) {
                    Text("NEW POLICY", color = Color(0xFF00E676))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false; onExitApp() }) {
                    Text("EXIT APP", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        bottomBar = { OverhauledBottomNav(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onResetAndNavigateBack,
                containerColor = Color(0xFF00E676),
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("NEW PROMPT", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MidnightPurple, SoftViolet),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavScreen.Analytics.route
            ) {
                composable(NavScreen.Map.route) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CoreSpatialCanvas(
                            telemetry = telemetry,
                            agents = state.activeAgents,
                            userPrompt = userPrompt
                        )
                    }
                }

                composable(NavScreen.Analytics.route) {
                    OverhauledDashboardScreen(
                        state = state,
                        onBackToPrompt = onResetAndNavigateBack
                    )
                }

                composable(NavScreen.Blueprints.route) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BlueprintRevisionCarousel(
                            revisions = state.blueprintRevisions,
                            onAmend = onAmendPolicy
                        )
                    }
                }

                composable(NavScreen.Comms.route) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LiveDebateTickerPanel(ticks = state.liveDebateTicks)
                    }
                }

                composable(NavScreen.Personas.route) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AgentRosterPanel(agents = state.activeAgents)
                    }
                }
            }
        }
    }
}

@Composable
fun OverhauledBottomNav(navController: NavHostController) {
    val items = listOf(
        NavScreen.Map, NavScreen.Analytics, NavScreen.Blueprints,
        NavScreen.Comms, NavScreen.Personas
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.blur(10.dp)
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, fontSize = 10.sp) },
                selected = currentRoute == screen.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF00E676),
                    selectedTextColor = Color(0xFF00E676),
                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                    unselectedTextColor = Color.White.copy(alpha = 0.4f),
                    indicatorColor = Color.White.copy(alpha = 0.1f)
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
