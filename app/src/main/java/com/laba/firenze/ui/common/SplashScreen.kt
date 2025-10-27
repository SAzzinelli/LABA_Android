package com.laba.firenze.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.laba.firenze.R

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isDarkTheme) Color.Black else Color.White
            ),
        contentAlignment = Alignment.Center
    ) {
        // Logo LABA centrale - usa logo tutto blu senza sfondo
        Image(
            painter = painterResource(
                id = R.drawable.blu
            ),
            contentDescription = "LABA Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}
