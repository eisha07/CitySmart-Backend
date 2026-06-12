package edu.skku.cs.citysmart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.skku.cs.citysmart.ui.screens.MainNavigationShell
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme
import edu.skku.cs.citysmart.viewmodel.DashboardViewModel

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val projectId = intent.getStringExtra("PROJECT_ID")
        val userPrompt = intent.getStringExtra("USER_PROMPT") ?: ""

        setContent {
            val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val state by viewModel.simulationState.collectAsState()
            val telemetry by viewModel.telemetry.collectAsState()

            // Trigger load if we have a project ID but no state yet
            // Pass the userPrompt to ensure proxy data can be re-generated
            LaunchedEffect(projectId) {
                if (state == null && projectId != null) {
                    viewModel.loadProject(projectId, userPrompt)
                }
            }
            
            CitySmartTheme {
                if (state != null && telemetry != null) {
                    MainNavigationShell(
                        state = state!!,
                        telemetry = telemetry!!,
                        userPrompt = userPrompt,
                        onAmendPolicy = { id, suggestion ->
                            viewModel.amendPolicy(id, suggestion)
                        },
                        onResetAndNavigateBack = {
                            viewModel.resetState()
                            startActivity(Intent(this@DashboardActivity, GenesisActivity::class.java))
                            finish()
                        },
                        onExitApp = {
                            finish()
                        }
                    )
                } else {
                    // Fallback loading screen while fetching data
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00E676))
                        Text(
                            text = "Initializing Command Center...", 
                            color = Color.White, 
                            modifier = Modifier.padding(top = 80.dp)
                        )
                    }
                }
            }
        }
    }
}
