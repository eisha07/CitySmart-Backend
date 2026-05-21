package edu.skku.cs.citysmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.skku.cs.citysmart.ui.screens.GenesisInputScreen
import edu.skku.cs.citysmart.ui.screens.MainNavigationShell
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme
import edu.skku.cs.citysmart.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val state by viewModel.simulationState.collectAsState()
            val telemetry by viewModel.telemetry.collectAsState()

            // The master router!
            val rootNavController = rememberNavController()

            CitySmartTheme {
                NavHost(
                    navController = rootNavController,
                    startDestination = "genesis_input"
                ) {
                    // SCREEN 1: The Input Terminal
                    composable("genesis_input") {
                        GenesisInputScreen(
                            viewModel = viewModel,
                            onNavigateToDashboard = {
                                rootNavController.navigate("dashboard") {
                                    popUpTo("genesis_input") { inclusive = true }
                                }
                            }
                        )
                    }

                    // SCREEN 2: The Main Dashboard
                    composable("dashboard") {
                        if (state != null && telemetry != null) {
                            MainNavigationShell(
                                state = state!!,
                                telemetry = telemetry!!,
                                onResetAndNavigateBack = {
                                    viewModel.resetState()
                                    rootNavController.navigate("genesis_input") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                },
                                onExitApp = {
                                    finish()
                                }
                            )
                        } else {
                            // Fallback loading screen
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Error loading AI Data", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}
