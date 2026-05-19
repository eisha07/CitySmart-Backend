package edu.skku.cs.citysmart.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.skku.cs.citysmart.domain.FeasibilityScores

/**
 * Step 22-25: Modular Custom Circular Progress Gauge utilizing Canvas Arc calculations.
 */
@Composable
fun CircularFeasibilityGauge(
    label: String,
    score: Float, // Value between 0.0f and 100.0f
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    // Smoothly animate the gauge sweeping fill when data updates
    val animatedSweepAngle by animateFloatAsState(
        targetValue = (score / 100f) * 360f,
        animationSpec = tween(durationMillis = 1200)
    )

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(90.dp)
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                // Draw background track dim ring
                drawCircle(
                    color = themeColor.copy(alpha = 0.15f),
                    style = Stroke(width = 8.dp.toPx())
                )

                // Draw animated neon indicator track arc
                drawArc(
                    color = themeColor,
                    startAngle = -90f, // Start at absolute top center
                    sweepAngle = animatedSweepAngle,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Numeric Core Value display
            Text(
                text = "${score.toInt()}%",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Component Sub-label description string
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}
@Composable
fun TriPillarGaugePanel(
    scores: FeasibilityScores,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Social Impact (Safety focus - Neon Cyan)
        CircularFeasibilityGauge(
            label = "Social Impact",
            score = scores.socialAcceptance,
            themeColor = Color(0xFF00E5FF)
        )

        // 2. Economic Viability (Commerce focus - Neon Green)
        CircularFeasibilityGauge(
            label = "Economic ROI",
            score = scores.economicRoi,
            themeColor = Color(0xFF00E676)
        )

        // 3. Political Feasibility (Compliance focus - Neon Purple)
        CircularFeasibilityGauge(
            label = "Political Fit",
            score = scores.politicalJustification,
            themeColor = Color(0xFFD500F9)
        )
    }
}
