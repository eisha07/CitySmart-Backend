package edu.skku.cs.citysmart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import edu.skku.cs.citysmart.ui.screens.GenesisInputScreen
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme
import edu.skku.cs.citysmart.viewmodel.DashboardViewModel

class GenesisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            CitySmartTheme {
                GenesisInputScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = {
                        val state = viewModel.simulationState.value
                        val intent = Intent(this@GenesisActivity, DashboardActivity::class.java).apply {
                            putExtra("PROJECT_ID", state?.projectId)
                            putExtra("USER_PROMPT", viewModel.currentPrompt.value)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
