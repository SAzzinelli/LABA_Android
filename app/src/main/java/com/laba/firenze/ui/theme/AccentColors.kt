package com.laba.firenze.ui.theme

import androidx.compose.ui.graphics.Color

object AccentColors {
    
    fun getAccentColor(accentKey: String): Color {
        return when (accentKey) {
            "system" -> Color(0xFF007AFF) // Blue system
            "peach" -> Color(0xFFFF9500) // Orange
            "lavender" -> Color(0xFFAF52DE) // Purple
            "mint" -> Color(0xFF00C7BE) // Teal
            "sand" -> Color(0xFFF2CC8C) // Beige
            "sky" -> Color(0xFF5AC8FA) // Light blue
            "brand" -> Color(0xFF0A84FF) // LABA blue
            "IED" -> Color(0xFFBB271A) // Red
            else -> Color(0xFF007AFF) // Default
        }
    }
    
    fun getAccentColorVariants(accentKey: String): AccentColorVariants {
        val baseColor = getAccentColor(accentKey)
        return AccentColorVariants(
            primary = baseColor,
            onPrimary = Color.White,
            primaryContainer = baseColor.copy(alpha = 0.1f),
            onPrimaryContainer = baseColor,
            secondary = baseColor.copy(alpha = 0.8f),
            onSecondary = Color.White,
            tertiary = baseColor.copy(alpha = 0.6f),
            onTertiary = Color.White
        )
    }
}

data class AccentColorVariants(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color
)
