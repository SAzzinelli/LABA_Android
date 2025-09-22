package com.laba.firenze

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laba.firenze.ui.LABANavigation
import com.laba.firenze.ui.common.AppLoadingScreen
import com.laba.firenze.ui.common.LoginScreen
import com.laba.firenze.ui.theme.LABAFirenzeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Status bar trasparente
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        
        setContent {
            LABAFirenzeTheme {
                LABAAuthWrapper()
            }
        }
    }
}

@Composable
fun LABAAuthWrapper(
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    // Gestione dinamica della status bar basata sul tema
    val isDarkTheme = isSystemInDarkTheme()
    val view = LocalView.current
    
    LaunchedEffect(isDarkTheme) {
        val windowInsetsController = WindowInsetsControllerCompat(
            (view.context as android.app.Activity).window,
            view
        )
        windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme
    }
    
    when {
        authState.isLoading -> {
            // Show loading screen
            AppLoadingScreen()
        }
        authState.isLoggedIn -> {
            // Show main app
            LABANavigation(
                modifier = Modifier.fillMaxSize()
            )
        }
        else -> {
            // Show login screen
            LoginScreen()
        }
    }
}
