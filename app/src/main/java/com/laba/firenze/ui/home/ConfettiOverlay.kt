package com.laba.firenze.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // Animazione continua per le particelle
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing), // 30 secondi per un ciclo completo
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawConfettiPattern(
            time = time,
            size = size,
            density = density.density
        )
    }
}

private fun DrawScope.drawConfettiPattern(
    time: Float,
    size: androidx.compose.ui.geometry.Size,
    density: Float
) {
    val color = Color.White.copy(alpha = 0.28f)
    val step = 28.dp.toPx()
    val radius = 3.0.dp.toPx()
    val t = time * 0.6f
    
    // Genera le particelle in una griglia
    var y = -step
    while (y <= size.height + step) {
        var x = -step
        while (x <= size.width + step) {
            val seed = (x * 13 + y * 7).toInt()
            val dx = sin((x + y) / 140 + t * (1.2f + 0.17f * sin(seed.toFloat()))) * 9 * (0.8f + 0.3f * cos(seed.toFloat()))
            val dy = cos((x - y) / 120 + t * (1.3f + 0.23f * cos((seed + 99).toFloat()))) * 9 * (0.8f + 0.3f * sin((seed + 42).toFloat()))
            
            val centerX = x + dx
            val centerY = y + dy
            
            // Disegna il cerchio
            val path = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        centerX - radius,
                        centerY - radius,
                        centerX + radius,
                        centerY + radius
                    )
                )
            }
            
            // Riempie il cerchio
            drawPath(
                path = path,
                color = color,
                style = Fill
            )
            
            // Contorno del cerchio
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 0.5f)
            )
            
            x += step
        }
        y += step
    }
}

