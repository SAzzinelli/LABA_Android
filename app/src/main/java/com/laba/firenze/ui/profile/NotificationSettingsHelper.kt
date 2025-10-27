package com.laba.firenze.ui.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun openNotificationSettings() {
    val context = LocalContext.current
    
    try {
        // Prima prova ad aprire le impostazioni specifiche delle notifiche dell'app
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+ - Impostazioni notifiche specifiche dell'app
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            // Android 7.1 e precedenti - Impostazioni generali app
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: apri le impostazioni generali delle notifiche
        try {
            val fallbackIntent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            // Ultimo fallback: impostazioni generali
            try {
                val generalIntent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(generalIntent)
            } catch (e3: Exception) {
                // Se tutto fallisce, non fare nulla
            }
        }
    }
}

fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                PackageManager.PERMISSION_GRANTED
    } else {
        // Su versioni precedenti, le notifiche sono abilitate di default
        true
    }
}
