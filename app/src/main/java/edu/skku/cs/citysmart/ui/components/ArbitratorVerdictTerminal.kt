package edu.skku.cs.citysmart.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Prompt 1 — Mediator Structured Bullet View
 *
 * Renders the Mediator Agent's [summaryPoints] as an animated, scannable
 * bullet list instead of a monolithic prose block.
 *
 * Each point slides in sequentially on first composition (staggered entrance).
 *
 * @param summaryPoints  Ordered list of distinct policy insights from the backend.
 * @param headline       Optional short headline (maps to `arbitrator_verdict`).
 * @param modifier       Standard Compose modifier.
 */
@Composable
fun ArbitratorVerdictTerminal(
    summaryPoints: List<String>,
    headline: String? = null,
    // Legacy single-string overload kept for call-sites not yet migrated
    verdictText: String? = null,
    modifier: Modifier = Modifier
) {
    // Resolve what to display: prefer the structured list, fall back to splitting
    // the legacy prose string so old data never renders as an empty panel.
    val points: List<String> = when {
        summaryPoints.isNotEmpty() -> summaryPoints
        !verdictText.isNullOrBlank() -> listOf(verdictText)
        else -> emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color(0xFF0D1117), shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = Color(0xFF21262D), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text = "⚖  MEDIATOR SYNTHESIS",
            color = Color(0xFFFFAB00),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        // Optional headline (arbitrator_verdict)
        if (!headline.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = headline,
                color = Color(0xFFE0E0E0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 19.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color(0xFF21262D), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // ── Bullet list ───────────────────────────────────────────────────────
        if (points.isEmpty()) {
            Text(
                text = "Awaiting mediator output…",
                color = Color(0xFF484F58),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        } else {
            points.forEachIndexed { index, point ->
                AnimatedBulletPoint(
                    text = point,
                    index = index
                )
                if (index < points.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// ── Animated bullet point ─────────────────────────────────────────────────────

@Composable
private fun AnimatedBulletPoint(text: String, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(text) {
        kotlinx.coroutines.delay(index * 80L)   // staggered entrance per bullet
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(animationSpec = tween(300)) { it / 4 }
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Neon dot
            Box(
                modifier = Modifier
                    .padding(top = 6.dp, end = 10.dp)
                    .size(6.dp)
                    .background(color = Color(0xFF00E676), shape = CircleShape)
            )
            // Point text
            Text(
                text = text,
                color = Color(0xFFCDD9E5),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ── Divider helper (avoids Material2/3 namespace collision) ──────────────────

@Composable
private fun Divider(color: Color, thickness: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF050505)
@Composable
private fun PreviewArbitratorVerdictTerminal() {
    ArbitratorVerdictTerminal(
        headline = "Pedestrian integration gap creates a systemic equity failure.",
        summaryPoints = listOf(
            "Rickshaw pick-up zones must be relocated 15 m back from the main intersection to prevent pedestrian conflict.",
            "Female commuters report a critical lighting gap on the underpass northern approach — requires 8 additional lamp posts.",
            "Khokha vendors on the eastern side face displacement; a designated 3 m vending setback must be added to the design.",
            "Traffic wardens indicate the new lane markings conflict with informal left-turn patterns at peak hours.",
            "Political acceptance is contingent on a 60-day public consultation period before groundbreaking."
        )
    )
}