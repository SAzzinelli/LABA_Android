package com.laba.firenze.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Pill component (allineato a iOS Pill)
 * Uso: status pills nell'header, badge anno/CFA in Corsi e Esami
 */
enum class PillKind { YEAR, GRADE, CFA, STATUS, ALERT }

@Composable
fun Pill(
    text: String,
    kind: PillKind,
    tintOverride: Color? = null,
    modifier: Modifier = Modifier
) {
    val baseColor = tintOverride ?: when (kind) {
        PillKind.YEAR -> Color(0xFF007AFF)   // systemBlue
        PillKind.GRADE -> Color(0xFF34C759)  // systemGreen
        PillKind.CFA -> Color(0xFFFF9500)    // systemOrange
        PillKind.STATUS -> Color(0xFFFF9500) // systemOrange
        PillKind.ALERT -> Color(0xFFFF3B30)  // systemRed
    }
    val fillOpacity = 0.88f
    Box(
        modifier = modifier
            .background(
                color = baseColor.copy(alpha = fillOpacity),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}
