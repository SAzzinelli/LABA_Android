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
        PillKind.YEAR -> Color(0xFF1565C0)   // blue scuro, contrasto OK
        PillKind.GRADE -> Color(0xFF2E7D32)  // green scuro
        PillKind.CFA -> Color(0xFFE65100)    // arancione scuro, contrasto OK
        PillKind.STATUS -> Color(0xFFE65100) // arancione scuro
        PillKind.ALERT -> Color(0xFFC62828)  // red scuro
    }
    val fillOpacity = 1f
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
