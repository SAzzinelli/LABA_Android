package com.laba.firenze

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laba.firenze.ui.LABANavigation
import com.laba.firenze.ui.common.SplashScreen
import com.laba.firenze.ui.common.LoginScreen
import com.laba.firenze.ui.common.NotificationPermissionHelper
import com.laba.firenze.ui.theme.LABAFirenzeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configura il comportamento della tastiera per evitare che sia tagliata (nuova API senza deprecazione)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            // Gestisce la tastiera automaticamente
        }
        
        // Imposta la status bar trasparente per permettere all'app di estendersi sotto
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        
        setContent {
            LABAAuthWrapper()
        }
    }
}

@Composable
fun LABAAuthWrapper(
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    LABAFirenzeTheme {
        when {
            authState.isLoading -> {
                // Show splash screen
                SplashScreen()
            }
            authState.isLoggedIn -> {
                // Request notification permission after successful login
                NotificationPermissionHelper { _ ->
                    // Permission granted or denied - we can log this or handle accordingly
                    // For now, we just proceed with the app
                }
                
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
}
