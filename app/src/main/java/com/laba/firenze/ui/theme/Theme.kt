package com.laba.firenze.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

/** Mappa accent key → colore primario (allineato a ColorSettingsScreen) */
private fun accentColorForKey(key: String): Color = when (key) {
    "peach" -> Color(0xFFFF9500)
    "lavender" -> Color(0xFFAF52DE)
    "mint" -> Color(0xFF00C896)
    "sand" -> Color(0xFFF1C40F)
    "sky" -> Color(0xFF5AC8FA)
    "brand" -> Color(0xFF007AFF)
    "dark" -> Color(0xFF1C1C1E)
    "IED" -> Color(0xFFBB271A)
    else -> Color(0xFF007AFF) // system / default
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF49454F),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFEAE1D9),
    onSurface = Color(0xFFEAE1D9),
    onSurfaceVariant = Color(0xFFD3C4B4),
    outline = Color(0xFF9C8F80),
    outlineVariant = Color(0xFF4F4539),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    surfaceVariant = Color(0xFFE7E0EC),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
)

@Composable
fun LABAFirenzeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    accentKey: String = "system",
    content: @Composable () -> Unit
) {
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && accentKey == "system" -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Applica accent personalizzato (da Aspetto > Colori)
    if (accentKey != "system") {
        val accent = accentColorForKey(accentKey)
        colorScheme = colorScheme.copy(
            primary = accent,
            primaryContainer = accent.copy(alpha = 0.3f),
            onPrimaryContainer = accent
        )
    }
    
    // Icone status bar chiare/scure in base al tema (status bar trasparente da MainActivity)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
