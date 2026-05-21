package edu.skku.cs.citysmart.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.domain.LiveDebateTick
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Updated Live Agent Chat Terminal: Aligned with the Analytics/UrbanDashboard theme.
 * Uses GlassPanel for messages and a transparent background to show the global gradient.
 */
@Composable
fun LiveDebateTickerPanel(
    ticks: List<LiveDebateTick>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Local state to manage the "streaming" effect of messages
    val visibleTicks = remember { mutableStateListOf<LiveDebateTick>() }

    LaunchedEffect(ticks) {
        if (ticks.size != visibleTicks.size) {
            visibleTicks.clear()
            ticks.forEach { tick ->
                delay(800) // Slightly faster entry
                visibleTicks.add(tick)
            }
        }
    }

    LaunchedEffect(visibleTicks.size) {
        if (visibleTicks.isNotEmpty()) {
            listState.animateScrollToItem(visibleTicks.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Massive thin number header to match Analytics theme
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = "AGENT TELEMETRY FEED",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(visibleTicks, key = { it.timestamp + (it.messageText ?: "") }) { tick ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 500)
                    ) + fadeIn(animationSpec = tween(durationMillis = 500))
                ) {
                    ChatBubble(tick)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(tick: LiveDebateTick) {
    val isAisha = tick.agentProfile == "female_commuter"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAisha) Arrangement.Start else Arrangement.End
    ) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (tick.agentName ?: if (isAisha) "AISHA" else "TARIQ").uppercase(Locale.ROOT),
                        color = if (isAisha) Color(0xFFE91E63) else Color(0xFF00B0FF),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AlertChip(level = tick.alertLevel)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tick.messageText ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = if (isAisha) TextAlign.Start else TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tick.timestamp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(if (isAisha) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}

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
            .padding(horizontal = 6.dp, vertical = 2.dp)
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
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF050505)
        ) {
            val mockTicks = listOf(
                LiveDebateTick(
                    timestamp = "10:02:45",
                    agentName = "Aisha",
                    agentProfile = "female_commuter",
                    messageText = "Yar, ye bridge raat ko bohat sunsaan lagta hai. Lightein thori zyada honi chahiye yahan.",
                    alertLevel = "WARNING"
                ),
                LiveDebateTick(
                    timestamp = "10:03:12",
                    agentName = "Tariq",
                    agentProfile = "qingqi_driver",
                    messageText = "Bhai agar ye pillars beech mein agaye to qingqi morna mushkil ho jaye ga. Rasta bohat tang hai.",
                    alertLevel = "CRITICAL"
                )
            )

            LiveDebateTickerPanel(
                ticks = mockTicks,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
