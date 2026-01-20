package com.laba.firenze.ui.appearance

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.laba.firenze.ui.navigation.AppTab
import com.laba.firenze.ui.navigation.NavigationManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationCustomizationScreen(
    navController: NavController,
    navigationManager: NavigationManager
) {
    val activeTabs by navigationManager.activeTabs.collectAsState()
    val hiddenTabs by navigationManager.hiddenTabs.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalizza Menu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Active Section
            item {
                Text(
                    "Barra di Navigazione",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(activeTabs) { tab ->
                val index = activeTabs.indexOf(tab)
                val isHome = tab == AppTab.HOME
                val isProfile = tab == AppTab.PROFILE
                
                ActiveTabRow(
                    tab = tab,
                    isFirst = index == 0,
                    isLast = index == activeTabs.lastIndex,
                    isLocked = tab.isLocked, // HOME e PROFILO sono locked
                    onRemove = { 
                        // Blocca rimozione per tab locked
                        if (!tab.isLocked) {
                            navigationManager.toggleTabVisibility(tab)
                        }
                    },
                    onMoveUp = { 
                        // Blocca movimento per HOME (sempre primo) e tab locked
                        if (!tab.isLocked && !isHome) {
                            navigationManager.moveTab(index, index - 1)
                        }
                    },
                    onMoveDown = { 
                        // Blocca movimento per PROFILO (sempre ultimo) e tab locked
                        if (!tab.isLocked && !isProfile) {
                            navigationManager.moveTab(index, index + 1)
                        }
                    }
                )
            }
            
            item {
                Text(
                    "Usa le frecce per riordinare. Home e Profilo sono bloccati e non possono essere rimossi o spostati.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Hidden Section
            if (hiddenTabs.isNotEmpty()) {
                item {
                    Text(
                        "Altre Risorse",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(hiddenTabs) { tab ->
                    HiddenTabRow(
                        tab = tab,
                        onAdd = { navigationManager.toggleTabVisibility(tab) },
                        canAdd = activeTabs.size < 5
                    )
                }
            }
            
            // Reset Button
            item {
                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Ripristina default")
                }
            }
        }
    }
    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Ripristina Impostazioni") },
            text = { Text("Sei sicuro di voler ripristinare la barra di navigazione predefinita?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        navigationManager.resetToDefault()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ripristina")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun ActiveTabRow(
    tab: AppTab,
    isFirst: Boolean,
    isLast: Boolean,
    isLocked: Boolean,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Action Icon (Remove or Lock)
                if (tab.isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp),
                        enabled = !isLocked
                    ) {
                        Icon(
                            Icons.Default.RemoveCircle,
                            contentDescription = "Remove",
                            tint = if (isLocked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Content
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        tab.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        tab.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Reorder Controls (bloccati per HOME e PROFILO)
            if (isLocked) {
                // Tab locked: non mostrare controlli di movimento (HOME e PROFILO)
                Spacer(modifier = Modifier.size(64.dp))
            } else {
                // Tab non locked: mostra controlli di movimento
                Row {
                    if (!isFirst) {
                        IconButton(
                            onClick = onMoveUp, 
                            modifier = Modifier.size(32.dp),
                            enabled = !isLocked
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp, 
                                contentDescription = "Move Up",
                                tint = if (isLocked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                    
                    if (!isLast) {
                        IconButton(
                            onClick = onMoveDown, 
                            modifier = Modifier.size(32.dp),
                            enabled = !isLocked
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown, 
                                contentDescription = "Move Down",
                                tint = if (isLocked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HiddenTabRow(
    tab: AppTab,
    onAdd: () -> Unit,
    canAdd: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add Button
            IconButton(
                onClick = onAdd,
                enabled = canAdd,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = "Add",
                    tint = if (canAdd) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                )
            }
            
            // Content
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    tab.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    tab.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
