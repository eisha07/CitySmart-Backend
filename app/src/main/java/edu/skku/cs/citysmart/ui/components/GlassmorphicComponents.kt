package edu.skku.cs.citysmart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import edu.skku.cs.citysmart.ui.theme.DeepPurpleCard

@Composable
fun Soft3DCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                // Bottom-right dark shadow
                drawRect(
                    color = Color.Black.copy(alpha = 0.3f),
                    topLeft = Offset(8f, 8f),
                    size = size
                )
            }
            .background(DeepPurpleCard, RoundedCornerShape(24.dp))
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                RoundedCornerShape(24.dp)
            )
            .blur(20.dp) // Frosted glass effect
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
