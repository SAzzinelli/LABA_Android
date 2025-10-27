package com.laba.firenze.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView

@Composable
fun LABAFirenzeTheme(
    content: @Composable () -> Unit
) {
    val context = LocalView.current.context
    val isDarkTheme = isSystemInDarkTheme()
    val colorScheme = if (isDarkTheme) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
