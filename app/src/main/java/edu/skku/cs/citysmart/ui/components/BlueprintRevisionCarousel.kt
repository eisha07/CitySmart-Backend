package edu.skku.cs.citysmart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 * Updated Blueprint Carousel: Displays all policy elements.
 * Now includes interaction to allow the user to "Actively Participate" by amending policies.
 */
@Composable
fun BlueprintRevisionCarousel(
    revisions: List<BlueprintRevision>,
    onAmend: (String, String) -> Unit = { _, _ -> },
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
        // Massive thin number header
        Text(
            text = String.format("%02d", revisions.size),
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = "ACTIVE POLICY DELIBERATIONS",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.weight(1f)
        ) { page ->
            val revision = revisions[page]
            BlueprintPolicyCard(
                revision = revision,
                onAmend = { suggestion -> 
                    revision.revisionId?.let { id ->
                        onAmend(id, suggestion)
                    }
                }
            )
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
                val color = if (pagerState.currentPage == iteration) Color(0xFF00E676) else Color.White.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage == iteration) 10.dp else 6.dp)
                        .background(color, RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
fun BlueprintPolicyCard(
    revision: BlueprintRevision,
    onAmend: (String) -> Unit
) {
    val status = revision.feasibilityStatus ?: "UNKNOWN"
    val isConflict = status == "CONFLICT"
    val statusColor = when (status) {
        "FEASIBLE" -> Color(0xFF00E676)
        "CONFLICT" -> Color(0xFFFF3D00)
        "OPTIMAL"  -> Color(0xFF00E5FF)
        else       -> Color.White.copy(alpha = 0.6f)
    }

    var showAmendDialog by remember { mutableStateOf(false) }
    var userSuggestion by remember { mutableStateOf("") }

    if (showAmendDialog) {
        AlertDialog(
            onDismissRequest = { showAmendDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("AMEND POLICY", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Suggest a fix for: ${revision.originalElement}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = userSuggestion,
                        onValueChange = { userSuggestion = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter engineering or social fix...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onAmend(userSuggestion)
                        showAmendDialog = false
                        userSuggestion = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("SUBMIT", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAmendDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    GlassPanel(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(2.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = revision.originalElement.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 24.sp
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
                        text = status,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                PolicySection(
                    title = "AGENT DELIBERATION",
                    content = if (revision.failureModeDetected != "None") revision.failureModeDetected else "Consensus verified: Aligns with local mobility patterns.",
                    icon = Icons.Default.Forum,
                    color = Color.White.copy(alpha = 0.6f)
                )

                if (revision.amendedDesignFix != "None") {
                    PolicySection(
                        title = if (revision.amendedDesignFix.startsWith("USER")) "USER INPUT ACTIVE" else "PROPOSED AI FIX",
                        content = revision.amendedDesignFix,
                        icon = if (revision.amendedDesignFix.startsWith("USER")) Icons.Default.Person else Icons.Default.AutoFixHigh,
                        color = if (revision.amendedDesignFix.startsWith("USER")) Color(0xFF00E5FF) else Color(0xFF00E676)
                    )
                }
            }

            // Interactive Button for active participation - only show if revisionId is not null
            if (revision.revisionId != null) {
                Button(
                    onClick = { showAmendDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConflict) Color(0xFFFF3D00).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, if (isConflict) Color(0xFFFF3D00) else Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isConflict) Color.White else Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isConflict) "OVERRIDE CONFLICT" else "MODIFY ELEMENT",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PolicySection(
    title: String,
    content: String,
    icon: ImageVector,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
        }
        Text(
            text = content,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp),
            lineHeight = 20.sp
        )
    }
}
