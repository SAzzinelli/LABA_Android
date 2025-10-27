package com.laba.firenze.ui.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermissionHelper(
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    
    // Check if we're on Android 13+ (API 33+) where POST_NOTIFICATIONS is required
    val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    
    // Check current permission status
    val hasPermission = remember {
        if (needsNotificationPermission) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // On older versions, notifications are enabled by default
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        onPermissionResult(isGranted)
    }
    
    // Show dialog first, then request permission
    LaunchedEffect(Unit) {
        if (needsNotificationPermission && !hasPermission) {
            // Show explanation dialog first
            showDialog = true
        } else {
            // Permission already granted or not needed
            onPermissionResult(true)
        }
    }
    
    // Dialog for permission explanation
    if (showDialog) {
        NotificationPermissionDialog(
            onDismiss = {
                showDialog = false
                onPermissionResult(false)
            },
            onConfirm = {
                showDialog = false
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        )
    }
}

fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // On older versions, notifications are enabled by default
    }
}
