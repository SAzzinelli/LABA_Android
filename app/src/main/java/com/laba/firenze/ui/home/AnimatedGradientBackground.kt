package com.laba.firenze.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun AnimatedGradientBackground(
    baseColor: Color,
    examsRemaining: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // Genera seed casuali stabili per il movimento organico
    val seeds = remember { (0..5).map { kotlin.random.Random.nextFloat() * 1000f } }
    
    // Animazione continua
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing), // 30 secondi per un ciclo
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
    ) {
        drawAnimatedGradient(
            time = time,
            size = size,
            density = density.density,
            baseColor = baseColor,
            examsRemaining = examsRemaining,
            seeds = seeds
        )
    }
}

private fun DrawScope.drawAnimatedGradient(
    time: Float,
    size: androidx.compose.ui.geometry.Size,
    density: Float,
    baseColor: Color,
    examsRemaining: Int,
    seeds: List<Float>
) {
    val shades = generateColorShades(baseColor, examsRemaining)
    val minSide = min(size.width, size.height)
    
    // Fattore di intensità che cresce quando examsRemaining -> 1
    val maxStep = 5
    val clamped = max(1, min(maxStep, examsRemaining))
    val factor = 1f - (clamped - 1).toFloat() / (maxStep - 1).toFloat()
    
    // Disegna 6 punti di luce animati
    for (i in 0..5) {
        // Scegli un colore dalla palette
        val color = if (shades.isEmpty()) {
            baseColor.copy(alpha = 0.6f)
        } else {
            shades[i % shades.size].copy(alpha = 0.75f + 0.15f * factor)
        }
        
        // Movimento organico usando i seed
        val sx = seeds[i % seeds.size]
        val sy = seeds[(i + 1) % seeds.size]
        val px = 0.5f + 0.42f * sin(time * 2 * PI.toFloat() * 0.18f + sx / 37f + i)
        val py = 0.5f + 0.42f * cos(time * 2 * PI.toFloat() * 0.15f + sy / 41f + i)
        
        // Raggio che scala con la dimensione della view + intensità
        val baseRadius = minSide * (0.20f + 0.10f * (i % 3))
        val radius = baseRadius * (0.92f + 0.55f * factor)
        
        val center = Offset(px * size.width, py * size.height)
        
        // Crea il gradiente radiale
        val gradient = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.85f),
                color.copy(alpha = 0.35f + 0.25f * factor),
                color.copy(alpha = 0f)
            ),
            center = center,
            radius = radius
        )
        
        // Disegna il cerchio con gradiente
        drawCircle(
            brush = gradient,
            radius = radius,
            center = center
        )
    }
}

private fun generateColorShades(baseColor: Color, examsRemaining: Int): List<Color> {
    // Versione semplificata che usa variazioni del colore base
    val factor = when (examsRemaining) {
        1 -> 1.0f  // Massima intensità
        2 -> 0.8f
        3 -> 0.6f
        4 -> 0.4f
        else -> 0.2f  // Minima intensità
    }
    
    return listOf(
        baseColor.copy(alpha = 0.9f * factor),
        baseColor.copy(alpha = 0.7f * factor),
        baseColor.copy(alpha = 0.5f * factor),
        baseColor.copy(alpha = 0.3f * factor)
    )
}

