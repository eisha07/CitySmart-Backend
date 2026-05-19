package edu.skku.cs.citysmart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Steps 28-30: The Executive Legal Verdict Box from the Gemini Core
 */
@Composable
fun ArbitratorVerdictTerminal(
    verdictText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = Color(0xFF121212), // Deep dark terminal background
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF333333),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        // Header Tag
        Text(
            text = "AI ARBITRATOR VERDICT",
            color = Color(0xFFFFAB00), // Amber warning color
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // The actual decision string
        Text(
            text = verdictText,
            color = Color(0xFFE0E0E0), // Crisp off-white reading text
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 20.sp
        )
    }
}