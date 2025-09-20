package com.laba.firenze.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.laba.firenze.ui.theme.LABA_Blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(userProfile = uiState.userProfile)
        }
        
        // Resources Section
        item {
            ProfileSection(
                title = "Risorse",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Programmi didattici",
                        icon = Icons.Default.School,
                        onClick = { navController.navigate("materials") }
                    ),
                    ProfileMenuActionItem(
                        title = "Dispense",
                        icon = Icons.Default.Description,
                        onClick = { navController.navigate("handouts") }
                    ),
                    ProfileMenuActionItem(
                        title = "Tesi di laurea",
                        icon = Icons.Default.Book,
                        onClick = { navController.navigate("thesis") }
                    )
                )
            )
        }
        
        // Assistance Section
        item {
            ProfileSection(
                title = "Assistenza",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Regolamenti",
                        icon = Icons.Default.Rule,
                        onClick = { navController.navigate("regulations") }
                    ),
                    ProfileMenuActionItem(
                        title = "FAQ",
                        icon = Icons.Default.Help,
                        onClick = { /* TODO: Implement FAQ */ }
                    )
                )
            )
        }
        
        // Preferences Section
        item {
            ProfileSection(
                title = "Preferenze",
                items = listOf(
                    ProfileToggleItem(
                        title = "Notifiche",
                        icon = Icons.Default.Notifications,
                        isChecked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::updateNotificationsEnabled
                    ),
                    ProfileMenuActionItem(
                        title = "Aspetto",
                        icon = Icons.Default.Palette,
                        onClick = { /* TODO: Implement appearance settings */ }
                    )
                )
            )
        }
        
        // Contacts Section
        item {
            ProfileSection(
                title = "Contatti",
                items = listOf(
                    ProfileLinkItem(
                        title = "Email LABA",
                        icon = Icons.Default.Email,
                        value = uiState.userProfile?.emailLABA ?: "N/A"
                    ),
                    ProfileLinkItem(
                        title = "Telefono",
                        icon = Icons.Default.Phone,
                        value = uiState.userProfile?.telefono ?: "N/A"
                    )
                )
            )
        }
        
        // Useful Links Section
        item {
            ProfileSection(
                title = "Link utili",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Sito LABA",
                        icon = Icons.Default.Language,
                        onClick = { /* TODO: Open LABA website */ }
                    )
                )
            )
        }
        
        // Actions Section
        item {
            ProfileSection(
                title = "Azioni",
                items = listOf(
                    ProfileMenuActionItem(
                        title = "Ricarica Dati",
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.refreshData() }
                    ),
                    ProfileActionItem(
                        title = "Debug",
                        icon = Icons.Default.BugReport,
                        iconColor = Color(0xFF6200EA),
                        onClick = { viewModel.showDebugInfo() }
                    ),
                    ProfileActionItem(
                        title = "Esci",
                        icon = Icons.Default.ExitToApp,
                        iconColor = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.logout() }
                    )
                )
            )
        }
    }
}

@Composable
private fun ProfileHeader(userProfile: com.laba.firenze.domain.model.StudentProfile?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LABA_Blue
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar placeholder
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                }
                
                Column {
                    Text(
                        text = userProfile?.displayName ?: "Studente LABA",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userProfile?.matricola ?: "Matricola non disponibile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = LABA_Blue
        )
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileMenuItem(item = item)
                    if (index < items.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(item: ProfileMenuItem) {
    when (item) {
        is ProfileMenuActionItem -> {
            ListItem(
                headlineContent = { Text(item.title) },
                leadingContent = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = LABA_Blue
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable { item.onClick() }
            )
        }
        is ProfileToggleItem -> {
            ListItem(
                headlineContent = { Text(item.title) },
                leadingContent = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = LABA_Blue
                    )
                },
                trailingContent = {
                    Switch(
                        checked = item.isChecked,
                        onCheckedChange = item.onCheckedChange
                    )
                }
            )
        }
        is ProfileLinkItem -> {
            ListItem(
                headlineContent = { Text(item.title) },
                supportingContent = { Text(item.value) },
                leadingContent = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = LABA_Blue
                    )
                }
            )
        }
        is ProfileActionItem -> {
            ListItem(
                headlineContent = { 
                    Text(
                        text = item.title,
                        color = item.iconColor
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = item.iconColor
                    )
                },
                modifier = Modifier.clickable { item.onClick() }
            )
        }
    }
}

// Sealed class for different menu item types
sealed class ProfileMenuItem

data class ProfileMenuActionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
) : ProfileMenuItem()

data class ProfileToggleItem(
    val title: String,
    val icon: ImageVector,
    val isChecked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
) : ProfileMenuItem()

data class ProfileLinkItem(
    val title: String,
    val icon: ImageVector,
    val value: String
) : ProfileMenuItem()

data class ProfileActionItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color = Color.Black,
    val onClick: () -> Unit
) : ProfileMenuItem()

@Composable
fun ProfileViewModel(): ProfileViewModel {
    return hiltViewModel()
}
