package edu.skku.cs.citysmart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.viewmodel.DashboardViewModel

@Composable
fun GenesisInputScreen(
    viewModel: DashboardViewModel,
    onNavigateToDashboard: () -> Unit
) {
    // 1. Memory for what the user types
    var promptText by remember { mutableStateOf("") }
    // 2.
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 3. Watch the ViewModel to see if the AI is thinking
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Deep dark background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- HEADER ---
        Text(
            text = "CITYSMART CORE",
            color = Color(0xFF00E676), // Neon Hacker Green
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
                        viewModel.submitGenesisPrompt(promptText, onNavigateToDashboard)
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

        // 👇 THIS IS THE CRITICAL ERROR CATCHER 👇
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage!!,
                color = Color(0xFFFF5252), // Bright Error Red
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    } // <-- This is the closing bracket for your main Column
} // <-- This is the closing bracket for the GenesisInputScreen Composable