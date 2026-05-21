package edu.skku.cs.citysmart.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.ui.theme.CitySmartTheme

/**
 * Refactored Arbitrator Verdict: Combines structured bullet points and animations
 * with the new Glassmorphic aesthetic.
 */
@Composable
fun ArbitratorVerdictTerminal(
    summaryPoints: List<String> = emptyList(),
    headline: String? = null,
    verdictText: String? = null, // Legacy support
    modifier: Modifier = Modifier
) {
    // Smart parsing: Use list if available, otherwise split legacy text
    val points: List<String> = when {
        summaryPoints.isNotEmpty() -> summaryPoints
        !verdictText.isNullOrBlank() -> verdictText.split("\n").filter { it.isNotBlank() }
        else -> emptyList()
    }

    GlassPanel(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- Header Section ---
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

            if (!headline.isNullOrBlank()) {
                Text(
                    text = headline,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- Animated Bullet Points ---
            if (points.isEmpty()) {
                Text(
                    text = "WAITING FOR AGENT SYNTHESIS...",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                points.forEachIndexed { index, point ->
                    AnimatedBulletPoint(text = point, index = index)
                    if (index < points.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Footer ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "VALIDATED BY GEMINI CORE",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AnimatedBulletPoint(text: String, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(text) {
        kotlinx.coroutines.delay(index * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
                slideInVertically(animationSpec = tween(400)) { it / 4 }
    ) {
        Row(
            modifier = Modifier.padding(bottom = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp, end = 12.dp)
                    .size(6.dp)
                    .background(color = Color(0xFF00E676), shape = CircleShape)
            )
            Text(
                text = text.trim().removePrefix("-").removePrefix("*").trim(),
                color = Color.White.copy(alpha = 0.8f), // Updated to Glassmorphism transparency
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0425)
@Composable
fun ArbitratorVerdictTerminalPreview() {
    CitySmartTheme {
        ArbitratorVerdictTerminal(
            headline = "EXECUTIVE SUMMARY: DISTRICT 7 REDEVELOPMENT",
            summaryPoints = listOf(
                "Prioritize pedestrian-first infrastructure in the central hub.",
                "Allocate surplus energy credits to the smart-grid expansion.",
                "Initiate public consultation for the new botanical gardens."
            )
        )
    }
}
