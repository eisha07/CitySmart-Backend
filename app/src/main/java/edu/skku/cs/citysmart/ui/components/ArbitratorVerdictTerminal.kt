package edu.skku.cs.citysmart.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Refactored Arbitrator Verdict: Combines structured bullet points from the new API
 * with the executive glassmorphic terminal aesthetic.
 */
@Composable
fun ArbitratorVerdictTerminal(
    summaryPoints: List<String>,
    headline: String? = null,
    verdictText: String? = null, // Legacy support
    modifier: Modifier = Modifier
) {
    val points: List<String> = when {
        summaryPoints.isNotEmpty() -> summaryPoints
        !verdictText.isNullOrBlank() -> verdictText.split("\n").filter { it.isNotBlank() }
        else -> emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color(0xFF0A0A0A), shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = Color(0xFF222222), shape = RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
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
                text = "MEDIATOR SYNTHESIS VERDICT",
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

        HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // --- Animated Bullet Points ---
        if (points.isEmpty()) {
            Text(
                text = "AWAITING AGENT SYNTHESIS...",
                color = Color.DarkGray,
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
                tint = Color(0xFF444444),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "VERIFIED BY CITYSMART MEDIATOR",
                color = Color(0xFF444444),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
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
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp, end = 12.dp)
                    .size(6.dp)
                    .background(color = Color(0xFF00E676), shape = CircleShape)
            )
            Text(
                text = text.trim().removePrefix("-").removePrefix("*").trim(),
                color = Color(0xFFB0B0B0),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
private fun PreviewArbitratorVerdictTerminal() {
    ArbitratorVerdictTerminal(
        headline = "Pedestrian integration gap creates a systemic equity failure.",
        summaryPoints = listOf(
            "Rickshaw pick-up zones must be relocated 15 m back from the main intersection.",
            "Female commuters report a critical lighting gap on the underpass approach.",
            "Political acceptance is contingent on a 60-day public consultation period."
        )
    )
}
