package com.laba.firenze.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.data.NotificationCategory
import com.laba.firenze.ui.notifications.viewmodel.NotificationSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifiche") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                // Main toggle
                item {
                    NotificationSection(
                        title = "Notifiche app"
                    ) {
                        NotificationToggleItem(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = viewModel::setNotificationsEnabled,
                            title = "Abilita tutte le notifiche",
                            icon = Icons.Default.Notifications
                        )
                        
                        // Category toggles
                        if (uiState.notificationsEnabled) {
                            NotificationCategory.values().forEach { category ->
                                NotificationToggleItem(
                                    checked = viewModel.getCategoryEnabled(category),
                                    onCheckedChange = { enabled ->
                                        viewModel.setCategoryEnabled(category, enabled)
                                    },
                                    title = category.displayName,
                                    icon = getIconForCategory(category)
                                )
                            }
                            
                            // Warning footer
                            Text(
                                text = "Disattivando le categorie singole potresti non ricevere comunicazioni importanti come assenze docenti o comunicazioni da professori. Anche se permesso per la privacy policy, è fortemente sconsigliato disattivarle.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
        }
    }
}

@Composable
fun NotificationSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun NotificationToggleItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        ListItem(
            headlineContent = { Text(title) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
    }
}

fun getIconForCategory(category: NotificationCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        NotificationCategory.EXAMS -> Icons.Default.Description
        NotificationCategory.GRADES -> Icons.Default.Numbers
        NotificationCategory.ABSENCES -> Icons.Default.PersonRemove
        NotificationCategory.PROFESSORS -> Icons.Default.Person
        NotificationCategory.MATERIALS -> Icons.Default.Folder
        NotificationCategory.SEMINARS -> Icons.Default.Book
        NotificationCategory.EVENTS -> Icons.Default.Event
        NotificationCategory.GENERAL -> Icons.Default.ChatBubble
    }
}

