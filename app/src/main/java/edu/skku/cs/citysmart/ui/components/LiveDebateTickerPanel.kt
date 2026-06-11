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
 * Now dynamically handles multiple agents in a unified conversation feed.
 */
@Composable
fun LiveDebateTickerPanel(
    ticks: List<LiveDebateTick>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val visibleTicks = remember { mutableStateListOf<LiveDebateTick>() }

    LaunchedEffect(ticks) {
        if (ticks.size != visibleTicks.size) {
            visibleTicks.clear()
            ticks.forEach { tick ->
                delay(600)
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
        Text(
            text = "DEBATE",
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = "MULTI-AGENT INFRASTRUCTURE ANALYSIS",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(visibleTicks, key = { it.timestamp + (it.messageText ?: "") + (it.agentName ?: "") }) { tick ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 400)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400))
                ) {
                    ChatBubble(tick)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(tick: LiveDebateTick) {
    val agentColor = when (tick.agentProfile) {
        "female_commuter" -> Color(0xFFE91E63) // Aisha Pink
        "qingqi_driver"   -> Color(0xFF00B0FF) // Tariq Blue
        "shopkeeper"     -> Color(0xFFFFAB00) // Zahid Amber
        "senior_resident" -> Color(0xFF9C27B0) // Mr. Khan Purple
        "admin"          -> Color(0xFF00E676) // System Green
        else             -> Color.White
    }

    val isSystem = tick.agentProfile == "admin"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSystem) Arrangement.Center else Arrangement.Start
    ) {
        GlassPanel(
            modifier = Modifier.fillMaxWidth(if (isSystem) 0.95f else 0.85f)
        ) {
            Column(modifier = Modifier.padding(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (tick.agentName ?: "UNKNOWN").uppercase(Locale.ROOT),
                        color = agentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AlertChip(level = tick.alertLevel)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = tick.timestamp,
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = tick.messageText ?: "",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AlertChip(level: String?) {
    val nonNullLevel = level ?: "INFO"
    val (bgColor, textColor) = when (nonNullLevel.uppercase(Locale.ROOT)) {
        "CRITICAL" -> Color(0xFFFF1744).copy(alpha = 0.15f) to Color(0xFFFF1744)
        "WARNING" -> Color(0xFFFF9100).copy(alpha = 0.15f) to Color(0xFFFF9100)
        else -> Color(0xFF00E676).copy(alpha = 0.15f) to Color(0xFF00E676)
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
