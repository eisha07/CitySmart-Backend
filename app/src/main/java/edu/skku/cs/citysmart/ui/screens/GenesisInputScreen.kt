package edu.skku.cs.citysmart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.ui.components.GlassPanel
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme
import edu.skku.cs.citysmart.ui.theme.MidnightPurple
import edu.skku.cs.citysmart.ui.theme.SoftViolet
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
    var serverIp by remember { mutableStateOf("http://10.0.2.2:8000/") }
    var showSettings by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // --- MASSIVE HEADER THEME ---
            Text(
                text = "CitySmart",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(top = 48.dp)
            )
            Text(
                text = "URBAN POLICY INGESTION",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // --- INPUT TERMINAL GLASS PANEL ---
            GlassPanel(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "CORE PROPOSAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { 
                            Text(
                                "e.g., Introduce a dedicated BRT lane in Saddar with solar-powered stations...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White.copy(alpha = 0.4f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF00E676)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SUBMIT BUTTON / LOADER ---
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF00E676))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("AGENTS DELIBERATING...", style = MaterialTheme.typography.labelMedium)
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (promptText.isNotBlank()) {
                            onSubmit(promptText, serverIp)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "INITIATE SIMULATION", 
                        color = Color.White, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            // --- ERROR CATCHER ---
            // --- ERROR CATCHER ---
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(24.dp))
                GlassPanel(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "SIMULATION LINK INTERRUPTED",
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 🔍 NEW: Prints the exact server IP being targeted
                        Text(
                            text = "Target Node: $serverIp",
                            color = Color.Cyan.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = errorMessage ?: "",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )

                        if (showFallbackOption) {
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(
                                onClick = onUseFallback,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("OVERRIDE WITH PROXY DATA", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- DYNAMIC SERVER CONFIG ---
            TextButton(
                onClick = { showSettings = !showSettings },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (showSettings) "HIDE SYSTEM LINK" else "CONFIGURE SYSTEM LINK",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }

            if (showSettings) {
                Spacer(modifier = Modifier.height(16.dp))
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = serverIp,
                        onValueChange = { serverIp = it },
                        label = { Text("BACKEND_HOST_NODE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Cyan.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
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
