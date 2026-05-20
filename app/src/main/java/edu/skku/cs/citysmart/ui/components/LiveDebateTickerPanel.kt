package edu.skku.cs.citysmart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.domain.LiveDebateTick
import java.util.Locale

/**
 * Step 31 & 32: The Main Scrollable Debate Container
 */
@Composable
fun LiveDebateTickerPanel(
    ticks: List<LiveDebateTick>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Step 37: Auto-scroll to the bottom whenever a new tick arrives
    LaunchedEffect(ticks.size) {
        if (ticks.isNotEmpty()) {
            listState.animateScrollToItem(ticks.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp) // Fixed height block for the dashboard
            .background(Color(0xFF0D0D0D))
            .border(1.dp, Color(0xFF262626))
            .padding(8.dp)
    ) {
        Text(
            text = "LIVE AGENT TELEMETRY FEED",
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(ticks) { tick ->
                if (tick.agentProfile == "female_commuter") {
                    AishaStreamComponent(tick)
                } else {
                    TariqStreamComponent(tick)
                }
            }
        }
    }
}

/**
 * Steps 33 & 34: Aisha's Commuter Component (Left Aligned - Magenta/Red focus)
 */
@Composable
fun AishaStreamComponent(tick: LiveDebateTick) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1A0A10), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp))
                .border(1.dp, Color(0xFFE91E63).copy(alpha = 0.3f), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tick.timestamp,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = (tick.agentName ?: "").uppercase(Locale.ROOT),
                    color = Color(0xFFE91E63),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                AlertChip(level = tick.alertLevel)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tick.messageText ?: "",
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Steps 35 & 36: Tariq's Driver Component (Right Aligned - Cyan/Blue focus)
 */
@Composable
fun TariqStreamComponent(tick: LiveDebateTick) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF0A141A), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                .border(1.dp, Color(0xFF00B0FF).copy(alpha = 0.3f), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                AlertChip(level = tick.alertLevel)
                Spacer(modifier = Modifier.width(8.dp))
                val agentDisplayName = (tick.agentName ?: "").uppercase(Locale.ROOT)
                Text(
                    text = agentDisplayName,
                    color = Color(0xFF00B0FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tick.timestamp,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tick.messageText ?: "",
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Tiny utility component for the neon CRITICAL/WARNING tags
 */
@Composable
fun AlertChip(level: String?) {
    val nonNullLevel = level ?: "INFO"
    val (bgColor, textColor) = when (nonNullLevel.uppercase(Locale.ROOT)) {
        "CRITICAL" -> Color(0xFFFF1744).copy(alpha = 0.2f) to Color(0xFFFF1744)
        "WARNING" -> Color(0xFFFF9100).copy(alpha = 0.2f) to Color(0xFFFF9100)
        else -> Color(0xFF00E676).copy(alpha = 0.2f) to Color(0xFF00E676)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = nonNullLevel,
            color = textColor,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewLiveDebateTicker() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0D0D0D)
        ) {
            val mockTicks = listOf(
                LiveDebateTick(
                    timestamp = "10:02:45",
                    agentName = "Aisha",
                    agentProfile = "female_commuter",
                    messageText = "The proposed pedestrian bridge feels too isolated at night. Requesting high-visibility lighting.",
                    alertLevel = "WARNING"
                ),
                LiveDebateTick(
                    timestamp = "10:03:12",
                    agentName = "Tariq",
                    agentProfile = "qingqi_driver",
                    messageText = "If you block that intersection for the bridge pillars, my turning radius is completely destroyed.",
                    alertLevel = "CRITICAL"
                ),
                LiveDebateTick(
                    timestamp = "10:03:50",
                    agentName = "Aisha",
                    agentProfile = "female_commuter",
                    messageText = "Understood, but ground-level crossing without a dedicated signal is a severe safety risk.",
                    alertLevel = "INFO"
                )
            )

            LiveDebateTickerPanel(
                ticks = mockTicks,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
