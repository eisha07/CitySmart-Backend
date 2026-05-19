package edu.skku.cs.citysmart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.domain.BlueprintRevision

/**
 * Steps 41-43: The Horizontal Swipeable Carousel Container
 */
@Composable
fun BlueprintRevisionCarousel(
    revisions: List<BlueprintRevision>,
    modifier: Modifier = Modifier
) {
    // Keeps track of which card we are swiping on
    val pagerState = rememberPagerState(pageCount = { revisions.size })

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "STRUCTURAL REVISIONS (${pagerState.currentPage + 1}/${revisions.size})",
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp, start = 16.dp)
        )

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp), // Shows a peek of the next card
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            BlueprintCard(revision = revisions[page])
        }
    }
}

/**
 * Steps 44-48: The Individual "Before & After" Schematic Card
 */
@Composable
fun BlueprintCard(revision: BlueprintRevision) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep architectural blue
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Steps 44 & 45: Original Element with Strikethrough
            Column {
                Text(
                    text = "ORIGINAL PROPOSAL",
                    color = Color(0xFFFF5252),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = revision.originalElement,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.LineThrough, // The red strikethrough!
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Step 46: Failure Mode Warning Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3E2723).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFFF3D00).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "FAILURE MODE DETECTED",
                    color = Color(0xFFFF3D00),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = revision.failureModeDetected,
                    color = Color(0xFFFFCDD2),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Steps 47 & 48: AI Amendment Green Glow Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF003314).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "AMENDED DESIGN FIX",
                    color = Color(0xFF00E676),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = revision.amendedDesignFix,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Steps 39, 40, & 50: The Visual Preview Test!
 */

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    showSystemUi = true,
    device = "id:pixel_5"
)
@Composable
fun PreviewBlueprintCarousel() {
    val mockRevisions = listOf(
        BlueprintRevision(
            revisionId = "REV-001",
            originalElement = "Solid concrete barrier walls along the pedestrian walkway.",
            failureModeDetected = "Creates an impassable bottleneck for Tariq's rickshaw turning radius.",
            amendedDesignFix = "Replace with modular bollards spaced exactly 1.5m apart."
        ),
        BlueprintRevision(
            revisionId = "REV-002",
            originalElement = "Ground-level crosswalk at Sector G-9 intersection.",
            failureModeDetected = "High risk collision zone for pedestrians during load-shedding hours.",
            amendedDesignFix = "Elevated pedestrian bridge with solar-backed neon safety lighting."
        )
    )

    Box(modifier = Modifier.padding(vertical = 32.dp)) {
        BlueprintRevisionCarousel(revisions = mockRevisions)
    }
}