package com.laba.firenze.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.ui.notifications.viewmodel.InboxNotificationsViewModel
import com.laba.firenze.ui.notifications.viewmodel.InboxNotificationsViewModel.NotificationDisplayItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxNotificationsScreen(
    navController: NavController,
    viewModel: InboxNotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedNotification by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifiche") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        var showMenu by remember { mutableStateOf(false) }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Segna tutte lette") },
                                leadingIcon = { Icon(Icons.Default.Check, null) },
                                onClick = { viewModel.markAllAsRead(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Elimina tutte") },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick = { /* TODO */; showMenu = false }
                            )
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Azioni")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Nessuna notifica",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notifications,
                    key = { it.id }
                ) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = { selectedNotification = notification.id },
                        onDismiss = { viewModel.dismiss(notification.id) }
                    )
                }
            }
        }
        
        // Navigate to detail when notification is selected
        selectedNotification?.let { id ->
            val notification = notifications.find { it.id == id }
            if (notification != null) {
                NotificationDetailSheet(
                    notification = notification,
                    onDismiss = { selectedNotification = null },
                    onReadChanged = { read -> viewModel.setRead(id, read) },
                    onDelete = { 
                        viewModel.dismiss(id)
                        selectedNotification = null 
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationDisplayItem,
    onClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Bell icon
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (notification.isRead) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ) {
                Box(
                    modifier = Modifier.size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (notification.isRead) Icons.Default.Notifications else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (notification.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 2,
                        lineHeight = MaterialTheme.typography.titleMedium.lineHeight
                    )
                    Text(
                        text = notification.dateString,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.25
                )
            }
        }
    }
}

