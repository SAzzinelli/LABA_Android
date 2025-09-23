package com.laba.firenze

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
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
        
        // Permette all'app di estendersi sotto la status bar mantenendo i contenuti visibili
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
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
    
    val view = LocalView.current
    
    // Nessuna configurazione personalizzata - Android gestisce tutto automaticamente
    
    LABAFirenzeTheme {
        when {
            authState.isLoading -> {
                // Show splash screen
                SplashScreen()
            }
            authState.isLoggedIn -> {
                // Request notification permission after successful login
                NotificationPermissionHelper { permissionGranted ->
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
