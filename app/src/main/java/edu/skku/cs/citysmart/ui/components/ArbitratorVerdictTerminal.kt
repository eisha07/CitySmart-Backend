package edu.skku.cs.citysmart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Steps 28-30 Refactored: Structured Executive Legal Verdict Box
 */
@Composable
fun ArbitratorVerdictTerminal(
    verdictText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0A0A0A), // Even darker for higher contrast
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF222222),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(20.dp)
    ) {
        // Header Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Gavel,
                contentDescription = null,
                tint = Color(0xFFFFAB00),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "EXECUTIVE ARBITRATION SUMMARY",
                color = Color(0xFFFFAB00),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }

        HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Body Content with automated structure parsing
        val lines = verdictText.split("\n").filter { it.isNotBlank() }
        
        if (lines.isEmpty()) {
            Text(
                text = "WAITING FOR AGENT SYNTHESIS...",
                color = Color.DarkGray,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        } else {
            lines.forEachIndexed { index, line ->
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = if (line.trim().startsWith("-") || line.trim().startsWith("*")) "•" else "[${index + 1}]",
                        color = Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = line.trim().removePrefix("-").removePrefix("*").trim(),
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        // Footer timestamp or status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = Color(0xFF444444),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "VALIDATED BY GEMINI CORE",
                color = Color(0xFF444444),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
