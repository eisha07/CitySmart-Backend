/**
 * UPDATED: The default server IP has been changed to 172.30.1.5:8000.
 * This update ensures the application connects to the latest backend deployment by default.
 */
package edu.skku.cs.citysmart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme
import edu.skku.cs.citysmart.viewmodel.DashboardViewModel

@Composable
fun GenesisInputScreen(
    viewModel: DashboardViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showFallbackOption by viewModel.showFallbackOption.collectAsState()

    GenesisInputContent(
        errorMessage = errorMessage,
        isLoading = isLoading,
        showFallbackOption = showFallbackOption,
        onSubmit = { prompt, ip ->
            viewModel.updateServerIp(ip)
            viewModel.submitGenesisPrompt(prompt, onNavigateToDashboard)
        },
        onUseFallback = {
            viewModel.triggerFallback(onNavigateToDashboard)
        }
    )
}

@Composable
fun GenesisInputContent(
    errorMessage: String?,
    isLoading: Boolean,
    showFallbackOption: Boolean,
    onSubmit: (String, String) -> Unit,
    onUseFallback: () -> Unit
) {
    var promptText by remember { mutableStateOf("") }
    
    // Memory for the Server IP (defaults to the active simulation server)
    var serverIp by remember { mutableStateOf("172.30.1.5:8000") }
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- HEADER ---
        Text(
            text = "CITYSMART CORE",
            color = Color(0xFF00E676),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter urban policy proposal for AI cross-examination.",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- INPUT TERMINAL ---
        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("e.g., Introduce a dedicated BRT lane in Saddar...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E676),
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                cursorColor = Color(0xFF00E676)
            ),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- SUBMIT BUTTON / LOADER ---
        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF00E676))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Agents deliberating...", color = Color.Gray)
        } else {
            Button(
                onClick = {
                    if (promptText.isNotBlank()) {
                        onSubmit(promptText, serverIp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
            ) {
                Text("INITIATE SIMULATION", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // --- ERROR CATCHER ---
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color(0xFFFF5252),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            if (showFallbackOption) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onUseFallback,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Text("USE PROXY DATA INSTEAD")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- DYNAMIC SERVER CONFIG ---
        TextButton(onClick = { showSettings = !showSettings }) {
            Text(
                text = if (showSettings) "HIDE SYSTEM LINK" else "CONFIGURE SYSTEM LINK",
                color = Color.DarkGray,
                fontSize = 10.sp
            )
        }

        if (showSettings) {
            OutlinedTextField(
                value = serverIp,
                onValueChange = { serverIp = it },
                label = { Text("Backend IP (e.g. 172.30.1.5:8000)", fontSize = 10.sp) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Cyan,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGenesisInputScreen() {
    CitySmartTheme {
        GenesisInputContent(
            errorMessage = null,
            isLoading = false,
            showFallbackOption = false,
            onSubmit = { _, _ -> },
            onUseFallback = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGenesisInputScreenLoading() {
    CitySmartTheme {
        GenesisInputContent(
            errorMessage = null,
            isLoading = true,
            showFallbackOption = false,
            onSubmit = { _, _ -> },
            onUseFallback = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGenesisInputScreenError() {
    CitySmartTheme {
        GenesisInputContent(
            errorMessage = "Link Severed: Connection Refused. Check Server IP.",
            isLoading = false,
            showFallbackOption = true,
            onSubmit = { _, _ -> },
            onUseFallback = {}
        )
    }
}