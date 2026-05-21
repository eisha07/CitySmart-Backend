package edu.skku.cs.citysmart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.domain.BlueprintRevision

/**
 * Updated Blueprint Carousel: Displays all policy elements, showing feasibility and amendments.
 * Features the topic at the head and agent deliberations underneath.
 */
@Composable
fun BlueprintRevisionCarousel(
    revisions: List<BlueprintRevision>,
    modifier: Modifier = Modifier
) {
    if (revisions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("NO POLICY ELEMENTS DETECTED", color = Color.White.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { revisions.size })

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Massive thin number header to match Analytics theme
        Text(
            text = revisions.size.toString(),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = "PROPOSAL COMPONENT FEASIBILITY",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.weight(1f)
        ) { page ->
            BlueprintPolicyCard(revision = revisions[page])
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pager Indicator
        Row(
            Modifier
                .height(40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(revisions.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp)
                        .background(color, RoundedCornerShape(50))
                )
            }
        }
    }
}

/**
 * Updated Card: Topic (Original Element) at the head, Amendments/Status below.
 * Shows both feasible and conflicting elements.
 */
@Composable
fun BlueprintPolicyCard(revision: BlueprintRevision) {
    val isConflict = revision.feasibilityStatus == "CONFLICT"
    val statusColor = when (revision.feasibilityStatus) {
        "FEASIBLE" -> Color(0xFF00E676)
        "CONFLICT" -> Color(0xFFFF3D00)
        "OPTIMAL" -> Color(0xFF00E5FF)
        else -> Color.White.copy(alpha = 0.6f)
    }

    GlassPanel(
        modifier = Modifier
            .fillMaxHeight(0.95f)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOPIC AT THE HEAD (The primary proposed element)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = revision.originalElement.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 26.sp,
                    letterSpacing = 0.5.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isConflict) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = revision.feasibilityStatus ?: "ANALYZING",
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            // 2. DELIBERATION & AMENDMENT UNDER
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Analysis from the neighbor chat agents
                PolicySection(
                    title = "NEIGHBOR CHAT DELIBERATION",
                    content = if (revision.failureModeDetected != "None") revision.failureModeDetected else "Agents reached consensus: This element aligns with local transit behaviors and is verified as feasible for current infrastructure.",
                    icon = Icons.Default.Forum,
                    color = Color.White.copy(alpha = 0.6f)
                )

                // Proposed Amendment (Only shown if conflict exists)
                if (isConflict && revision.amendedDesignFix != "None") {
                    PolicySection(
                        title = "PROPOSED AMENDMENT",
                        content = revision.amendedDesignFix,
                        icon = Icons.Default.AutoFixHigh,
                        color = Color(0xFF00E676)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // FEASIBILITY STATUS & SENTIMENT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "AGENT SENTIMENT",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = revision.agentSentiment ?: "NEUTRAL",
                        color = if (revision.agentSentiment == "POSITIVE") Color(0xFF00E676) else Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Text(
                    text = if (isConflict) "ACTION_REQUIRED" else "ENGINEERING_VERIFIED",
                    color = statusColor.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun PolicySection(
    title: String,
    content: String,
    icon: ImageVector,
    color: Color,
    strikethrough: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = color.copy(alpha = 0.8f), 
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
        Text(
            text = content,
            color = if (strikethrough) Color.White.copy(alpha = 0.4f) else Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            textDecoration = if (strikethrough) TextDecoration.LineThrough else null,
            modifier = Modifier.padding(top = 8.dp),
            lineHeight = 22.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewBlueprintCarouselFull() {
    val mockRevisions = listOf(
        BlueprintRevision(
            revisionId = "REV-001",
            originalElement = "High-density concrete road dividers.",
            failureModeDetected = "Agents Tariq and Aisha noted that these restrict turning for rickshaws, causing major gridlock in small lanes.",
            amendedDesignFix = "Replace with flexible safety bollards at 1.5m intervals.",
            feasibilityStatus = "CONFLICT",
            agentSentiment = "NEGATIVE"
        ),
        BlueprintRevision(
            revisionId = "REV-002",
            originalElement = "Solar-powered smart bus stations.",
            failureModeDetected = "None",
            amendedDesignFix = "None",
            feasibilityStatus = "FEASIBLE",
            agentSentiment = "POSITIVE"
        )
    )

    MaterialTheme {
        Box(modifier = Modifier.background(Color.DarkGray)) {
            BlueprintRevisionCarousel(revisions = mockRevisions)
        }
    }
}
