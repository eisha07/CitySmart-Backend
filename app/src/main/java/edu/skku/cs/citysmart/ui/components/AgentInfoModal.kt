package edu.skku.cs.citysmart.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
private val BgDeep        = Color(0xFF0D1117)
private val BgCard        = Color(0xFF161B22)
private val BorderSubtle  = Color(0xFF21262D)
private val NeonGreen     = Color(0xFF00E676)
private val AmberAccent   = Color(0xFFFFAB00)
private val TextPrimary   = Color(0xFFCDD9E5)
private val TextSecondary = Color(0xFF8B949E)

// ─────────────────────────────────────────────────────────────────────────────
// AgentRosterPanel  –  Prompt 2 top-level component
//
// Renders the full list of active PersonaMetadata objects as scrollable cards.
// Each card has an (ⓘ) icon button that triggers AgentInfoModal.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen scrollable roster of active simulation agents.
 *
 * @param agents   List of [PersonaMetadata] supplied by the backend `active_agents` field.
 * @param modifier Standard Compose modifier (fills parent by default).
 */
@Composable
fun AgentRosterPanel(
    agents: List<PersonaMetadata>,
    modifier: Modifier = Modifier
) {
    // Track which agent's modal is open (null = none)
    var selectedAgent by remember { mutableStateOf<PersonaMetadata?>(null) }

    Column(modifier = modifier.background(BgDeep)) {

        // ── Section header ────────────────────────────────────────────────────
        Text(
            text = "ACTIVE PERSONA AGENTS",
            color = AmberAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )

        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

        if (agents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active agents — run a simulation first.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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

    // ── Bottom-sheet modal ────────────────────────────────────────────────────
    selectedAgent?.let { agent ->
        AgentInfoModal(
            agent     = agent,
            onDismiss = { selectedAgent = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AgentCard  –  single persona row
// ─────────────────────────────────────────────────────────────────────────────

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = BgCard, shape = RoundedCornerShape(10.dp))
                .border(width = 0.5.dp, color = BorderSubtle, shape = RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon-tag bubble
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color = Color(0xFF0D2818), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = agent.iconTag,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = agent.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = agent.shortDescription,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // (ⓘ) info button
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector    = Icons.Filled.Info,
                    contentDescription = "View ${agent.name} details",
                    tint           = NeonGreen,
                    modifier       = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AgentInfoModal  –  bottom-sheet popup with characteristics bullets
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Modal bottom sheet that displays the full [PersonaMetadata] profile for one
 * agent. Opened when the user taps the (ⓘ) icon on an [AgentCard].
 *
 * Renders [PersonaMetadata.characteristics] as the same staggered neon bullet
 * style used in [ArbitratorVerdictTerminal] for visual consistency.
 *
 * @param agent     The persona to display.
 * @param onDismiss Called when the user dismisses the sheet.
 */
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
        tonalElevation     = 0.dp,
        dragHandle         = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(3.dp)
                    .background(color = Color(0xFF30363D), shape = RoundedCornerShape(50))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── Agent header ─────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(color = Color(0xFF0D2818), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = agent.iconTag, fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = agent.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = agent.shortDescription,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // ── Characteristics section header ────────────────────────────────
            Text(
                text = "CHARACTERISTICS",
                color = AmberAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Staggered bullet list ─────────────────────────────────────────
            agent.characteristics.forEachIndexed { idx, trait ->
                ModalBulletPoint(text = trait, index = idx)
                if (idx < agent.characteristics.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// ── Staggered bullet row (modal-scoped) ───────────────────────────────────────

@Composable
private fun ModalBulletPoint(text: String, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(text) {
        kotlinx.coroutines.delay(index * 70L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp, end = 10.dp)
                    .size(5.dp)
                    .background(color = NeonGreen, shape = CircleShape)
            )
            Text(
                text       = text,
                color      = TextPrimary,
                fontSize   = 13.sp,
                lineHeight = 20.sp,
                modifier   = Modifier.weight(1f)
            )
        }
    }
}

// ── Divider helper ────────────────────────────────────────────────────────────

@Composable
private fun HorizontalDivider(color: Color, thickness: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PreviewAgentRosterPanel() {
    AgentRosterPanel(
        agents = listOf(
            PersonaMetadata(
                name             = "Aisha, Female Commuter",
                iconTag          = "👩",
                shortDescription = "Daily commuter for whom safety dictates every route choice.",
                characteristics  = listOf(
                    "Lighting conditions are the primary route-selection factor.",
                    "Avoids unlit alleys and unmarked crossing points.",
                    "Relies on scheduled public transport over informal options.",
                    "Peak vulnerability windows: early morning and late evening."
                )
            ),
            PersonaMetadata(
                name             = "Muhammad, Rickshaw Driver",
                iconTag          = "🛺",
                shortDescription = "Qingqi driver navigating dense urban corridors.",
                characteristics  = listOf(
                    "Survival depends on road-side stopping zones for pick-up.",
                    "Income directly tied to route efficiency and stop accessibility.",
                    "Acts as a critical last-mile connector for low-income areas."
                )
            )
        )
    )
}
