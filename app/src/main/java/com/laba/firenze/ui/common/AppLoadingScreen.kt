package com.laba.firenze.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.laba.firenze.R

@Composable
fun AppLoadingScreen(
    modifier: Modifier = Modifier
) {
    // Animazione di pulsazione per il logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo LABA con animazione - usa logo blu in light mode, bianco in dark mode
            val isDarkTheme = isSystemInDarkTheme()
            Image(
                painter = painterResource(
                    id = if (isDarkTheme) R.drawable.bianco else R.drawable.blu
                ),
                contentDescription = "LABA Logo",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alpha)
            )
            
            // Loading indicator sotto il logo
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = stringResource(R.string.loading),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
