package edu.skku.cs.citysmart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.skku.cs.citysmart.domain.*
import edu.skku.cs.citysmart.network.IngestRequest
import edu.skku.cs.citysmart.network.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    // 1. UI State: Holds the data so the screens can watch it
    private val _simulationState = MutableStateFlow<UrbanSimulationState?>(null)
    val simulationState: StateFlow<UrbanSimulationState?> = _simulationState.asStateFlow()

    private val _telemetry = MutableStateFlow<SpatialTelemetryCollection?>(null)
    val telemetry: StateFlow<SpatialTelemetryCollection?> = _telemetry.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 1. ADD THIS right below your isLoading StateFlow:
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun submitGenesisPrompt(prompt: String, onSimulationReady: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Clear old errors when we try again

            try {
                val request = IngestRequest(user_prompt = prompt)
                val newState = NetworkClient.api.ingestProposal(request)
                val newTelemetry = NetworkClient.api.getTelemetry()

                _simulationState.value = newState
                _telemetry.value = newTelemetry
                onSimulationReady()

            } catch (e: Exception) {
                // 2. UPDATE THIS to send the error to the UI!
                _errorMessage.value = "Link Severed: ${e.message}"
                Log.e("NetworkError", "Agents failed to process prompt: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}