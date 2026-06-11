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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.domain.PersonaMetadata

// ── Colour tokens shared across both components ───────────────────────────────
private val BgCard        = Color(0xFF161B22).copy(alpha = 0.6f)
private val BorderSubtle  = Color(0xFF21262D)
private val NeonGreen     = Color(0xFF00E676)
private val AmberAccent   = Color(0xFFFFAB00)
private val TextPrimary   = Color(0xFFCDD9E5)
private val TextSecondary = Color(0xFF8B949E)

/**
 * Full-screen scrollable roster of active simulation agents.
 * Updated to match the "Massive Header" style of the Analytics/Feed screens.
 */
@Composable
fun AgentRosterPanel(
    agents: List<PersonaMetadata>,
    modifier: Modifier = Modifier
) {
    var selectedAgent by remember { mutableStateOf<PersonaMetadata?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Massive thin number header to match Analytics theme
        Text(
            text = String.format("%02d", agents.size),
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = "SELECTED AGENTS & DEMOGRAPHICS",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (agents.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO AGENTS SELECTED",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(agents) { index, agent ->
                    AgentCard(
                        agent = agent,
                        index = index,
                        onInfoClick = { selectedAgent = agent }
                    )
                }
            }
        }
    }

    selectedAgent?.let { agent ->
        AgentInfoModal(
            agent     = agent,
            onDismiss = { selectedAgent = null }
        )
    }
}

@Composable
private fun AgentCard(
    agent: PersonaMetadata,
    index: Int,
    onInfoClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(agent.name) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 3 }
    ) {
        // Using Glass-style panel container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color = Color.White.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = agent.iconTag, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = agent.name.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    agent.demographics?.let {
                        Text(
                            text = it,
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Text(
                        text = agent.shortDescription,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentInfoModal(
    agent: PersonaMetadata,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest   = onDismiss,
        sheetState         = sheetState,
        containerColor     = Color(0xFF161B22),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = agent.iconTag,
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = agent.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            agent.demographics?.let {
                Surface(
                    color = NeonGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = it,
                        color = NeonGreen,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = agent.shortDescription,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))

            Text(
                text = "CHARACTERISTICS",
                color = AmberAccent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                letterSpacing = 2.sp
            )

            agent.characteristics.forEach { trait ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("• ", color = NeonGreen)
                    Text(text = trait, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewAgentRosterPanel() {
    AgentRosterPanel(
        agents = listOf(
            PersonaMetadata(
                name = "Aisha",
                iconTag = "👩",
                shortDescription = "Daily commuter relying on public transport and safe walking paths.",
                demographics = "Female, 24, G-9 Sector Resident",
                characteristics = listOf("Prioritizes lighting", "Sidewalk quality focus")
            )
        )
    )
}
